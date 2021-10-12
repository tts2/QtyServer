package com.javacgo.remote.qty.server.handler;


import com.javacgo.remote.qty.common.entity.QtyHost;
import com.javacgo.remote.qty.common.protocol.BigPack;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
public class NettyChannelManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    //
    //通过channelID 找到 设备 ID
    private ConcurrentMap<ChannelId, String> channelId_DeviceID_Map = new ConcurrentHashMap<>();
    //通过设备ID 找到 设备信息
    private ConcurrentMap<String, QtyHost> deviceId_Host_Map = new ConcurrentHashMap<>();
    //在线人数
    private int countPeople = 0;

    public void addUser(String deviceID, QtyHost host) {
        boolean success = false;
        if (deviceId_Host_Map.containsKey(deviceID)) {
            logger.warn("设备已经登录过");
        } else {
            success = true;
            deviceId_Host_Map.put(deviceID, host);
            channelId_DeviceID_Map.put(host.getChannel().id(), deviceID);
            if (host.isActive()) {
                logger.info("[主控连接 {} 登录]", deviceID);
            } else {
                countPeople++;
                logger.info("[被控连接 {} 登录]", deviceID);
            }
        }
        BigPack.Exchange.Builder exB = BigPack.Exchange.newBuilder();
        exB.setDataType(BigPack.Exchange.DataType.TypeReplyRegisterDetails);
        exB.setReplyInfo(BigPack.ScReplyInfo.newBuilder().setSuccess(success).setRegisterId(deviceID));
        host.getChannel().writeAndFlush(exB.build());

        logger.info("[被控设备] 在线人数 ：[{}]", countPeople);
    }

    public void timeoutHandling(Channel channel, String eventType) {
        logger.warn(channel.remoteAddress() + "--超时时间--" + eventType);
        channel.close();
    }

    public void connect(String resourceId, String targetId) {
        QtyHost resourceHost = deviceId_Host_Map.get(resourceId);
        QtyHost targetHost = deviceId_Host_Map.get(targetId);
        if (resourceHost == null || targetHost == null) {
            logger.error("关联关系不存在");
            return;
        }
        resourceHost.addRelations(targetHost);
        targetHost.addRelations(resourceHost);
        Set<QtyHost> relations = resourceHost.getRelations();
        BigPack.Exchange.Builder exB = BigPack.Exchange.newBuilder();
        exB.setDataType(BigPack.Exchange.DataType.TypeRequestDesk);
        if (relations.size() == 1) {
            exB.setRequestDesk(BigPack.CsDeskRequest.newBuilder().setOperation(1));
        } else {
            exB.setRequestDesk(BigPack.CsDeskRequest.newBuilder().setOperation(2));
        }
        resourceHost.getChannel().writeAndFlush(exB.build());

    }

    public void disconnect(String resourceId, String targetId) {
        QtyHost resourceHost = deviceId_Host_Map.get(resourceId);
        QtyHost targetHost = deviceId_Host_Map.get(targetId);
        if (resourceHost == null || targetHost == null) {
            logger.error("关联关系不存在");
            return;
        }
        resourceHost.removeRelations(targetHost);
        targetHost.removeRelations(resourceHost);

    }

    public void remove(Channel channel) {
        //断开的主机设备ID
        String deviceId = channelId_DeviceID_Map.get(channel.id());
        channelId_DeviceID_Map.remove(channel.id());
        //断开的主机
        QtyHost host = deviceId_Host_Map.get(deviceId);
        deviceId_Host_Map.remove(deviceId);

        if (!host.isActive()) {
            countPeople--;
        }

        deviceId_Host_Map.forEach((key, value) -> {
            value.removeRelations(host);
            //通知关闭截图
            if (!value.isActive()) {
                Set<QtyHost> relations = value.getRelations();
                if (relations.size() == 0) {
                    BigPack.Exchange.Builder exB = BigPack.Exchange.newBuilder();
                    exB.setDataType(BigPack.Exchange.DataType.TypeRequestDesk);
                    exB.setRequestDesk(BigPack.CsDeskRequest.newBuilder().setOperation(0));
                    value.getChannel().writeAndFlush(exB.build());
                }
            }
        });
        logger.info("[remove][一个连接({})离开]", deviceId);
        logger.info("[被控设备] 在线人数 ：[{}]", countPeople);
    }


    public void sendImage(String resourceId, BigPack.Exchange msg) {
        //得到源设备
        QtyHost resourceHost = deviceId_Host_Map.get(resourceId);
        //接受了多少
        int sendNum = msg.getImage().getSendNum();
        BigPack.Exchange.Builder exB = BigPack.Exchange.newBuilder();
        exB.setDataType(BigPack.Exchange.DataType.TypeImageReceived);
        exB.setImageReceived(BigPack.CsImageReceived.newBuilder().setTileNum(sendNum));
        resourceHost.getChannel().writeAndFlush(exB.build());
        //发送图片
        Set<QtyHost> hostSet = resourceHost.getRelations();
        for (QtyHost host : hostSet) {
            // if (host.getCurrentScreenDeviceId().equals(resourceId)) {
            host.getChannel().writeAndFlush(msg);
            // }
        }
    }

    public void dealQuestHost(String resourceId, String targetUser) {
        // 获得用户对应的 Channel
        QtyHost targetHost = deviceId_Host_Map.get(targetUser);
        QtyHost resourceHost = deviceId_Host_Map.get(resourceId);
        BigPack.Exchange.Builder exB = BigPack.Exchange.newBuilder();
        exB.setDataType(BigPack.Exchange.DataType.TypeResponseHost);
        exB.setResourceId(resourceId);
        exB.setTargetId(targetUser);
        if (targetHost == null) {
            logger.warn("[查询主机]不存在{}", targetUser);
            exB.setResponseHost(BigPack.ScResponseHost.newBuilder().setIsExist(false));
        } else {
            exB.setResponseHost(BigPack.ScResponseHost.newBuilder().setIsExist(true));
        }
        resourceHost.getChannel().writeAndFlush(exB.build());
    }

    public void send(String targetUser, BigPack.Exchange msg) {
        // 获得用户对应的 Channel
        QtyHost host = deviceId_Host_Map.get(targetUser);
        if (host == null) {
            logger.error("[targetHost不存在]{}", targetUser);
            return;
        }
        Channel channel = host.getChannel();
        if (channel == null) {
            logger.error("[channel连接不存在]");
            return;
        }
        if (!channel.isActive()) {
            logger.error("[连接({})未激活]", channel.id());
            return;
        }
        // 发送消息
        channel.writeAndFlush(msg);
    }

    public void setNeedSendId(String sourceId, String targetId) {
        QtyHost host = deviceId_Host_Map.get(sourceId);
        host.setCurrentScreenDeviceId(targetId);
    }

    //注册主机信息
    public void dealHostInfo(BigPack.CsHostInfo csHostInfo, Channel channel) {
        String deviceId;
        //加入设备
        QtyHost host = new QtyHost();
        if (0 == csHostInfo.getActiveOrpassive()) {
            //被控用CPUID作为设备ID
            deviceId = csHostInfo.getCpuId().substring(0, 9);
            host.setDeviceID(deviceId);
        } else if (1 == csHostInfo.getActiveOrpassive()) {
            //主控用netty自带channel短ID
            deviceId = channel.id().asShortText();
            host.setDeviceID(deviceId);
        } else {
            return;
        }
        host.setMac(csHostInfo.getMac()).
                setCpuID(csHostInfo.getCpuId()).
                setActiveOrPassive(csHostInfo.getActiveOrpassive()).
                setChannel(channel);
        addUser(deviceId, host);
    }

}
