package dev.kosmx.vikauthfabric.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLoginNetworkHandler.class)
public interface ServerLoginHandlerAccessInterface {
    @Accessor
    GameProfile getProfile();

    @Accessor
    void setProfile(GameProfile profile);
}
