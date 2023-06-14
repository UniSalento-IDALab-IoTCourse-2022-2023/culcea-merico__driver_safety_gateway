package a.b

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.TimeUnit


fun start_web_socket(ws_ip_address : String, do_with_message: (String) -> Unit){
    val uri: URI = URI("ws://${ws_ip_address}/")

    val mWebSocketClient = object : WebSocketClient(uri) {
        override fun onOpen(serverHandshake: ServerHandshake) {
            Log.i("Websocket", "Opened")
        }

        override fun onMessage(s: String) {
            //Log.d("D", s)
            do_with_message(s)
        }

        override fun onClose(i: Int, s: String, b: Boolean) {
            Log.i("Websocket", "Closed $s")
        }

        override fun onError(e: Exception) {
            Log.i("Websocket", "Error " + e.message)
        }
    }

    mWebSocketClient.connectBlocking(10, TimeUnit.SECONDS)
}