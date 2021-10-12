package com.javacgo.remote.qty.common.entity;

import io.netty.channel.Channel;
import java.util.HashSet;
import java.util.Set;

public class QtyHost {
    //设备ID
    private String deviceID;
    //channel传输
    private Channel channel;
    //cpu ID
    private String cpuID;
    //mac地址
    private String macAddress;
    //被动主动
    private int activeOrPassive;
    //现在发送屏幕设备（减少带宽，暂时不需要）
    private String currentScreenDeviceId ;
    //？共享资源是否锁
    private Set<QtyHost> relations = new HashSet();
    //是否主动设备
    public boolean isActive() {
        if (1 == activeOrPassive) {
            return true;
        } else {
            return false;
        }
    }
    public Set<QtyHost>  getRelations(){
        return this.relations;
    }
    public void addRelations(QtyHost host) {
        this.relations.add(host);
    }

    public boolean removeRelations(QtyHost host) {
        if(this.relations.contains(host)){
            this.relations.remove(host);
            return true ;
        }else{
            return false;
        }

    }

    public QtyHost setDeviceID(String id) {
        this.deviceID = id;
        return this;
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public Channel getChannel() {
        return this.channel;
    }
    public QtyHost setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public QtyHost setCpuID(String cpuID) {
        this.cpuID = cpuID;
        return this;
    }

    public QtyHost setMac(String mac) {
        this.macAddress = mac;
        return this;
    }

    public QtyHost setActiveOrPassive(int aOrP) {
        this.activeOrPassive = aOrP;
        return this;
    }


    public String getCurrentScreenDeviceId() {
        return currentScreenDeviceId;
    }


    public void setCurrentScreenDeviceId(String currentScreenDeviceId) {
        this.currentScreenDeviceId = currentScreenDeviceId;
    }

}
