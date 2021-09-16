/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javacgo.remote.qty.common.entity;

import io.netty.channel.Channel;
import java.util.HashSet;
import java.util.Set;

public class KeskHost {
    //设备ID
    private String deviceID;
    //channel传输
    private Channel channel;
    //pc名字
    private String cpuID;
    //mac地址
    private String macAddress;
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
    public Set<KeskHost>  getRelations(){
        return this.relations;
    }
    public void addRelations(KeskHost host) {
        this.relations.add(host);
    }

    public boolean removeRelations(KeskHost host) {
        if(this.relations.contains(host)){
            this.relations.remove(host);
            return true ;
        }else{
            return false;
        }

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
    public KeskHost setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public KeskHost setCpuID(String cpuID) {
        this.cpuID = cpuID;
        return this;
    }

    public KeskHost setMac(String mac) {
        this.macAddress = mac;
        return this;
    }

    public KeskHost setActiveOrPassive(int ap) {
        this.activeOrPassive = ap;
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
