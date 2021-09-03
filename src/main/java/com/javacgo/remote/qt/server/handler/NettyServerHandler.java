package com.javacgo.remote.qt.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.*;

@Component
@ChannelHandler.Sharable
public class NettyServerHandler  extends ChannelInboundHandlerAdapter {
    private Logger logger = getLogger(getClass());

    @Autowired
    private NettyChannelManager channelManager;


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 从管理器中添加
        //channelManager.add(ctx.channel());
    }
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        // 从管理器中移除
        //channelManager.remove(ctx.channel());
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info(ctx.channel().remoteAddress() + " 网络连接上 " + ctx.channel().id().asLongText());
    }

    //断开连接, 将xx客户离开信息推送给当前在线的客户
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelManager.remove(channel);
        ctx.close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("[exceptionCaught][连接({}) 发生异常]", ctx.channel().id(), cause);
        //关闭通道
        if (cause instanceof CorruptedFrameException) {
            //logger.warn(cause.getMessage());
            return;
        }
        // 断开连接
        ctx.channel().close();
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            //将  evt 向下转型 IdleStateEvent
            IdleStateEvent event = (IdleStateEvent) evt;
            String eventType = null;
            switch (event.state()) {
                case READER_IDLE:
                    eventType = "读空闲";
                    break;
                case WRITER_IDLE:
                    eventType = "写空闲";
                    break;
                case ALL_IDLE:
                    eventType = "读写空闲";
                    break;
            }
            System.out.println(ctx.channel().remoteAddress() + "--超时时间--" + eventType);
            System.out.println("服务器做相应处理..");
            //如果发生空闲，我们关闭通道
            // ctx.channel().close();
        }
    }

}
