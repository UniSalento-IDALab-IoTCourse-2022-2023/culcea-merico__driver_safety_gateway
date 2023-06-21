package a.b

import com.beust.klaxon.Klaxon
import java.io.File

data class Config(
    val web_socket_ip_addr: String,
    val web_socket_port: String,
    val ble_address: String,
    val obd_service_uuid: String,
    val obd_speed_uuid: String,
    val watch_ble_address: String,
    val watch_service_uuid: String,
    val watch_heartbeat_uuid: String
                  )

fun read_config(): Config? {
    val config_string : String = File("config.json").readText(Charsets.UTF_8)

    return Klaxon().parse<Config>(config_string)
}