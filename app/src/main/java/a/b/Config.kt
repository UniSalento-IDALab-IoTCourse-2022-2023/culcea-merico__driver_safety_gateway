package a.b

import com.beust.klaxon.Klaxon
import java.io.File

data class Config(
    val web_socket_ip_addr: String,
    val web_socket_port: String,
    val ble_address: String,
    val obd_service_uuid: String,
    val obd_speed_uuid: String,
    val obd_rpm_uuid: String,
    val obd_engine_load_uuid: String,
    val obd_throttle_uuid: String,
    val watch_ble_address: String,
    val watch_service_uuid: String,
    val watch_heartbeat_uuid: String,
    val backend_ip: String,
    val backend_port: String,
    val backend_url_post_drive_alert: String,
    val backend_url_post_hearbeat_alert: String
                  )

fun read_config(): Config {
    return Config(
        "192.168.105.51",
        "8765",
    "E4:5F:01:5F:5C:35",
    "af7cf399-7046-4869-86e2-9aad105cc5ae",
    "9c9ec551-771f-4ef5-a3c9-687cd7223370",
        "c1053c6f-e375-4e77-9afe-9d71aeea2854",
        "29713fe1-f53a-4836-8445-22da7f0837bb",
        "18bf892a-76e0-482c-a7ef-b1ffd1b9f3c8",
        "A0:B7:65:F5:6F:A6",
    "6e400001-b5a3-f393-e0a9-e50e24dcca9e",
    "6f400003-b5a3-f393-e0a9-e50f24dcca9f",
        "http://192.168.105.60",
        "3000",
        "/api/post/driveAlert",
        "/api/post/heartAlert"
    )
}