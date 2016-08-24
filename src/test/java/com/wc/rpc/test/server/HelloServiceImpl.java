package com.wc.rpc.test.server;

import com.wc.rpc.test.client.HelloService;
import com.wc.rpc.test.client.Person;
import com.wc.rpc.server.RpcService;

/**
 * Created by 12083 on 2016/8/24.
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello, " + person.getFirstName() + person.getLastName();
    }
}
