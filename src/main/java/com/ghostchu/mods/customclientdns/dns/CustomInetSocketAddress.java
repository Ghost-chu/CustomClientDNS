package com.ghostchu.mods.customclientdns.dns;

import java.net.InetSocketAddress;

public class CustomInetSocketAddress extends InetSocketAddress {

    private final String realAddress;

    public CustomInetSocketAddress(String realAddress, String hostname, int port) {
        super(hostname, port);
        this.realAddress = realAddress;
    }

    public String getRealAddress() {
        return realAddress;
    }
}
