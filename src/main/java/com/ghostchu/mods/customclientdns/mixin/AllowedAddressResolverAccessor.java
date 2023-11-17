package com.ghostchu.mods.customclientdns.mixin;

import net.minecraft.client.network.AddressResolver;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.BlockListChecker;
import net.minecraft.client.network.RedirectResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AllowedAddressResolver.class)
public interface AllowedAddressResolverAccessor {
    @Accessor
    AddressResolver getAddressResolver();
    @Accessor
    RedirectResolver getRedirectResolver();
    @Accessor
    BlockListChecker getBlockListChecker();
}
