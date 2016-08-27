package com.wc.rpc.test.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by 12083 on 2016/8/27.
 */
// another server node for test the load balanec
public class RpcBootStrap2 {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("server-spring-2.xml");
    }
}

