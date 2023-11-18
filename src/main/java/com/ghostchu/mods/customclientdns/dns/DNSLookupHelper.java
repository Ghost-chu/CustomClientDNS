package com.ghostchu.mods.customclientdns.dns;

import com.ghostchu.mods.customclientdns.IPAddressMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author tekgator <a href="https://gist.github.com/tekgator/fd39017561b506139962">...</a>
 */
public class DNSLookupHelper {
    private static final Logger LOG = LoggerFactory.getLogger("DNSLookupHelper");
    private static final int DEFAULT_PORT = 25565;
    private static final String SRV_STR = "_minecraft._tcp.";
    private String ipAddress = "";
    private String hostName = "";
    private int port = 0;

    public DNSLookupHelper(String hostName, int port) throws
            UnknownHostException {
        this.resolve(hostName, port);
    }

    public DNSLookupHelper(String hostName) throws
            UnknownHostException {
        this.resolve(hostName, 0);
    }

    private void resolve(String hostName, int port) throws
            UnknownHostException {
        // check whether the port is encoded within the hostname string
        String[] parts = hostName.split(":");
        if (parts.length > 2) {
            StringJoiner partsHosts = new StringJoiner(":");
            for (int i = 0; i < parts.length; i++) {
                partsHosts.add(parts[i]);
                if (i + 1 >= parts.length - 1) {
                    break;
                }
            }
            String partsPort = parts[parts.length - 1];
            String partsHost = partsHosts.toString();
            parts = new String[2];
            parts[0] = partsHost;
            parts[1] = partsPort;
        }
        if (parts.length > 1) {
            this.hostName = parts[0];
            try {
                this.port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                LOG.debug("Invalid port within hostname '{}' provided, use port from second parameter '{}'", hostName, port);
                this.port = port;
            }
        } else {
            this.hostName = hostName;
            this.port = port;
        }

        if (!IPAddressMatcher.isIPAddress(this.hostName) &&
                !Objects.equals(this.hostName, InetAddress.getLocalHost().getHostName()) &&
                this.port == 0) {
            // input is an hostname, but no port submitted, try to resolve via SRV record
            try {
                SRVRecord srvRecord = (SRVRecord) lookupRecord(SRV_STR + hostName, Type.SRV);
                this.hostName = srvRecord.getTarget().toString().replaceFirst("\\.$", "");
                this.port = srvRecord.getPort();
            } catch (UnknownHostException e) {
                // no SRV record found at the moment, just continue
            }
        }

        if (IPAddressMatcher.isIPAddress(this.hostName)) {
            // hostname provided is a IP address
            this.ipAddress = this.hostName;
        } else {
            // hostname provided is an actual hostname, resolve IP address
            try {
                this.ipAddress = ((ARecord) lookupRecord(this.hostName, Type.A)).getAddress().getHostAddress();
            } catch (UnknownHostException e) {
                this.ipAddress = ((AAAARecord) lookupRecord(this.hostName, Type.AAAA)).getAddress().getHostAddress();
            }
        }

        if (this.port == 0) {
            // couldn't resolve via SVR record, therefore use default minecraft port
            this.port = DEFAULT_PORT;
        }
    }

    private Record lookupRecord(String hostName, int type) throws
            UnknownHostException {

        Record record;
        Lookup lookup;
        int result;

        try {
            lookup = new Lookup(hostName, type);
        } catch (TextParseException e) {
            throw new UnknownHostException(String.format("Host '%s' parsing error:%s", hostName, e.getMessage()));
        }

        lookup.run();

        result = lookup.getResult();

        if (result == Lookup.SUCCESSFUL) {
            record = lookup.getAnswers()[0];
            LOG.info("Successfully got DNS record of type '{}' for hostname '{}': '{}'", Type.string(type), hostName, record);
        } else {
            LOG.debug("Failed to get DNS record of type '{}' for hostname '{}'", type, hostName);

            switch (result) {
                case Lookup.HOST_NOT_FOUND:
                    throw new UnknownHostException(String.format("Host '%s' not found", hostName));
                case Lookup.TYPE_NOT_FOUND:
                    throw new UnknownHostException(String.format("Host '%s' not found (no A record)", hostName));
                case Lookup.UNRECOVERABLE:
                    throw new UnknownHostException(String.format("Cannot lookup host '%s'", hostName));
                case Lookup.TRY_AGAIN:
                    throw new UnknownHostException(String.format("Temporary failure to lookup host '%s'", hostName));
                default:
                    throw new UnknownHostException(String.format("Unknown error %d in host lookup of '%s'", result, hostName));
            }
        }

        return record;
    }


    public int getPort() {
        return this.port;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    @Override
    public String toString() {
        return "DNSLookupHelper{" +
                "ipAddress='" + ipAddress + '\'' +
                ", hostName='" + hostName + '\'' +
                ", port=" + port +
                '}';
    }
}
