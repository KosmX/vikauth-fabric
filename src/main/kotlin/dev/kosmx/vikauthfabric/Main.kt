package dev.kosmx.vikauthfabric

import dev.kosmx.vikauth.api.APIData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import kotlin.io.path.inputStream

object Main : ModInitializer{
    lateinit var api: APIData

    @JvmStatic
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    fun main() = println("xd")

    @OptIn(ExperimentalSerializationApi::class)
    override fun onInitialize() {
        main()
        api = FabricLoader.getInstance().configDir.resolve("vikauth.json").inputStream().use { Json.decodeFromStream(it) }

        return ServerPlayConnectionEvents.DISCONNECT.register(AuthEvents::playerLeave)
    }
}