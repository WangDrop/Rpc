package com.wc.rpc.test.app;

import com.wc.rpc.client.RPCFuture;
import com.wc.rpc.client.RpcClient;
import com.wc.rpc.client.proxy.IAsyncObjectProxy;
import com.wc.rpc.test.client.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.*;

/**
 * Created by 12083 on 2016/8/24.
 */
public class AsyBenchMark {
    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("client-spring.xml");
        final RpcClient rpcClient = ctx.getBean(RpcClient.class);
        int threadNum = 100;
        int requestNum = 100000;
        final CountDownLatch countDownLatch = new CountDownLatch(requestNum);
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        long startTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < requestNum; ++i) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        final IAsyncObjectProxy helloService = rpcClient.createAsync(HelloService.class);
                        try {
                            System.out.println("" + helloService.call("hello", "wangcheng").get(3000, TimeUnit.MILLISECONDS));
                        } catch (Exception e) {

                        }
                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();
        } finally {
            executor.shutdown();
        }
        long timeCost = System.currentTimeMillis() - startTime;
        System.out.println("Mode: SynchroniousCall");
        System.out.println("Thread num: " + threadNum);
        System.out.println("Total time cost: " + ((double) timeCost) / 1000.0);
        System.out.println("Tps : " + requestNum / (((double) timeCost) / 1000.0));
        rpcClient.stop();
    }
}
