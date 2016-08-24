package com.wc.rpc.test.app;

import com.wc.rpc.client.RpcClient;
import com.wc.rpc.test.client.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by 12083 on 2016/8/24.
 */
public class BenchMark {
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("client-spring.xml");
        final RpcClient rpcClient = ctx.getBean(RpcClient.class);
        int threadNum = 100;
        final int requestNum = 100000;
        final CountDownLatch countDownLatch = new CountDownLatch(requestNum);
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < requestNum; ++i) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    final HelloService helloService = rpcClient.create(HelloService.class);
                    System.out.println(helloService.hello("wangcheng! "));
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long timeCost = System.currentTimeMillis() - startTime;
        System.out.println("Mode: SynchroniousCall");
        System.out.println("Thread num: " + threadNum);
        System.out.println("Total time cost: " + ((double) timeCost) / 1000.0);
        System.out.println("Tps : " + requestNum / (((double) timeCost) / 1000.0));
        executor.shutdown();
        rpcClient.stop();
    }
}
