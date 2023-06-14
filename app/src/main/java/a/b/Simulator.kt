package a.b

import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.Klaxon


enum class Simulator_input {
    BLE, WS
}

fun simulator_data_flow(input_type: Simulator_input, change_state: ((State_data) -> State_data, String) -> Unit, app_compat_activity: AppCompatActivity) {
    if(input_type == Simulator_input.BLE){
        ble_simulator_data(change_state, app_compat_activity)
    } else if (input_type == Simulator_input.WS){
        ws_simulator_data(change_state, app_compat_activity)
    }

}

fun ws_simulator_data(change_state: ((State_data) -> State_data, String) -> Unit, app_compat_activity: AppCompatActivity) {
    val ws_server_address : String = "192.168.1.50:8765"

    val f : (String) -> Unit = { received_message ->
        data class Ws_response(val timestamp: Double, val speed: Double)

        val result = Klaxon().parse<Ws_response>(received_message)

        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(result!!.speed, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, result!!.timestamp.toString())
        }
    }

    start_web_socket(ws_server_address, f)
}

fun ble_simulator_data(change_state: ((State_data) -> State_data, String) -> Unit, app_compat_activity: AppCompatActivity) {
    val simulator_address = "DC:A6:32:9B:5B:C3"

    val obd_service_uuid_str = "af7cf399-7046-4869-86e2-9aad105cc5ae"
    val obd_speed_uuid_str = "9c9ec551-771f-4ef5-a3c9-687cd7223370"

    val speed_c_data = Characteristic_data(obd_speed_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
        val timestamp_and_value_list = received_str.split("--")
        val timestamp : String = timestamp_and_value_list.get(0)
        val speed_value : Double = timestamp_and_value_list.get(1).toDouble()
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(speed_value, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp)
        }
    }
    )

    val obd_service = Service_data(obd_service_uuid_str, arrayOf(speed_c_data, ))

    BLE(simulator_address, arrayOf(obd_service, ), app_compat_activity)
}
