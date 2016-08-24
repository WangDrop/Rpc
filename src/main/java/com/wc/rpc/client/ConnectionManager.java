package com.wc.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**通过ConnectionManager实现了对InetSocketAddress以及对应的RpcCleintHandler的管理，建立了长连接
 * Created by 12083 on 2016/8/23.
 */
public class ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
    private volatile static ConnectionManager connectionManager;
    EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4); //线程数为4
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
    private CopyOnWriteArrayList<RpcClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private ReentrantLock reentrantLock = new ReentrantLock();
    private Condition connected = reentrantLock.newCondition();
    protected long connectTimeOutMillis = 6000L;
    private AtomicInteger rrNumber = new AtomicInteger(0); //round robin,实现负载均衡
    private volatile boolean isRunning = true;

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        if (connectionManager == null) {
            synchronized (ConnectionManager.class) {
                if (connectionManager == null)
                    connectionManager = new ConnectionManager();
            }
        }
        return connectionManager;
    }

    public void updateConnectedServer(List<String> allServerAddress) {
        String arr[] = null;
        HashSet<InetSocketAddress> newServerAddrs = new HashSet<>();
        if (allServerAddress.size() > 0) {
            for (String addr : allServerAddress) {
                arr = addr.split(":");
                if (arr.length == 2) {
                    final InetSocketAddress remotePeerAddr = new InetSocketAddress(arr[0], Integer.parseInt(arr[1]));
                    newServerAddrs.add(remotePeerAddr);
                }
            }

            for (final InetSocketAddress socketAddress : newServerAddrs) {
                if (!connectedServerNodes.keySet().contains(socketAddress)) {
                    connectServerNode(socketAddress);
                }
            }

            for (int i = 0; i < connectedHandlers.size(); ++i) {
                RpcClientHandler connectedHander = connectedHandlers.get(i);
                SocketAddress remotePeer = connectedHander.getRemotePeer();
                if (!newServerAddrs.contains(remotePeer)) {
                    LOGGER.debug("remove the node from serverNodes");
                    RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                    handler.close();
                    connectedServerNodes.remove(remotePeer);
                    connectedHandlers.remove(i);
                }
            }
        } else { //no more server addresses
            LOGGER.error("No available server node. All server nodes are down !!!");
            for (final RpcClientHandler connectedServerHandler : connectedHandlers) {
                SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                handler.close();
                connectedServerNodes.remove(connectedServerHandler);
            }
            connectedHandlers.clear();
        }
    }

    public void stop() {
        isRunning = false;
        for (RpcClientHandler handler : connectedHandlers) {
            handler.close();
        }
        signalAvailiableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

    /**
     * 连接到服务器节点
     * 使用长连接的方式
     *
     * @param socketAddress
     */
    private void connectServerNode(final InetSocketAddress socketAddress) {
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup).channel(NioSocketChannel.class)
                        .handler(new RpcClientInitializer());
                final ChannelFuture future = b.connect(socketAddress);
                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        LOGGER.debug("successful connect to remote server " + socketAddress);
                        RpcClientHandler handler = future.channel().pipeline().get(RpcClientHandler.class);
                        addHandler(handler);
                    }
                });
            }
        });
    }

    /**
     * 将得到的handler保存起来
     */
    private void addHandler(RpcClientHandler handler) {
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getRemotePeer();
        connectedServerNodes.put(remoteAddress, handler);
        signalAvailiableHandler();
    }

    /**
     * 唤醒等待handler的线程
     */
    private void signalAvailiableHandler() {
        reentrantLock.lock();
        try {
            connected.signalAll();
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 等待handler,但是最多等待6s
     */
    private boolean waitAvailiableHandler() throws InterruptedException {
        reentrantLock.lock();
        try {
            return connected.await(this.connectTimeOutMillis, TimeUnit.MILLISECONDS);
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 选择handler
     */
    public RpcClientHandler chooseHandler() {
        CopyOnWriteArrayList<RpcClientHandler> handlers = (CopyOnWriteArrayList<RpcClientHandler>) connectedHandlers.clone();
        int sz = handlers.size();
        while (isRunning && sz <= 0) {
            try {
                boolean availiable = waitAvailiableHandler(); // 最多等待6S
                if (availiable) {
                    handlers = (CopyOnWriteArrayList<RpcClientHandler>) connectedHandlers.clone();
                    sz = handlers.size();
                }
            } catch (InterruptedException e) {
                LOGGER.debug("", e);
            }
        }
        int index = (rrNumber.getAndAdd(1) + sz) % sz; //从这里实现了负载均衡
        return handlers.get(index);
    }
}
