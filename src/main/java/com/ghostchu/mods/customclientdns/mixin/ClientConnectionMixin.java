package com.ghostchu.mods.customclientdns.mixin;

import com.ghostchu.mods.customclientdns.dns.CustomInetSocketAddress;
import com.ghostchu.mods.customclientdns.dns.DNSLookupHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.client.network.*;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.handler.PacketSizeLogger;
import net.minecraft.util.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static net.minecraft.network.ClientConnection.CLIENT_IO_GROUP;
import static net.minecraft.network.ClientConnection.EPOLL_CLIENT_IO_GROUP;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    private Logger logger = LoggerFactory.getLogger("ClientConnectionMixin");
    @Inject(method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;", at = @At("HEAD"), cancellable = true)
    private static void connect(InetSocketAddress address, boolean useEpoll, ClientConnection connection, CallbackInfoReturnable<ChannelFuture> cir) {

        EventLoopGroup eventLoopGroup;
        Class<? extends Channel> class_;
        if (Epoll.isAvailable() && useEpoll) {
            class_ = EpollSocketChannel.class;
            eventLoopGroup = EPOLL_CLIENT_IO_GROUP.get();
        } else {
            class_ = NioSocketChannel.class;
            eventLoopGroup = CLIENT_IO_GROUP.get();
        }
        cir.setReturnValue(new Bootstrap().group(eventLoopGroup).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                ClientConnection.setHandlers(channel);
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
                ClientConnection.addHandlers(channelPipeline, NetworkSide.CLIENTBOUND, ((ClientConnectionAccessor) connection).getPacketSizeLogger());
                connection.addFlowControlHandler(channelPipeline);
            }
        }).channel(class_).connect(address.getAddress(), address.getPort()));

//        Supplier<? extends EventLoopGroup> lazy;
//        Class<? extends Channel> class_;
//        if (Epoll.isAvailable() && useEpoll) {
//            class_ = EpollSocketChannel.class;
//            lazy = EPOLL_CLIENT_IO_GROUP;
//        } else {
//            class_ = NioSocketChannel.class;
//            lazy = CLIENT_IO_GROUP;
//        }
//        InetAddress addressToUse = address.getAddress();
//        if(address instanceof CustomInetSocketAddress customInetSocketAddress){
//            try {
//                addressToUse = InetAddress.getByName(customInetSocketAddress.getRealAddress());
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
//        }
//         cir.setReturnValue(new Bootstrap().group(lazy.get()).handler(new ChannelInitializer<>() {
//
//             @Override
//             protected void initChannel(Channel channel) {
//                 try {
//                     channel.config().setOption(ChannelOption.TCP_NODELAY, true);
//                 } catch (ChannelException channelException) {
//                     // empty catch block
//                 }
//                 ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
//                 ClientConnection.addHandlers(channelPipeline, NetworkSide.CLIENTBOUND, ((ClientConnectionAccessor) connection).getPacketSizeLogger());
//                 channelPipeline.addLast("packet_handler", connection);
//             }
//         }).channel(class_).connect(addressToUse, address.getPort()));

    }
}
