package com.ashyadav.FYPBT_app;

public class BTInfoID {

    private String deviceName, deviceHardwareAddress;

    public BTInfoID(){}

    public BTInfoID(String deviceName, String deviceHardwareAddress){
        this.deviceName = deviceName;
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

    public String getDeviceName(){return deviceName;}

    public String getDeviceHardwareAddress(){return deviceHardwareAddress;}

}
