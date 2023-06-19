package a.b

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID


class BLE(address: String, data_services : Array<Service_data>, app_compat_activity: AppCompatActivity) {
    //private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var appCompatActivity : AppCompatActivity? = null

    init {
        Log.d(TAG, "Starting bluetooth")
        appCompatActivity = app_compat_activity
        val bluetoothManager = appCompatActivity!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(appCompatActivity!!, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
                ) {
                /*
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                */
                ActivityCompat.requestPermissions(appCompatActivity!!, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1234)
                //return
            }
            appCompatActivity!!.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            connectToDevice(address, bluetoothAdapter, data_services)
        }
    }

    private fun connectToDevice(address: String, bluetoothAdapter: BluetoothAdapter?, data_services : Array<Service_data>) {
        if (ContextCompat.checkSelfPermission(appCompatActivity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(appCompatActivity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        } else {
            Log.d(TAG, "About to connect to device")

            ///* Method 1:
            val device = bluetoothAdapter!!.getRemoteDevice(address)
            Thread.sleep(5000)
            val gatt_callback = mk_gatt_callback(data_services)
            bluetoothGatt = device.connectGatt(appCompatActivity, true, gatt_callback)
            //Thread.sleep(5000)
            //*/

            /* Method 2: try to see if you can discover the gatt server (FAILED for simulator)
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            val receiver = mk_receiver(address)
            registerReceiver(receiver, filter)
            bluetoothAdapter!!.startDiscovery()
            */

            /* Method 3: try to scan for the gatt server (SUCCESS for simulator)
            val bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
            bluetoothLeScanner!!.startScan(leScanCallback)
             */
        }
    }

    fun mk_receiver(address: String) : BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "Somebody has been discovered")

                //Thread.sleep(5000)

                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // A new device has been discovered
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (ActivityCompat.checkSelfPermission(appCompatActivity!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        /*
                        // dTODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        */
                        ActivityCompat.requestPermissions(appCompatActivity!!, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1234)
                        //return
                    }

                    Log.d(TAG, device.toString())
                    if(device.toString().equals(address)){
                        Log.d(TAG, "found it!")
                    }
                    // bluetoothGatt = device!!.connectGatt(this@MainActivity, false, bluetoothGattCallback)
                }
            }
        }
        return receiver
    }

    fun mk_gatt_callback(data_services : Array<Service_data>) : BluetoothGattCallback {
        val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                Thread.sleep(5000)
                super.onConnectionStateChange(gatt, status, newState)

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT server.")
                    if (ActivityCompat.checkSelfPermission(appCompatActivity!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        /*
                        // dTODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        */
                        ActivityCompat.requestPermissions(appCompatActivity!!, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1234)
                        //return
                    }
                    Log.d(TAG,"Attempting to start service discovery:" + bluetoothGatt!!.discoverServices())
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected from GATT server.")
                    /* trying to reconnect
                    //bluetoothGatt!!.close()
                    Thread.sleep(5000)
                    //Log.d(TAG, "Trying to reconnect")
                    val device = bluetoothAdapter!!.getRemoteDevice(simulator_address)
                    bluetoothGatt = device.connectGatt(this@MainActivity, false, this)
                    */
                }
            }

            fun do_for_service(data_service : Service_data){
                val service = bluetoothGatt!!.getService(UUID.fromString(data_service.service_uuid))
                if (service != null) {
                    for (data_characteristic in data_service.data_characteristics){
                        val characteristic = service.getCharacteristic(UUID.fromString(data_characteristic.uuid_string))
                        if (characteristic != null) {
                            if (ActivityCompat.checkSelfPermission(appCompatActivity!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                /*
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                 */
                                ActivityCompat.requestPermissions(appCompatActivity!!, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1234)
                                //return
                            }
                            bluetoothGatt!!.setCharacteristicNotification(characteristic, true)

                            val desc: BluetoothGattDescriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"))
                            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            bluetoothGatt!!.writeDescriptor(desc)
                        }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    for (data_service in data_services){
                        do_for_service(data_service)
                    }

                    //val service = bluetoothGatt!!.getService(UUID.fromString(obd_service_uuid_str))
//                    if (service != null) {
//                        val characteristic =
//                            service.getCharacteristic(UUID.fromString(obd_speed_uuid_str))
//                        if (characteristic != null) {
//                            if (ActivityCompat.checkSelfPermission(
//                                    this@MainActivity,
//                                    Manifest.permission.BLUETOOTH_CONNECT
//                                ) != PackageManager.PERMISSION_GRANTED
//                            ) {
//                                // TODO: Consider calling
//                                //    ActivityCompat#requestPermissions
//                                // here to request the missing permissions, and then overriding
//                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                //                                          int[] grantResults)
//                                // to handle the case where the user grants the permission. See the documentation
//                                // for ActivityCompat#requestPermissions for more details.
//                                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1234)
//                                //return
//                            }
//                            bluetoothGatt!!.setCharacteristicNotification(characteristic, true)
//                        }
//                    }
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                super.onCharacteristicChanged(gatt, characteristic)
                var f : ((String) -> Unit)? = null
                for (data_service in data_services){
                    for(data_characteristic in data_service.data_characteristics){
                        if(characteristic.uuid.toString().lowercase() == data_characteristic.uuid_string.lowercase()){
                            f = data_characteristic.update_state
                        }
                    }
                }

                if (f != null) {
                    f(String(characteristic.value))
                }

            }
        }
        return bluetoothGattCallback
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_LOCATION) {
//            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                //connectToDevice()
//            } else {
//                Toast.makeText(
//                    this,
//                    "Location permission is required to scan for BLE devices.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (bluetoothGatt != null) {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                ActivityCompat.requestPermissions(this@BLE, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1234)
//                //return
//            }
//            bluetoothGatt!!.close()
//            bluetoothGatt = null
//        }
//    }

    companion object {
        private const val TAG = "BLE"
        private const val REQUEST_ENABLE_BT = 1
        private const val PERMISSION_REQUEST_LOCATION = 2
    }
}