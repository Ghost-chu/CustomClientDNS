# CustomClientDNS

🔧Fix DNS! A Fabric mod to use custom DNS server for resolve the Multiplayer servers IP address, including DoH.

## Description

This Fabric Mod for Minecraft Multiplayer provides a custom DNS resolver to resolve DNS hijacking, DNS pollution, and users not being able to connect to your server due to incorrect local DNS settings.

## Features

* Support SRV record
* Supprot AAAA (IPV6) record
* Use DoH (Dns Over Https) servers
* Use or prevent use user local Dns server settings.

## Configuration

```yaml
# This configuration item allows you to configure whether or not to use the local DNS settings of the user's computer system.
# When set to false, the system's local DNS is ignored and only the DNS servers configured by the Mod are used.
use-local-dns-server: true
# This configuration item allows you to add a custom DNS server that has a higher priority than the user system's local DNS settings.
customized-dns-resolvers:
- https://dns.alidns.com/dns-query
- https://dot.pub/dns-query
- 223.5.5.5
- 119.29.29.29
- 114.114.114.114
- 1.1.1.1
- 8.8.8.8
```

## Credit

DNSLookupHelper.java is a modified class from [tekgator](https://gist.github.com/tekgator/fd39017561b506139962), added AAAA support.
