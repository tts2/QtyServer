package com.javacgo.remote.qty.server.handler;

import com.javacgo.remote.qty.codec.ProtobufFixed32FrameDecoderRedefine;
import com.javacgo.remote.qty.codec.ProtobufFixed32LengthFieldPrependerRedefine;
import com.javacgo.remote.qty.common.protocol.BigPack;
import com.javacgo.remote.qty.dispatcher.MessageDispatcher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class NettyServerHandlerInitializer extends ChannelInitializer<Channel> {
    /**
     * 心跳超时时间
     */
    private static final Integer READ_TIMEOUT_SECONDS = 3 * 60;

    @Autowired
    private MessageDispatcher messageDispatcher;
    @Autowired
    private NettyServerHandler nettyServerHandler;

    @Override
    protected void initChannel(Channel channel) throws Exception {
        // <1> 获得 Channel 对应的 ChannelPipeline
        ChannelPipeline channelPipeline = channel.pipeline();
        // <2> 添加一堆 NettyServerHandler 到 ChannelPipeline 中
        channelPipeline
                // 空闲检测
                //.addLast(new ReadTimeoutHandler(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .addLast(new IdleStateHandler(7000, 7000, 15, TimeUnit.SECONDS))
                //入站
                .addLast(new ProtobufFixed32FrameDecoderRedefine())
                .addLast("ExchangeProtobufferDecoder", new ProtobufDecoder(BigPack.Exchange.getDefaultInstance()))
                //出站
                .addLast(new ProtobufFixed32LengthFieldPrependerRedefine())
                .addLast(new ProtobufEncoder())
                // 消息分发器
                .addLast(messageDispatcher)
                // 服务端处理器
                .addLast(nettyServerHandler);
    }
}
