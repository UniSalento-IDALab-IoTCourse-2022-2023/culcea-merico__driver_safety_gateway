package a.b

import androidx.appcompat.app.AppCompatActivity

fun watch_data_flow(change_state: ((State_data) -> State_data, String) -> Unit, app_compat_activity: AppCompatActivity){
    ble_watch_data(change_state, app_compat_activity)
}

fun ble_watch_data(change_state: ((State_data) -> State_data, String) -> Unit, app_compat_activity: AppCompatActivity){
    val config = read_config()

    //val watch_address = "A0:B7:65:F5:6F:A6"
    val watch_address = config!!.watch_ble_address

//    val service_uuid_str = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E".lowercase()
//    val heartbeat_uuid_str = "6F400003-B5A3-F393-E0A9-E50F24DCCA9F".lowercase()
    val service_uuid_str = config.watch_service_uuid
    val heartbeat_uuid_str = config.watch_heartbeat_uuid

    val heartbeat_c_data = Characteristic_data(heartbeat_uuid_str, { characteristic_value ->
        val received_str = characteristic_value

        val timestamp : String = System.currentTimeMillis().toString()

        val heartbeat : Int = received_str.toDouble().toInt()

        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(prev_state.speed, heartbeat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp)
        }
    }
    )

    val hearbeat_service = Service_data(service_uuid_str, arrayOf(heartbeat_c_data, ))

    BLE(watch_address, arrayOf(hearbeat_service, ), app_compat_activity)
}