package dev.kosmx.vikauth.api

import com.macasaet.fernet.Key
import com.macasaet.fernet.StringValidator
import com.macasaet.fernet.Token
import com.mojang.authlib.GameProfile
import dev.kosmx.vikauthfabric.Main
import dev.kosmx.vikauthfabric.api.C2SVikAuthPacket
import dev.kosmx.vikauthfabric.api.S2CVikAuthPacket
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets


@Serializable
class APIData(val key: String,
              val url: String,
              val port: Int,
              val allowedDomain: String? = null,
              val serverName: String? = null
) {

    @Transient
    val fernetKey: Key = Key(key)

    private val logger: org.slf4j.Logger
        get() = Main.LOGGER

    fun fetchOffline(name: String): S2CVikAuthPacket {
        return fetch(C2SVikAuthPacket(name, "", premium = false, login = true))
    }

    fun fetchOnline(profile: GameProfile): S2CVikAuthPacket {
        return fetch(C2SVikAuthPacket(profile.name, profile.id.toString(), premium = true, login = true))
    }

    private fun fetch(i: C2SVikAuthPacket): S2CVikAuthPacket {
        val packet = i.copy(serverName = serverName)

        val out = Token.generate(fernetKey, Json.encodeToString(packet))
            .serialise().toByteArray()

        val outBuf = ByteBuffer.allocate(out.size + 4).apply {
            putInt(out.size)
            put(out)
        }

        val socket = Socket(url, port).apply {
            soTimeout = 10 * 1000
        }

        return try {
            return socket.getOutputStream().use { outStream ->
                outStream.write(outBuf.array())

                socket.getInputStream().use { inputStream ->
                    val dataSize = run {
                        val sizeData = inputStream.readNBytes(4)
                        ByteBuffer.wrap(sizeData).getInt()
                    }

                    val data = inputStream.readNBytes(dataSize)!!

                    val fernetIn = Token.fromString(String(data, StandardCharsets.UTF_8))
                    val input = fernetIn.validateAndDecrypt(fernetKey, object : StringValidator {})!!

                    Json.decodeFromString(input)
                }

            }
        } catch (e: Exception) {
            if (packet.login) { // If login is false, it's normal that the server closes connection.
                logger.error("Failed to fetch auth data", e)
            }
            S2CVikAuthPacket()
        } finally {
            socket.close()
        }
    }

    fun fireLogout(gameProfile: GameProfile) {
        fetch(C2SVikAuthPacket(gameProfile.name, gameProfile.id.toString(), login = false))
    }

}