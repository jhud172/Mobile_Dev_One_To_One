package uk.ac.cardiff.trainerhub.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class OneToOneApiClient(
    private val sessionStore: SecureSessionStore,
) {
    suspend fun get(path: String): JSONObject = request("GET", path, null)

    suspend fun post(path: String, body: JSONObject = JSONObject()): JSONObject = request("POST", path, body)

    private suspend fun request(method: String, path: String, body: JSONObject?): JSONObject = withContext(Dispatchers.IO) {
        val connection = URL(sessionStore.baseUrl() + path).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Content-Type", "application/json")

        val token = sessionStore.token()
        if (token != null) {
            connection.setRequestProperty("Authorization", "Bearer $token")
        }

        if (body != null) {
            connection.doOutput = true
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }
        }

        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val text = stream?.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        }.orEmpty()
        val json = if (text.isBlank()) JSONObject() else JSONObject(text)
        if (connection.responseCode !in 200..299) {
            throw MobileApiClientException(json.optString("error", "Request failed."), connection.responseCode)
        }
        json
    }
}

class MobileApiClientException(
    message: String,
    val statusCode: Int,
) : Exception(message)
