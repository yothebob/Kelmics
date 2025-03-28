package org.kel.mics.IO

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class Greeting {
    private val client = HttpClient()

    suspend fun greeting(): String {
        val response = client.get("https://ktor.io/docs/")
        return response.bodyAsText()
    }

    suspend fun putCmd(): String {
        val response = client.get("https://ktor.io/docs/")
        return response.bodyAsText()
    }
}