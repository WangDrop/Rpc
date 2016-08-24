package com.wc.rpc.test.app;

import com.wc.rpc.client.RPCFuture;
import com.wc.rpc.client.proxy.IAsyncObjectProxy;
import com.wc.rpc.test.client.HelloPersonService;
import com.wc.rpc.test.client.HelloService;
import com.wc.rpc.client.RpcClient;
import com.wc.rpc.test.client.Person;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.ExtendedBeanInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.instantiator.perc.PercSerializationInstantiator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 12083 on 2016/8/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class HelloServiceTest {
    @Autowired
    private RpcClient rpcClient; //for autowire

    @Test
    public void helloTest1() {
        HelloService helloService = rpcClient.create(HelloService.class);
        String res = helloService.hello("Wangcheng");
        System.out.println(res);
    }

    @Test
    public void helloTest2() {
        HelloService helloService = rpcClient.create(HelloService.class);
        String res = helloService.hello(new Person("wang", "cheng"));
        System.out.println(res);
    }

    @Test
    public void helloPersonService() {
        HelloPersonService helloPersonService = rpcClient.create(HelloPersonService.class);
        int num = 5;
        List<Person> persons = helloPersonService.getTestPerson("wangcheng", 5);
        for (Person p : persons) {
            System.out.println(p.getFirstName() + " " + p.getLastName());
        }
    }

    @Test
    public void helloFutureTest1() throws ExecutionException, InterruptedException {
        IAsyncObjectProxy helloService = rpcClient.createAsync(HelloService.class);
        RPCFuture result = helloService.call("hello", "world"); // funcname and parameter
        System.out.println(result.get());
    }

    @Test
    public void helloFutureTest2() throws ExecutionException, InterruptedException {
        IAsyncObjectProxy helloService = rpcClient.createAsync(HelloService.class);
        RPCFuture result = helloService.call("hello", new Person("wang", "cheng"));
        System.out.println(result.get());
    }

    @Test
    public void helloFuturePersonTest3() throws ExecutionException, InterruptedException {
        IAsyncObjectProxy helloPersonService = rpcClient.createAsync(HelloPersonService.class);
        RPCFuture result = helloPersonService.call("getTestPerson", "wangcheng", 10);
        List<Person> res = (List<Person>) result.get();
        for (Person p : res) {
            System.out.println(p);
        }
    }

    @Test
    public void helloTestWithMultiThread() throws ExecutionException, InterruptedException {
        int threadNum = 8;
        int loopCount = 80000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        final CountDownLatch countDownLatch = new CountDownLatch(loopCount);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < loopCount; ++i) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    HelloService helloService = rpcClient.create(HelloService.class);
                    String result = helloService.hello("world");
                    System.out.println(result);
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        long costTime = System.currentTimeMillis() - startTime;
        System.out.println("Total time cost is " + costTime + " ms");
        System.out.println("Thread count: " + threadNum);
        System.out.println("Loop count " + loopCount);
        System.out.println("Tps: " + (double) loopCount / ((double) costTime / 1000));
        executorService.shutdown();
    }
}
