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


class MainActivity : AppCompatActivity() {
    private var state : State_data = State_data("", "", "","","")

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

    fun change_state(state_transaction: (State_data) -> State_data, timestamp: String): Unit {
        state = state_transaction(this.state)

        speed_view_elem?.text = "Speed: \n" + state.speed
        rpm_view_elem?.text = "RPM: \n" + state.rpm
        engine_load_view_elem?.text  = "Engine load: \n" + state.engine_load
        throttle_view_elem?.text  = "Throttle: \n" + state.throttle
        heart_view_elem?.text = "Heartbeat: \n" + state.heart_beat

        // anomaly detection
        if(state.heart_beat != "") {
            val heart_beat: Double = state.heart_beat.toDouble()
            if (heart_beat < 70) {
                Log.d("PERICOLO", "Battito sceso sotto i 70!")
            }
        }

        // send to backend

        return
    }
}