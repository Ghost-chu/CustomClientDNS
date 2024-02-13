package com.ghostchu.mods.customclientdns.mixin;

import com.ghostchu.mods.customclientdns.dns.CustomInetSocketAddress;
import com.ghostchu.mods.customclientdns.dns.DNSLookupHelper;
import net.minecraft.client.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;

@Mixin(AllowedAddressResolver.class)
public class AllowedAddressResolverMixin {
    private Logger logger = LoggerFactory.getLogger("AllowedAddressResolverMixin");

    @Inject(method = "resolve", at = @At("HEAD"), cancellable = true)
    private void resolve(ServerAddress address, CallbackInfoReturnable<Optional<Address>> cir) {
        AllowedAddressResolver instance = ((AllowedAddressResolver) ((Object) this));
        AddressResolver addressResolver = address1 -> {
            try {
                DNSLookupHelper helper = new DNSLookupHelper(address1.getAddress(), address1.getPort());
                return Optional.of(new Address() {
                    @Override
                    public String getHostName() {
                        return helper.getHostName();
                    }

                    @Override
                    public String getHostAddress() {
                        return helper.getIpAddress();
                    }

                    @Override
                    public int getPort() {
                        return helper.getPort();
                    }

                    @Override
                    public InetSocketAddress getInetSocketAddress() {
                        return new CustomInetSocketAddress(helper.getIpAddress(), address.getAddress(), address.getPort());
                    }
                });
            } catch (UnknownHostException e) {
                logger.info("UnknownHostException: {}", address);
                return Optional.empty();
            }
        };
        RedirectResolver redirectResolver = ((AllowedAddressResolverAccessor) instance).getRedirectResolver();
        Optional<Address> addressResolverResult = addressResolver.resolve(address);
        if (addressResolverResult.isEmpty()) {
            Optional<ServerAddress> redirectResolverResult = redirectResolver.lookupRedirect(address);
            if (redirectResolverResult.isPresent()) {
                Optional<Address> addressResovledForRedirectResult = addressResolver.resolve(redirectResolverResult.get());
                Objects.requireNonNull(addressResovledForRedirectResult);
                addressResolverResult = addressResovledForRedirectResult;
            }
        }
        cir.setReturnValue(addressResolverResult);
    }
}
