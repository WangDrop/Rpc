package com.wc.rpc.test.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by 12083 on 2016/8/24.
 */
public class RpcBootstrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("server-spring.xml");
    }
}
