package com.ghostchu.mods.customclientdns;

import com.ghostchu.mods.customclientdns.config.DNSConfig;
import com.ghostchu.mods.customclientdns.dns.DNSLookupHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CustomClientDNS implements ModInitializer {
    public DNSConfig DNS_CONFIG;
    public static CustomClientDNS INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger("CustomClientDNS");

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        INSTANCE = this;
        try {
            DNS_CONFIG = loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e); // Crash the game if files failed to create to avoid silent failure
        }
        setupDNSResolver(DNS_CONFIG);
        selfTest();
    }

    private void selfTest() {
        List<String> testList = List.of(
                "www.baidu.com",
                "www.google.com",
                "www.mcbbs.net",
                "www.imdodo.com",
                "www.qq.com",
                "weibo.com",
                "github.com",
                "gbcraft.org",
                "zth.ria.red"
        );
        testList.parallelStream().forEach(s -> {
            try {
                LOGGER.info("[Self-Test] DNS lookup for {}: {} ", s, new DNSLookupHelper(s));
            } catch (UnknownHostException e) {
                LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
            }
        });
    }

    private void setupDNSResolver(DNSConfig dnsConfig) {
        List<Resolver> resolvers = new ArrayList<>();
        for (String dnsServer : dnsConfig.getCustomizedDNSResolver()) {
            try {
                if (dnsServer.startsWith("http")) {
                    LOGGER.info("Added {} as a DoH resolver", dnsServer);
                    resolvers.add(new DohResolver(dnsServer));
                } else {
                    LOGGER.info("Added {} as a Simple resolver", dnsServer);
                    resolvers.add(new SimpleResolver(dnsServer));
                }
            } catch (UnknownHostException e) {
                LOGGER.error("Failed to register dns server {}, skipping...", dnsServer, e);
            }
        }
        if (dnsConfig.isUseLocalDnsServer()) {
            try {
                resolvers.add(new SimpleResolver((String) null));
            } catch (UnknownHostException e) {
                LOGGER.error("Failed to add default resolver", e);
            }
        }
        Lookup.setPacketLogger((prefix, local, remote, data) -> LOGGER.debug("SocketAddress-Local: {}, SocketAddress-Remote: {}, Data: {}", local, remote, new String(data, StandardCharsets.UTF_8)));
        Lookup.setDefaultResolver(new ExtendedResolver(resolvers));
    }

    private DNSConfig loadConfig() throws IOException {
        File configDirPath = FabricLoaderImpl.INSTANCE.getConfigDir().toFile();
        if (!configDirPath.exists()) {
            configDirPath.mkdirs();
        }
        File configFile = new File(configDirPath, "customclientdns.yml");
        if (!configFile.exists()) {
            configFile.createNewFile();
            new DNSConfig().createConfiguration().save(configFile);// Save default configuration
        }
        return new DNSConfig(YamlConfiguration.loadConfiguration(configFile));
    }

}
