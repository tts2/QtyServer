package com.javacgo.remote.qt.messgehandler;

import com.javacgo.remote.qt.common.entity.KeskHost;
import com.javacgo.remote.qt.common.protocol.BigPack;
import com.javacgo.remote.qt.dispatcher.MessageHandler;
import com.javacgo.remote.qt.server.handler.NettyChannelManager;
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
            //加入设备
            if (1 == wmhostInfo.getActiveOrpassive() || 0 == wmhostInfo.getActiveOrpassive()) {
                KeskHost host = new KeskHost();
                host.setDeviceID(resourceId).
                        setMac(wmhostInfo.getMac()).
                        setPcName(wmhostInfo.getPcName()).
                        setActiveOrPassive(wmhostInfo.getActiveOrpassive()).
                        setChannel(ctx.channel());
                nettyChnnelManager.addUser(resourceId, host);
            }
        }
        switch (dataType) {
            case TypeRequestAuth:
                if (nettyChnnelManager.ifTargetUserExistDeal(resourceId, targetId)) {
                    break;
                }
                logger.info("{} 请求 {} 认证", resourceId, targetId);
                nettyChnnelManager.send(targetId, msg);
                break;
            case TypeResponseAuth:
                if (1 == msg.getResponseAuth().getSuccess()) {
                    //认证成功，加入进来,隶属于关系
                    nettyChnnelManager.connet(resourceId, targetId);
                }
                nettyChnnelManager.send(targetId, msg);
                break;
            case TypeRequestDesk:
                nettyChnnelManager.setNeedSendId(resourceId, targetId);
                if (1 == msg.getCommandDesk().getOpenOrClose()) {
                    nettyChnnelManager.send(targetId, msg);
                }
                break;
            case TypeImageParameters:
            case TypeImage:
                nettyChnnelManager.sendImage(resourceId, msg);
                break;
            case TypeImageReceived:
                nettyChnnelManager.send(targetId, msg);
                break;
        }
    }

    @Override
    public String getType() {
        // return HeartbeatRequest.TYPE;
        // return BigPack.Exchange.DataType.TypeHost.toString();
        return "one";
    }

}