package com.wc.rpc.client;

import com.wc.rpc.protocol.RpcRequest;
import com.wc.rpc.protocol.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * Created by 12083 on 2016/8/23.
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    public static final Logger LOGGER = LoggerFactory.getLogger(RpcClientHandler.class);
    private ConcurrentHashMap<String, RPCFuture> pendingRPC = //注意RPCFuture和requestId是一一对应的
            new ConcurrentHashMap<String, RPCFuture>();
    private volatile Channel channel;
    private SocketAddress remotePeer;

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RPCFuture sendRequest(RpcRequest request) {
        RPCFuture future = new RPCFuture(request);
        pendingRPC.put(request.getRequestId(), future); //为了实现异步调用
        channel.writeAndFlush(request);

        return future;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        if (rpcResponse != null) {
            String reqId = rpcResponse.getRequestId();
            RPCFuture future = pendingRPC.get(reqId);
            if (future != null) {
                pendingRPC.remove(reqId);
                future.done(rpcResponse);
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel(); //让外界可以通过channel访问到对端的address
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("", cause);
        ctx.close();
    }
}
