package dev.kosmx.vikauthfabric

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.mojang.authlib.GameProfile
import dev.kosmx.vikauth.api.APIData
import dev.kosmx.vikauthfabric.api.*
import dev.kosmx.vikauthfabric.mixin.ServerLoginHandlerAccessInterface
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerLoginNetworkHandler
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import java.util.concurrent.TimeUnit

object AuthEvents {

    val api: APIData
        get() = Main.api

    private val cache: Cache<ServerLoginNetworkHandler, S2CVikAuthPacket> = CacheBuilder.newBuilder()
        .maximumSize(512)
        .weakKeys()
        .expireAfterAccess(20, TimeUnit.SECONDS)
        .build()


    @JvmStatic
    fun ServerLoginNetworkHandler.playerPreJoin(onlineMode: Boolean, helloPacket: LoginHelloC2SPacket): Boolean {
        return if (onlineMode) {
            val res = api.fetchOffline(helloPacket.profile.name)
            if (res.allowed) {
                cache.put(this, res)
                this.profile = GameProfile(res.uuid, res.displayName)
                false
            } else true
        } else false
    }

    @JvmStatic
    fun ServerLoginNetworkHandler.playerJoin(original: Text?): Text? {
        return original ?: let {
            if (this in cache) {
                cache.invalidate(this)
                null
            }
            else if (api.fetchOnline(profile!!).allowed) {
                null
            } else {
                TranslatableText("multiplayer.disconnect.not_whitelisted")
            }
        }
    }

    fun playerLeave(handler: ServerPlayNetworkHandler, minecraft: MinecraftServer) {
        api.fireLogout(handler.player.gameProfile)
    }
}

var ServerLoginNetworkHandler.profile: GameProfile?
    get() = (this as ServerLoginHandlerAccessInterface).profile
    set(value) {
        (this as ServerLoginHandlerAccessInterface).profile = value
    }

private operator fun <K: Any, V> Cache<K, V>.contains(k: K): Boolean = getIfPresent(k) != null
