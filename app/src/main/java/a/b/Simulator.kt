package a.b

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.Klaxon
import java.io.File

enum class Simulator_input {
    BLE, WS
}

fun simulator_data_flow(input_type: Simulator_input, change_state: ((State_data) -> State_data, String) -> Unit, app_compat_activity: AppCompatActivity) {
    val config = read_config()

    if(input_type == Simulator_input.BLE){
        ble_simulator_data(change_state, app_compat_activity, config)
    } else if (input_type == Simulator_input.WS){
        ws_simulator_data(change_state, app_compat_activity, config)
    }

}

fun ws_simulator_data(change_state: ((State_data) -> State_data, String) -> Unit, app_compat_activity: AppCompatActivity, config: Config) {
    //val ws_server_address : String = "192.168.1.61:8765"
    val ws_server_address : String = "${config.web_socket_ip_addr}:${config.web_socket_port}"

    val f : (String) -> Unit = { received_message ->
        data class Ws_response(val timestamp: String, val speed: String, val rpm : String, val engine_load : String, val throttle : String)

        val result = Klaxon().parse<Ws_response>(received_message)

        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(result!!.speed, result.rpm, result.engine_load, result.throttle, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, result!!.timestamp)
        }
    }

    start_web_socket(ws_server_address, f)
}

fun ble_simulator_data(change_state: ((State_data) -> State_data, String) -> Unit, app_compat_activity: AppCompatActivity, config: Config) {
    //val simulator_address = "DC:A6:32:9B:5B:C3"
    val simulator_address = config.ble_address

//    val obd_service_uuid_str = "af7cf399-7046-4869-86e2-9aad105cc5ae"
//    val obd_speed_uuid_str = "9c9ec551-771f-4ef5-a3c9-687cd7223370"
    val obd_service_uuid_str = config.obd_service_uuid
    val obd_speed_uuid_str = config.obd_speed_uuid
    val obd_rpm_uuid_str: String = config.obd_rpm_uuid
    val obd_engine_load_uuid_str: String = config.obd_engine_load_uuid
    val obd_throttle_uuid_str: String = config.obd_throttle_uuid

    data class Split_result(val timestamp: String, val value: String)
    fun split_values(received_str : String) : Split_result {
        val timestamp_and_value_list = received_str.split("--")
        val timestamp : String = timestamp_and_value_list.get(0)
        val value : String = timestamp_and_value_list.get(1)
        return Split_result(timestamp, value)
    }

    val speed_c_data = Characteristic_data(obd_speed_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
//        val timestamp_and_value_list = received_str.split("--")
//        val timestamp : String = timestamp_and_value_list.get(0)
//        val speed_value : String = timestamp_and_value_list.get(1)
        val (timestamp, speed_value) = split_values(received_str)
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(speed_value, prev_state.rpm, prev_state.engine_load, prev_state.throttle, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp)
        }
    }
    )

    val rpm_c_data = Characteristic_data(obd_rpm_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
        //val timestamp_and_value_list = received_str.split("--")
        //val timestamp : String = timestamp_and_value_list.get(0)
        //val rpm_value : String = timestamp_and_value_list.get(1)
        val (timestamp, rpm_value) = split_values(received_str)
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(prev_state.speed, rpm_value, prev_state.engine_load, prev_state.throttle, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp)
        }
    }
    )

    val engine_load_c_data = Characteristic_data(obd_engine_load_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
//        val timestamp_and_value_list = received_str.split("--")
//        val timestamp : String = timestamp_and_value_list.get(0)
//        val engine_load_value : String = timestamp_and_value_list.get(1)
        val (timestamp, engine_load_value) = split_values(received_str)
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(prev_state.speed, prev_state.rpm, engine_load_value, prev_state.throttle, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp)
        }
    }
    )

    val throttle_c_data = Characteristic_data(obd_throttle_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
//        val timestamp_and_value_list = received_str.split("--")
//        val timestamp : String = timestamp_and_value_list.get(0)
//        val engine_load_value : String = timestamp_and_value_list.get(1)
        val (timestamp, throttle_value) = split_values(received_str)
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(prev_state.speed, prev_state.rpm, prev_state.engine_load, throttle_value, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp)
        }
    }
    )


    val obd_service = Service_data(obd_service_uuid_str, arrayOf(speed_c_data, rpm_c_data, engine_load_c_data, throttle_c_data ))

    BLE(simulator_address, arrayOf(obd_service, ), app_compat_activity)
}

