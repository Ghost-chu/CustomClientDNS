package com.ghostchu.mods.customclientdns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPAddressMatcher {
    private static final String IPV4_REGEX = "^(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
    private static final String IPV6_STD_REGEX = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
    private static final String IPV6_COMPRESS_REGEX = "^([0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4})*)?::([0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4})*)?$";

    public static boolean isIPv4Address(String ipAddress) {
        Pattern pattern = Pattern.compile(IPV4_REGEX);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    public static boolean isIPv6Address(String ipAddress) {
        Pattern pattern = Pattern.compile(IPV6_STD_REGEX);
        Matcher matcher = pattern.matcher(ipAddress);
        if (matcher.matches()) {
            return true;
        }
        pattern = Pattern.compile(IPV6_COMPRESS_REGEX);
        matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    public static boolean isIPAddress(String ip){
        return isIPv4Address(ip) || isIPv6Address(ip);
    }
}