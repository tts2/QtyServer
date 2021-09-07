/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javacgo.remote.qt.common.entity;

import io.netty.channel.Channel;
import java.util.HashSet;
import java.util.Set;

public class KeskHost {
    //设备ID
    private String deviceID;
    //channel传输
    private Channel channel;
    //pc名字
    private String pcName;
    //mac地址
    private String mac;
    //被动主动
    private int activeOrPassive;
    
    private String currentScreenDeviceId ;
    private Set<KeskHost> relations = new HashSet();
    public boolean isActive() {
        if (1 == activeOrPassive) {
            return true;
        } else {
            return false;
        }
    }
    public Set getRelations() {
        return relations;
    }

    public void addRelations(KeskHost host) {
        this.relations.add(host);
    }

    public void removeRelations(KeskHost host) {
        this.relations.remove(host);
    }

    public KeskHost setDeviceID(String id) {
        this.deviceID = id;
        return this;
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public KeskHost setPcName(String pcName) {
        this.pcName = pcName;
        return this;
    }

    public KeskHost setMac(String mac) {
        this.mac = this.mac;
        return this;
    }

    public KeskHost setActiveOrPassive(int ap) {
        this.activeOrPassive = ap;
        return this;
    }

    public KeskHost setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    /**
     * @return the currentScreenDeviceId
     */
    public String getCurrentScreenDeviceId() {
        return currentScreenDeviceId;
    }

    /**
     * @param currentScreenDeviceId the currentScreenDeviceId to set
     */
    public void setCurrentScreenDeviceId(String currentScreenDeviceId) {
        this.currentScreenDeviceId = currentScreenDeviceId;
    }

}
