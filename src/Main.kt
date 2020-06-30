package com.firstapp

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.firstapp.api.getRequest
import com.firstapp.api.postRequest
import com.firstapp.api.userApi
import com.firstapp.crud.UserDatabase
import com.firstapp.database.DatabaseFactory
import com.firstapp.errors.errorHandler
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.accept
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {

        DatabaseFactory.init()

        install(CallLogging)
        {
            level = Level.INFO
            /*format { call ->
                when (val status = call.response.status() ?: "Unhandled") {
                    HttpStatusCode.Found -> "$status: ${call.request.httpMethod.value} - ${call.request.uri} " +
                            "[accept: '${call.request.accept()}'] -> " +
                            "${call.response.headers[HttpHeaders.Location]}"
                    else -> "$status: ${call.request.httpMethod.value} - ${call.request.uri} [accept: " +
                            "'${call.request.accept()}']"
                }
            }*/
        }

        install(StatusPages) {
            errorHandler()
        }

        install(ContentNegotiation) {
            jackson {
                configure(SerializationFeature.INDENT_OUTPUT, true)

                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

                setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                })
            }
        }

        install(Authentication){
            authenticationForm()
        }
        install(Routing) {
            getRequest()
            postRequest()
            userApi(UserDatabase())
        }
    }.also { it.start(wait = true) }

}

data class LResponse(val data: String)
data class LRequest(val id: Int, val name: String)
