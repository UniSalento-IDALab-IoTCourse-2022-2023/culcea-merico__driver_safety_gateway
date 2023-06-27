package a.b

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.Klaxon
import java.io.File
import java.util.Optional

enum class Simulator_input {
    BLE, WS
}

fun simulator_data_flow(input_type: Simulator_input, change_state: ((State_data) -> State_data, Optional<Long>, Thread_type) -> Unit, app_compat_activity: AppCompatActivity) {
    val config = read_config()

    if(input_type == Simulator_input.BLE){
        ble_simulator_data(change_state, app_compat_activity, config)
    } else if (input_type == Simulator_input.WS){
        ws_simulator_data(change_state, app_compat_activity, config)
    }

}

fun ws_simulator_data(change_state: ((State_data) -> State_data, Optional<Long>, Thread_type) -> Unit, app_compat_activity: AppCompatActivity, config: Config) {
    //val ws_server_address : String = "192.168.1.61:8765"
    val ws_server_address : String = "${config.web_socket_ip_addr}:${config.web_socket_port}"

    val f : (String) -> Unit = { received_message ->
        data class Ws_response(val timestamp: String, val speed: String, val rpm : String, val engine_load : String, val throttle : String)

        val result = Klaxon().parse<Ws_response>(received_message)
        val parsed_state : State_data = parse_state_data_obd(result!!.speed, result.rpm, result.engine_load, result.throttle)

        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(parsed_state.speed, parsed_state.rpm, parsed_state.engine_load, parsed_state.throttle, prev_state.heart_beat)
        }
//        val timestamp_as_double = result!!.timestamp.toDouble() * 1000 // python tells the number of seconds from epoch as double
//        val timestamp = timestamp_as_double.toLong()
        val timestamp = string_to_double(result!!.timestamp).map { d ->  d.toLong() }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp, Thread_type.SIMULATOR)
        }
    }

    start_web_socket(ws_server_address, f)
}

fun ble_simulator_data(change_state: ((State_data) -> State_data, Optional<Long>, Thread_type) -> Unit, app_compat_activity: AppCompatActivity, config: Config) {
    //val simulator_address = "DC:A6:32:9B:5B:C3"
    val simulator_address = config.ble_address

//    val obd_service_uuid_str = "af7cf399-7046-4869-86e2-9aad105cc5ae"
//    val obd_speed_uuid_str = "9c9ec551-771f-4ef5-a3c9-687cd7223370"
    val obd_service_uuid_str = config.obd_service_uuid
    val obd_speed_uuid_str = config.obd_speed_uuid
    val obd_rpm_uuid_str: String = config.obd_rpm_uuid
    val obd_engine_load_uuid_str: String = config.obd_engine_load_uuid
    val obd_throttle_uuid_str: String = config.obd_throttle_uuid

    data class Split_result(val timestamp: Optional<Long>, val value: String)
    fun split_values(received_str : String) : Split_result {
        val timestamp_and_value_list = received_str.split("--")
        val timestamp_as_string : Optional<String> = optional_element(timestamp_and_value_list, 0)
        val timestamp = timestamp_as_string.map{ s -> (s.toDouble() * 1000).toLong() } // python tells the number of seconds from epoch as double
        val value : String = timestamp_and_value_list.get(1)
        return Split_result(timestamp, value)
    }

    val speed_c_data = Characteristic_data(obd_speed_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
        val (timestamp, speed_value) = split_values(received_str)
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(parse_obd_speed(speed_value), prev_state.rpm, prev_state.engine_load, prev_state.throttle, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp, Thread_type.SIMULATOR)
        }
    }
    )

    val rpm_c_data = Characteristic_data(obd_rpm_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
        val (timestamp, rpm_value) = split_values(received_str)
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(prev_state.speed, parse_obd_rpm(rpm_value), prev_state.engine_load, prev_state.throttle, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp, Thread_type.SIMULATOR)
        }
    }
    )

    val engine_load_c_data = Characteristic_data(obd_engine_load_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
        val (timestamp, engine_load_value) = split_values(received_str)
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(prev_state.speed, prev_state.rpm, parse_obd_engine_load(engine_load_value), prev_state.throttle, prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp, Thread_type.SIMULATOR)
        }
    }
    )

    val throttle_c_data = Characteristic_data(obd_throttle_uuid_str, { characteristic_value ->
        val received_str = characteristic_value
        val (timestamp, throttle_value) = split_values(received_str)
        val state_transaction : (State_data) -> State_data  = {prev_state ->
            State_data(prev_state.speed, prev_state.rpm, prev_state.engine_load, parse_obd_throttle(throttle_value), prev_state.heart_beat)
        }
        app_compat_activity.runOnUiThread{
            change_state(state_transaction, timestamp, Thread_type.SIMULATOR)
        }
    }
    )


    val obd_service = Service_data(obd_service_uuid_str, arrayOf(speed_c_data, rpm_c_data, engine_load_c_data, throttle_c_data ))

    BLE(simulator_address, arrayOf(obd_service, ), app_compat_activity)
}

fun parse_state_data_obd(speed : String, rpm : String, engine_load : String, throttle: String) : State_data {

    val speed_value : Optional<Double> = parse_obd_speed(speed)

    val rpm_value : Optional<Double> = parse_obd_rpm(rpm)

    val engine_load_value : Optional<Double> = parse_obd_engine_load(engine_load)

    val throttle_value : Optional<Double> = parse_obd_throttle(throttle)

    return State_data(speed_value, rpm_value, engine_load_value, throttle_value, Optional.ofNullable(null))

}

fun parse_obd_speed(speed: String) : Optional<Double> {
    // '72.0 kmh'
    val speed_value_op_string = optional_element(speed.split(" "), 0)
    return speed_value_op_string.map { s -> s.toDouble() }
}

fun parse_obd_rpm(rpm: String) : Optional<Double> {
    // '960.0 revolutions_per_minute'
    val rpm_value_op_string = optional_element(rpm.split(" "), 0)
    return rpm_value_op_string.map { s -> s.toDouble() }
}

fun parse_obd_engine_load(engine_load: String) : Optional<Double> {
    // '0.0 percent'
    val engine_load_value_op_string = optional_element(engine_load.split(" "), 0)
    return engine_load_value_op_string.map { s -> s.toDouble() }
}

fun parse_obd_throttle(throttle: String) : Optional<Double> {
    // '100.0 percent'
    val throttle_value_op_string = optional_element(throttle.split(" "), 0)
    return throttle_value_op_string.map { s -> s.toDouble() }
}
