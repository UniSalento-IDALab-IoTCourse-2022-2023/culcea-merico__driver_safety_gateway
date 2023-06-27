package a.b


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.beust.klaxon.Klaxon
import java.net.URI
import java.util.Calendar
import java.util.Optional
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private var state : State_data = empty_state_data()
    private var prev_state : State_data = empty_state_data()
    private var prev_timestamp_watch : Optional<Long> = Optional.ofNullable(null)


    // view elements
    private var speed_view_elem: TextView? = null
    private var rpm_view_elem: TextView? = null
    private var engine_load_view_elem: TextView? = null
    private var throttle_view_elem: TextView? = null

    private var heart_view_elem: TextView? = null

    private var start_button_elem: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        speed_view_elem = findViewById<TextView>(R.id.speed_view)
        rpm_view_elem = findViewById<TextView>(R.id.rpm_view)
        engine_load_view_elem = findViewById<TextView>(R.id.engine_load_view)
        throttle_view_elem = findViewById<TextView>(R.id.throttle_view)
        heart_view_elem = findViewById(R.id.heart_view)

        start_button_elem = findViewById(R.id.start_button)

        start_button_elem?.setOnClickListener { view ->
            start_button_elem?.visibility = View.GONE
            Thread(Runnable {
                simulator_data_flow(Simulator_input.WS, this::change_state, this)
            }).start()
            Thread(Runnable {
                watch_data_flow(this::change_state, this)
            }).start()

        }




        return
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // try to make the connection again
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to scan for BLE devices.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        /*
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // dTODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1234)
                //return
            }
            bluetoothGatt!!.close()
            bluetoothGatt = null
        }
         */
    }

    fun change_state(state_transaction: (State_data) -> State_data, timestamp: Optional<Long>, thread_type: Thread_type): Unit {
        state = state_transaction(this.state)

        speed_view_elem?.text = "Speed: \n" + state.speed.map { v -> v.toString() }.orElse("")
        rpm_view_elem?.text = "RPM: \n" + state.rpm.map { v -> v.toString() }.orElse("")
        engine_load_view_elem?.text  = "Engine load: \n" + state.engine_load.map { v -> v.toString() }.orElse("")
        throttle_view_elem?.text  = "Throttle: \n" + state.throttle.map { v -> v.toString() }.orElse("")
        heart_view_elem?.text = "Heartbeat: \n" + state.heart_beat.map { v -> v.toString() }.orElse("")


        // anomaly detection

        // get current date and hour
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date_string : String = dateFormat.format(currentDate)

        val current_hour = Date()
        val dateFormat_hour = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val time_string : String = dateFormat_hour.format(current_hour)

//        Log.d("main", date_string + "--" + time_string)

        val config = read_config()

        if (thread_type == Thread_type.WATCH){

            state.heart_beat.ifPresent {heart_beat ->
                if (heart_beat < 70) {
                    Log.d("PERICOLO", "Battito sceso sotto i 70!")
                    val data = """
                        {
                            "timestamp": {
                                "date": "${date_string}",
                                "time": "${time_string}"
                            },
                            "heartRate": {
                                "value": "${heart_beat}",
                                "unitMeasure": "bpm"
                            },
                            "vehicleID": "3a465f29-3109-4988-9976-7c8420956565"
                        }
                    """.trimIndent()
                    Thread(Runnable {
                        send_post_request("${config.backend_ip}:${config.backend_port}${config.backend_url_post_hearbeat_alert}", data)
                    }).start()
                }
            }
        }

        if(thread_type == Thread_type.SIMULATOR) {

//        Log.d("main", "prev: " + prev_state.toString() + "\nCurrent:" + state.toString() + "\nprev_t: " + prev_timestamp.toString() + "\nCurrent_t: " + timestamp.toString())
            val acc: Optional<Double> = calc_acceleration(prev_timestamp_watch, timestamp, prev_state.speed, state.speed)
            acc.ifPresent { acc_value ->
                Log.d("main", acc_value.toString())
                if (Math.abs(acc_value) > 3) {
                    val data = """
                    {
                        "timestamp": {
                            "date": "${date_string}",
                            "time": "${time_string}"
                        },
                        "acceleration": {
                            "value": "${acc_value}",
                            "unitMeasure": "m/s^2"
                        },
                        "vehicleID": "3a465f29-3109-4988-9976-7c8420956565"
                    }
                    """.trimIndent()
                    Thread(Runnable {
                        send_post_request(
                            "${config.backend_ip}:${config.backend_port}${config.backend_url_post_drive_alert}",
                            data
                        )
                    }).start()
                }
            }
            prev_timestamp_watch = timestamp.map { t -> t }
        }

        prev_state = state.copy()

        return
    }

}

enum class Thread_type {
    WATCH, SIMULATOR
}