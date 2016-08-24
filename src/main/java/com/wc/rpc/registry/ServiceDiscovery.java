package com.wc.rpc.registry;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.wc.rpc.client.ConnectionManager;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 12083 on 2016/8/23.
 */
public class ServiceDiscovery {
    private static Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private volatile List<String> dataList = new ArrayList<>();
    private String registryAddress;
    private ZooKeeper zooKeeper;

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        zooKeeper = connectServer();
        if (zooKeeper != null){
            watchNode(zooKeeper); //开始观测节点，发现服务
        }
    }


    /**
     * @return zookeeper对象
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        } catch (IOException ee) {
            LOGGER.error("", ee);
        }
        return zk;
    }

    /**
     * 观察节点的变更情况
     */
    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDataChanged)
                        watchNode(zk);
                }
            });
            List<String> dataList = new ArrayList<>();
            int sz = nodeList.size();
            for (int i = 0; i < sz; ++i) {
                byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + nodeList.get(i), false, null);
                if (data != null)
                    dataList.add(new String(data));
            }

            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;

            LOGGER.debug("Service discovery triggered updating connected server node.");
            updateConnectedServer();
        } catch (KeeperException e) {
            LOGGER.error("", e);
        } catch (InterruptedException ee) {
            LOGGER.error("", ee);
        }
    }


    /**
     * 更新已经连接到的节点
     */
    private void updateConnectedServer(){
        ConnectionManager.getInstance().updateConnectedServer(this.dataList);
    }

    /**
     * 停止发现服务
     */
    public void stop(){
        if(zooKeeper != null){
            try{
                zooKeeper.close();
            }catch(InterruptedException e){
                LOGGER.debug("", e);
            }
            zooKeeper = null;
        }
    }
}
