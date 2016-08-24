package com.wc.rpc.test.client;

import java.util.List;

/**
 * Created by 12083 on 2016/8/24.
 */
public interface HelloPersonService {
    List<Person> getTestPerson(String name, int num);
}
