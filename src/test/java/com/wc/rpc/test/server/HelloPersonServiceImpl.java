package com.wc.rpc.test.server;

import com.wc.rpc.test.client.HelloPersonService;
import com.wc.rpc.test.client.Person;
import com.wc.rpc.server.RpcService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 12083 on 2016/8/24.
 */
@RpcService(HelloPersonService.class)
public class HelloPersonServiceImpl implements HelloPersonService {
    @Override
    public List<Person> getTestPerson(String name, int num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i)
            persons.add(new Person(name, "" + i));
        return persons;
    }
}
