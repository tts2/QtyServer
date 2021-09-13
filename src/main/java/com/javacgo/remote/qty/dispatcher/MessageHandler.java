package com.javacgo.remote.qty.dispatcher;


import io.netty.channel.ChannelHandlerContext;

public interface MessageHandler<T> {
    /**
     * 执行处理消息

     */
    void execute(ChannelHandlerContext ctx, T message);

    /**
     * @return 消息类型，即每个 Message 实现类上的 TYPE 静态字段
     */
    String getType();
}
