package dev.kosmx.vikauthfabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.kosmx.vikauthfabric.AuthEvents;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginMixin {
    @ModifyExpressionValue(method = "onHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private boolean modifyAuth(boolean original, LoginHelloC2SPacket helloPacket) {
        return AuthEvents.playerPreJoin((ServerLoginNetworkHandler)(Object) this, original, helloPacket);
    }


    @ModifyExpressionValue(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"))
    private @Nullable Text canPlayerJoin(Text original) {
        return AuthEvents.playerJoin((ServerLoginNetworkHandler)(Object)this, original);
    }
}
