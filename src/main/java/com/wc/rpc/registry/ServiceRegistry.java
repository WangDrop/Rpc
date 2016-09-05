package com.wc.rpc.registry;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 12083 on 2016/8/23.
 */
public class ServiceRegistry {
    private static Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
    private CountDownLatch latch = new CountDownLatch(1);
    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    /**
     * 服务注册
     */
    public void register(String data) {
        if (data != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                addRootNode(zk); //add path
                addDataNode(zk, data); //add data node
            }
        }
    }

    /**
     * 连接zookeeper服务
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException e) {
            LOGGER.error("", e);
        } catch (InterruptedException ex) {
            LOGGER.error("", ex);
        }
        return zk;
    }

    /**
     * 增加目录节点
     */
    private void addRootNode(ZooKeeper zk) {
        try {
            Stat stat = zk.exists(Constant.ZK_REGISTRY_PATH, false);
            if (stat == null) { // 如果目录的节点首先是不存在的
                //目录为持久化的，ACL是初始的Access Control，也就是初始的访问权限
                zk.create(Constant.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOGGER.debug("create zoo path on path " + Constant.ZK_REGISTRY_PATH);
            }
        } catch (KeeperException e) {
            LOGGER.error("", e);
        } catch (InterruptedException ee) {
            LOGGER.error("", ee);
        }
    }


    /**
     * 增加数据节点
     */
    private void addDataNode(ZooKeeper zk, String data) {
        try {
            byte[] bytes = data.getBytes();

            String path = zk.create(Constant.ZK_DATA_PATH, bytes,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);//这里创建的是一个临时顺序编号的节点
            LOGGER.debug("create zoo data on path " + Constant.ZK_DATA_PATH);
        } catch (InterruptedException e) {
            LOGGER.debug("", e);
        } catch (KeeperException ee) {
            LOGGER.debug("", ee);
        }
    }
}
