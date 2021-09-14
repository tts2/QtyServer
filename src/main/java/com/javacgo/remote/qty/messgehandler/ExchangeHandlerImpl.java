package com.javacgo.remote.qty.messgehandler;

import com.javacgo.remote.qty.common.entity.KeskHost;
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
    NettyChannelManager nettyChnnelManager;

    @Override
    public void execute(ChannelHandlerContext ctx, BigPack.Exchange msg) {
        //根据dataType 来显示不同的信息
        BigPack.Exchange.DataType dataType = msg.getDataType();
        //源ID
        String resourceId = msg.getResourceId();
        //目标ID
        String targetId = msg.getTargetId();
        if (ObjectUtils.isEmpty(dataType) || ObjectUtils.isEmpty(resourceId)) {
            logger.error("协议类型为空 或者 resourceId没有携带,不处理！");
            return;
        }
        if (dataType == BigPack.Exchange.DataType.TypeHost) {
            BigPack.WMHostInfo wmhostInfo = msg.getHostInfo();
            nettyChnnelManager.dealHostInfo(wmhostInfo, resourceId, ctx.channel());
        }
        switch (dataType) {
            case TypeQueryHost:
                nettyChnnelManager.dealQuestHost(resourceId, targetId);
                break;
            case TypeRequestAuth:
                logger.info("{} 请求 {} 认证", resourceId, targetId);
                nettyChnnelManager.send(targetId, msg);
                break;
            case TypeResponseAuth:
                if (msg.getResponseAuth().getSuccess()) {
                    //认证成功，加入进来,隶属于关系
                    nettyChnnelManager.connect(resourceId, targetId);
                }
                nettyChnnelManager.send(targetId, msg);
                break;
            case TypeRequestLeaveLook:
                nettyChnnelManager.disconnect(resourceId, targetId);
                break;
            case TypeRequestDesk:
                nettyChnnelManager.setNeedSendId(resourceId, targetId);
                break;
            case TypeImageParameters:
            case TypeImage:
                nettyChnnelManager.sendImage(resourceId, msg);
                break;
            case TypeMouseMove:
            case TypeMouseKeys:
            case TypeWheelEvent:
            case TypeKeyBoard:
            case TypeImageReceived:
                nettyChnnelManager.send(targetId, msg);
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