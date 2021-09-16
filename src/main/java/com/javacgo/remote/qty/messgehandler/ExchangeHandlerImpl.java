package com.javacgo.remote.qty.messgehandler;

import com.javacgo.remote.qty.common.protocol.BigPack;
import com.javacgo.remote.qty.dispatcher.MessageHandler;
import com.javacgo.remote.qty.server.handler.NettyChannelManager;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class ExchangeHandlerImpl implements MessageHandler<BigPack.Exchange> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    NettyChannelManager nettyChannelManager;

    @Override
    public void execute(ChannelHandlerContext ctx, BigPack.Exchange msg) {
        //根据dataType 来显示不同的信息
        BigPack.Exchange.DataType dataType = msg.getDataType();
        if (dataType == BigPack.Exchange.DataType.TypeRegisterHost) {
            BigPack.CsHostInfo csHostInfo = msg.getHostInfo();
            nettyChannelManager.dealHostInfo(csHostInfo,ctx.channel());
            return ;
        }
        //源ID
        String resourceId = msg.getResourceId();
        //目标ID
        String targetId = msg.getTargetId();
        if (ObjectUtils.isEmpty(dataType) || ObjectUtils.isEmpty(resourceId)) {
            logger.error("协议类型为空 或者 resourceId没有携带,不处理！");
            return;
        }
        switch (dataType) {
            case TypeQueryHost:
                nettyChannelManager.dealQuestHost(resourceId, targetId);
                break;
            case TypeRequestAuth:
                logger.info("{} 请求 {} 认证", resourceId, targetId);
                nettyChannelManager.send(targetId, msg);
                break;
            case TypeResponseAuth:
                if (msg.getResponseAuth().getSuccess()) {
                    //认证成功，加入进来,隶属于关系
                    nettyChannelManager.connect(resourceId, targetId);
                }
                nettyChannelManager.send(targetId, msg);
                break;
            case TypeRequestLeaveLook:
                nettyChannelManager.disconnect(resourceId, targetId);
                break;
            case TypeRequestDesk:
                nettyChannelManager.setNeedSendId(resourceId, targetId);
                break;
            case TypeImageParameters:
            case TypeImage:
                nettyChannelManager.sendImage(resourceId, msg);
                break;
            case TypeMouseMove:
            case TypeMouseKeys:
            case TypeWheelEvent:
            case TypeKeyBoard:
            case TypeImageReceived:
                nettyChannelManager.send(targetId, msg);
                break;

        }
    }

    @Override
    public String getType() {
        // return HeartbeatRequest.TYPE;
        // return BigPack.Exchange.DataType.TypeHost.toString();
        return "BigPack.Exchange";
    }

}