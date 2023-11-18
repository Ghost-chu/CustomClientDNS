package com.ghostchu.mods.customclientdns;

import org.apache.commons.validator.routines.InetAddressValidator;

public class IPAddressMatcher {
    public static boolean isIPAddress(String ip){
        return InetAddressValidator.getInstance().isValid(ip);
    }
}