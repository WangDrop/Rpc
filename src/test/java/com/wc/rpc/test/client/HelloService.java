package com.wc.rpc.test.client;

/**
 * the service interface
 * Created by wc
 */

public interface HelloService {
    String hello(String name);
    String hello(Person person);
}
