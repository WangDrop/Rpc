package com.wc.rpc.client;

import com.wc.rpc.protocol.RpcDecoder;
import com.wc.rpc.protocol.RpcEncoder;
import com.wc.rpc.protocol.RpcRequest;
import com.wc.rpc.protocol.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created by 12083 on 2016/8/23.
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new RpcEncoder(RpcRequest.class))
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0)) //半包解码器
                .addLast(new RpcDecoder(RpcResponse.class))
                .addLast(new RpcClientHandler());
    }
}
