package com.wc.rpc.test.app;

import com.wc.rpc.client.AsyncRPCCallback;
import com.wc.rpc.client.RPCFuture;
import com.wc.rpc.client.RpcClient;
import com.wc.rpc.client.proxy.IAsyncObjectProxy;
import com.wc.rpc.server.RpcService;
import com.wc.rpc.test.client.HelloPersonService;
import com.wc.rpc.test.client.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 12083 on 2016/8/29.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml") //启用配置文件
public class AsycCallBackBenchMark {
    @Autowired
    private RpcClient rpcClient;

    @Test
    public void testMultiThreadCallBack() throws Exception {
        //IAsyncObjectProxy proxy = rpcClient.createAsync(HelloService.class);
        int threadNum = 100;
        int loopCount = 100000;
        ExecutorService service = Executors.newFixedThreadPool(threadNum);
        final CountDownLatch countDownLatch = new CountDownLatch(loopCount);

        final AsyncRPCCallback asyncRPCCallback = new AsyncRPCCallback() {
            @Override
            public void success(Object result) {
                System.out.println(result);
                countDownLatch.countDown();
            }

            @Override
            public void fail(Exception e) {
                System.out.println(e);
                countDownLatch.countDown();
            }
        };
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < loopCount; ++i) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    IAsyncObjectProxy proxy = rpcClient.createAsync(HelloService.class);
                    RPCFuture future = proxy.call("hello", "world");
                    future.addCallback(asyncRPCCallback);
                }
            });
        }
        countDownLatch.await();
        long timeCost = System.currentTimeMillis() - startTime;
        System.out.println("Thread Num: " + threadNum);
        System.out.println("Loop Count: " + loopCount);
        System.out.println("Total time Cost: " + (double)timeCost/1000.0);
        System.out.println("Tps : " + loopCount/(((double) timeCost) / 1000.0));
    }

}
