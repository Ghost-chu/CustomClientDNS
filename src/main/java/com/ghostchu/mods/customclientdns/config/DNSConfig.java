package com.ghostchu.mods.customclientdns.config;

import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class DNSConfig {
    private boolean useLocalDnsServer = true;
    private List<String> customizedDNSResolver = new ArrayList<>(List.of(
            "https://dns.alidns.com/dns-query",
            "https://dot.pub/dns-query",
            "223.5.5.5",
            "119.29.29.29",
            "114.114.114.114",
            "1.1.1.1",
            "8.8.8.8"));

    public DNSConfig(YamlConfiguration configuration) {
        this.useLocalDnsServer = configuration.getBoolean("use-local-dns-server");
        this.customizedDNSResolver = configuration.getStringList("customized-dns-resolvers");
    }

    public DNSConfig() {
    }

    public List<String> getCustomizedDNSResolver() {
        return customizedDNSResolver;
    }

    public boolean isUseLocalDnsServer() {
        return useLocalDnsServer;
    }

    public YamlConfiguration createConfiguration() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("use-local-dns-server", this.useLocalDnsServer);
        yamlConfiguration.setComments("use-local-dns-server",
                List.of("This configuration item allows you to configure whether or not to use the local DNS settings of the user's computer system.",
                        "When set to false, the system's local DNS is ignored and only the DNS servers configured by the Mod are used.")
        );
        yamlConfiguration.set("customized-dns-resolvers", this.customizedDNSResolver);
        yamlConfiguration.setComments("customized-dns-resolvers",
                List.of("This configuration item allows you to add a custom DNS server that has a higher priority than the user system's local DNS settings."));
        return yamlConfiguration;
    }
}
