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
    private List<Resolver> resolvers;

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
        try {
            LOGGER.info("DNS lookup for www.baidu.com: " + new DNSLookupHelper("www.baidu.com"));
            LOGGER.info("DNS lookup for github.com: " + new DNSLookupHelper("github.com"));
            LOGGER.info("DNS lookup for gbcraft.org: " + new DNSLookupHelper("gbcraft.org"));
            LOGGER.info("DNS lookup for zth.ria.red: " + new DNSLookupHelper("zth.ria.red"));
            LOGGER.info("DNS lookup for play.barbatos.club: " + new DNSLookupHelper("play.barabtos.club"));
        } catch (UnknownHostException e) {
            LOGGER.info(e.getClass().getName() + ": " + e.getMessage());
        }
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
        this.resolvers = resolvers;
        Lookup.setPacketLogger((prefix, local, remote, data) -> LOGGER.info("SocketAddress-Local: {}, SocketAddress-Remote: {}, Data: {}", local,remote, new String(data, StandardCharsets.UTF_8)));
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
