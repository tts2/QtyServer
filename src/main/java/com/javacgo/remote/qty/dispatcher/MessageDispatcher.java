package com.javacgo.remote.qty.dispatcher;


import com.javacgo.remote.qty.common.protocol.BigPack;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;


import static java.util.concurrent.Executors.*;

@ChannelHandler.Sharable
public class MessageDispatcher extends SimpleChannelInboundHandler<BigPack.Exchange> {
    @Autowired
    private MessageHandlerContainer messageHandlerContainer;
    private final ExecutorService executor = newFixedThreadPool(200);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BigPack.Exchange exchange) throws Exception {
        // <3.1> 获得 type 对应的 MessageHandler 处理器
         MessageHandler messageHandler = messageHandlerContainer.getMessageHandler(BigPack.Exchange.class.toGenericString());
        // 获得  MessageHandler 处理器的消息类
        //Class<? extends Message> messageClass = MessageHandlerContainer.getMessageClass(messageHandler);
        // <3.2> 解析消息
        //Message message = JSON.parseObject(invocation.getMessage(), messageClass);
        // <3.3> 执行逻辑
        executor.submit(() -> {
            // noinspection unchecked
            messageHandler.execute(ctx, exchange);
        });
    }
}
