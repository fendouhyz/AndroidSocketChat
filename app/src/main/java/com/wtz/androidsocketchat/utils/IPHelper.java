package com.wtz.androidsocketchat.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IPHelper {
    private final static String TAG = IPHelper.class.getName();

    public static String getLocalIPAddress() {
        String ipAddress = null;
        try {
            for (Enumeration mEnumeration = NetworkInterface.getNetworkInterfaces(); mEnumeration
                    .hasMoreElements();) {
                NetworkInterface netInterface = (NetworkInterface) mEnumeration.nextElement();
                if (netInterface.getName().toLowerCase().equals("eth0")
                        || netInterface.getName().toLowerCase().equals("wlan0")) {
                    for (Enumeration enumIPAddr = netInterface.getInetAddresses(); enumIPAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = (InetAddress) enumIPAddr.nextElement();
                        // 如果不是回环地址
                        if (!inetAddress.isLoopbackAddress()) {
                            ipAddress = inetAddress.getHostAddress().toString();
                            if (!ipAddress.contains("::")) {// 如果不是ipV6的地址
                                return ipAddress;
                            }
                        }
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
