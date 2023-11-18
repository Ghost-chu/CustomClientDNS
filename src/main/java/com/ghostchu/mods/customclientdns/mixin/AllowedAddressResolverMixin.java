package com.ghostchu.mods.customclientdns.mixin;

import com.ghostchu.mods.customclientdns.dns.DNSLookupHelper;
import net.minecraft.client.network.*;
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
    @Inject(method = "resolve", at = @At("HEAD"), cancellable = true)
    private void resolve(ServerAddress address, CallbackInfoReturnable<Optional<Address>> cir) {
        AllowedAddressResolver instance = ((AllowedAddressResolver) ((Object) this));
        AddressResolver addressResolver = address1 -> {
            try {
                DNSLookupHelper helper = new DNSLookupHelper(address1.getAddress(), address1.getPort());
                return Optional.of(Address.create(new InetSocketAddress(helper.getIpAddress(), helper.getPort())));
            } catch (UnknownHostException e) {
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
