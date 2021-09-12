package com.javacgo.remote.qt.server.handler;


import com.javacgo.remote.qt.common.entity.KeskHost;
import com.javacgo.remote.qt.common.protocol.BigPack;
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

    //通过channelID 找到 设备 ID
    private ConcurrentMap<ChannelId, String> channelId_DeviceID_Map = new ConcurrentHashMap<>();
    //通过设备ID 找到 设备信息
    private ConcurrentMap<String, KeskHost> deviceId_Host_Map = new ConcurrentHashMap<>();
    //在线人数
    private  int countPeople = 0 ;

    public void addUser(String deviceID, KeskHost host) {
        if (deviceId_Host_Map.containsKey(deviceID)) {
            logger.error("设备已经登录过");
            return;
        }
        deviceId_Host_Map.put(deviceID, host);
        countPeople++ ;
        channelId_DeviceID_Map.put(host.getChannel().id(), deviceID);
        if(host.isActive()){
            logger.info("[add][主控连接 {} 登录]", deviceID);
        }else{
            logger.info("[add][被控连接 {} 登录]", deviceID);
        }
        logger.info("[peopleCount] 在线人数 ：[{}]", countPeople);
    }

    public void connect(String resourceId, String targetId) {
        KeskHost resourceHost = deviceId_Host_Map.get(resourceId);
        KeskHost targetHost = deviceId_Host_Map.get(targetId);
        if (resourceHost == null || targetHost == null) {
            logger.error("关联关系不存在");
            return;
        }
        resourceHost.addRelations(targetHost);
        targetHost.addRelations(resourceHost);
    }
    public void disconnect(String resourceId, String targetId){
        KeskHost resourceHost = deviceId_Host_Map.get(resourceId);
        KeskHost targetHost = deviceId_Host_Map.get(targetId);
        if (resourceHost == null || targetHost == null) {
            logger.error("关联关系不存在");
            return;
        }
        resourceHost.removeRelations(targetHost);
        targetHost.removeRelations(resourceHost);

    }

    public void remove(Channel channel) {
        logger.info("====================remove======================");
        countPeople--;
        //断开的主机设备ID
        String deviceId = channelId_DeviceID_Map.get(channel.id());
        channelId_DeviceID_Map.remove(channel.id());
        //断开的主机
        KeskHost host = deviceId_Host_Map.get(deviceId);
        deviceId_Host_Map.remove(deviceId);

        deviceId_Host_Map.forEach((key, value) -> {
            value.removeRelations(host);
        });

        logger.info("=====[remove][一个连接({})离开]==============", deviceId);
    }


    public void sendImage(String resourceId, BigPack.Exchange msg) {
        KeskHost resourceHost = deviceId_Host_Map.get(resourceId);
        Set<KeskHost> hostSet = resourceHost.getRelations();
        for (KeskHost host : hostSet) {
            if(host.getCurrentScreenDeviceId().equals(resourceId)){
                host.getChannel().writeAndFlush(msg);
            }
        }
    }

    public void dealQuestHost(String resourceId, String targetUser) {
        // 获得用户对应的 Channel
        KeskHost targetHost = deviceId_Host_Map.get(targetUser);
        KeskHost resourceHost = deviceId_Host_Map.get(resourceId);
        BigPack.Exchange.Builder exB = BigPack.Exchange.newBuilder();
        exB.setDataType(BigPack.Exchange.DataType.TypeResponseHost);
        exB.setResourceId(resourceId);
        exB.setTargetId(targetUser);
        if (targetHost == null) {
            logger.error("[ifTargetUserExistDeal]不存在]{}", targetUser);
            exB.setResponseHost(BigPack.ScResponseHost.newBuilder().setIsExist(false));
        }else{
            exB.setResponseHost(BigPack.ScResponseHost.newBuilder().setIsExist(true));
        }
        resourceHost.getChannel().writeAndFlush(exB.build());

    }

    public void send(String targetUser, BigPack.Exchange msg) {
        // 获得用户对应的 Channel
        KeskHost host = deviceId_Host_Map.get(targetUser);
        if (host == null) {
            logger.error("[send][targetHost不存在]{}", targetUser);
            return;
        }
        Channel channel = host.getChannel();
        if (channel == null) {
            logger.error("[send][连接不存在]");
            return;
        }
        if (!channel.isActive()) {
            logger.error("[send][连接({})未激活]", channel.id());
            return;
        }
        // 发送消息
        channel.writeAndFlush(msg);
    }

    public void setNeedSendId(String sourceId,String targetId) {
        KeskHost host = deviceId_Host_Map.get(sourceId);
        host.setCurrentScreenDeviceId(targetId);
    }
}
