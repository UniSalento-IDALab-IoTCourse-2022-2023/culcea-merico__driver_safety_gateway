package a.b

import android.util.Log
import java.util.Optional

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

fun send_post_request(url: String, postData: String) {
    val urlObject = URL(url)
    val connection = urlObject.openConnection() as HttpURLConnection

    Log.d("POST", "Trying to send post request")

    // Set the necessary headers
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("charset", "utf-8")
    connection.setRequestProperty("Content-Length", postData.length.toString())

    // Enable output and input streams
    connection.doOutput = true
    connection.doInput = true

    try {
        // Write the data to the request body
        val outputStream = DataOutputStream(connection.outputStream)
        outputStream.writeBytes(postData)
        outputStream.flush()
        outputStream.close()

        // Read the response
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            // Process the response
            val responseBody = response.toString()
            // Do something with the response
            Log.d("POST", responseBody)
        } else {
            // Handle error cases
            Log.d("POST", "Error in post! ${responseCode}")

        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        connection.disconnect()
    }
}


fun <T : Any> optional_element(xs : List<T>, index: Int) : Optional<T>{
    if (xs.size <= index){
        return Optional.ofNullable(null)
    } else {
        return Optional.of(xs.get(index))
    }
}

fun calc_acceleration(prev_timestamp: Optional<Long>, current_timestamp : Optional<Long>, prev_speed : Optional<Double>, current_speed: Optional<Double>) : Optional<Double> {
    val time_delta : Optional<Double> = prev_timestamp.flatMap { pt -> current_timestamp.map { ct -> (ct - pt).toDouble() / 1000 } } // ms -> s

    val speed_delta : Optional<Double> = prev_speed.flatMap { ps -> current_speed.map { cs -> (cs - ps) / 3.6 } } // km/h -> m/s

//    Log.d("acc", "current - previous = time_delta = ${current_timestamp} - ${prev_timestamp} = ${time_delta} s")
//    Log.d("acc",  "current - previous = speed_delta = ${current_speed} - ${prev_speed} = ${speed_delta} m/s")
    return time_delta.flatMap { td -> speed_delta.map { sd -> sd / td } } // m/s^2
}

fun string_to_int(s: String) : Optional<Int> {
    try {
        return Optional.of(s.toInt())
    } catch (e : NumberFormatException){
        return Optional.ofNullable(null)
    }
}

fun string_to_double(s: String) : Optional<Double> {
    try {
        return Optional.of(s.toDouble())
    } catch (e : NumberFormatException){
        return Optional.ofNullable(null)
    }
}
