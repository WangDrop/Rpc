package com.wc.rpc.test.app;

import com.wc.rpc.test.client.HelloPersonService;
import com.wc.rpc.test.client.HelloService;
import com.wc.rpc.client.RpcClient;
import com.wc.rpc.test.client.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.instantiator.perc.PercSerializationInstantiator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

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
    public void helloPersonService(){
        HelloPersonService helloPersonService = rpcClient.create(HelloPersonService.class);
        int num = 5;
        List<Person> persons = helloPersonService.getTestPerson("wangcheng", 5);
        for(Person p : persons){
            System.out.println(p.getFirstName() + " " + p.getLastName());
        }
    }


}
