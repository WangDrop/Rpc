# Rpc
- An Simple RPC Framework based on Netty, ZooKeeper and Spring

# Fetures
- Synchronous and Non-blocking asynchronous call support
- High availability, load balance and failover
- Use long-Alive connection
- Simple code, and also simple to Use

# Usage
1.Define a Interface
```java
public interface HelloService {
    String hello(String name);
    String hello(Person person);
}
```

2.Mark the impl with annotation @RpcService
```java
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
```
3.Run the ServerBootStrap with Zookeeper

4.Run a client
```java
public void helloTest1() {
    HelloService helloService = rpcClient.create(HelloService.class);
    String res = helloService.hello("Wangcheng");
    System.out.println(res);
}
```
