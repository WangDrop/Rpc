package com.wc.rpc.test.app;

import com.wc.rpc.client.AsyncRPCCallback;
import com.wc.rpc.client.RPCFuture;
import com.wc.rpc.client.RpcClient;
import com.wc.rpc.client.proxy.IAsyncObjectProxy;
import com.wc.rpc.registry.ServiceDiscovery;
import com.wc.rpc.test.client.HelloPersonService;
import com.wc.rpc.test.client.HelloService;
import com.wc.rpc.test.client.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by 12083 on 2016/8/24.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class HelloPersonCallbackTest {
    @Autowired
    RpcClient rpcClient;

    @Test
    public void testCallBask() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            IAsyncObjectProxy proxy = RpcClient.createAsync(HelloPersonService.class);
            int num = 10;
            final RPCFuture future = proxy.call("getTestPerson", "wangcheng ", num);
            future.addCallback(new AsyncRPCCallback() {
                @Override
                public void success(Object result) {
                    List<Person> list = (List<Person>) result;
                    for (Person p : list) {
                        System.out.println(p);
                    }
                    countDownLatch.countDown();
                }

                @Override
                public void fail(Exception e) {
                    System.out.println(e);
                    countDownLatch.countDown();
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        rpcClient.stop();
        System.out.println("End the process! ");
    }

    @Test
    public void testCallBackHello() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        IAsyncObjectProxy proxy = RpcClient.createAsync(HelloService.class);
        try {
            final RPCFuture future = proxy.call("hello", "wangcheng");
            //注意，下面添加回调函数的时候，是不会出现结果已经完成但是无法调用的情况的
            //addCallBack会首先尝试去查看这个future是否是done的情况，如果是的话那么
            //直接调用回调，当然，addCallBack的时候是会加锁的首先
            future.addCallback(new AsyncRPCCallback() {
                @Override
                public void success(Object result) {
                    System.out.println("The result is " + result);
                    countDownLatch.countDown();
                }

                @Override
                public void fail(Exception e) {
                    System.out.println(e);
                    countDownLatch.countDown();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
