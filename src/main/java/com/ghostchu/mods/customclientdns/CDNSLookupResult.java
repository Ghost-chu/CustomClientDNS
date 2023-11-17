package com.ghostchu.mods.customclientdns;

public class CDNSLookupResult {
    private String host;
    private int port;

    public CDNSLookupResult(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
