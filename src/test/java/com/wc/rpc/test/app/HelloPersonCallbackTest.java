package com.wc.rpc.test.app;

import com.wc.rpc.client.AsyncRPCCallback;
import com.wc.rpc.client.RPCFuture;
import com.wc.rpc.client.RpcClient;
import com.wc.rpc.client.proxy.IAsyncObjectProxy;
import com.wc.rpc.registry.ServiceDiscovery;
import com.wc.rpc.test.client.HelloPersonService;
import com.wc.rpc.test.client.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 12083 on 2016/8/24.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class HelloPersonCallbackTest {
    @Autowired
    RpcClient rpcClient;

    @Test
    public void testCallBask() throws Exception{
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
}
