package com.llamalad7

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.llamalad7.data.GetDataRequest
import com.llamalad7.data.SetDataRequest
import com.llamalad7.data.User
import com.llamalad7.data.Users
import com.llamalad7.data.Users.data
import com.llamalad7.data.Users.password
import com.llamalad7.utils.Sessions
import com.llamalad7.utils.Utils
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection

fun main() {
    Database.connect(
        "jdbc:sqlite:database/database.db",
        "org.sqlite.JDBC"
    )

    transaction(Connection.TRANSACTION_SERIALIZABLE, 1) {
        SchemaUtils.create(Users)
    }
    embeddedServer(Netty, port = 8080) {
        install(DefaultHeaders)
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
        routing {
            post("/signup") {
                val user = call.receive<User>()
                val exists = Utils.getUser(user.username) != null
                if (exists) {
                    call.respondText(ContentType.Application.Json) {
                        val response = JsonObject()
                        response.addProperty("ok", false)
                        response.addProperty("message", "User Already Exists")
                        response.toString()
                    }
                } else {
                    transaction(Connection.TRANSACTION_SERIALIZABLE, 1) {
                        Users.insert {
                            it[username] = user.username
                            it[password] = BCrypt.hashpw(user.password, BCrypt.gensalt())
                            it[data] = ""
                        }
                    }
                    call.respondText(ContentType.Application.Json) {
                        val response = JsonObject()
                        response.addProperty("ok", true)
                        response.addProperty("message", "Successful")
                        response.toString()
                    }
                }
            }
            post("/login") {
                val user = call.receive<User>()
                val entry = Utils.getUser(user.username)
                if (entry == null) {
                    call.respondText(ContentType.Application.Json) {
                        val response = JsonObject()
                        response.addProperty("ok", false)
                        response.addProperty("message", "User Doesn't Exist")
                        response.toString()
                    }
                } else {
                    val correct = BCrypt.checkpw(user.password, entry[password])
                    if (correct) {
                        call.respondText(ContentType.Application.Json) {
                            val response = JsonObject()
                            response.addProperty("ok", true)
                            response.addProperty("message", "Correct Password")
                            val id = Sessions.newSession(user.username)
                            response.addProperty("sessionId", id.toString())
                            response.add("session", Gson().toJsonTree(Sessions.sessionList[id]))
                            response.toString()
                        }
                    } else {
                        call.respondText(ContentType.Application.Json) {
                            val response = JsonObject()
                            response.addProperty("ok", false)
                            response.addProperty("message", "Incorrect Password")
                            response.toString()
                        }
                    }
                }
            }
            get("/data") {
                val request = call.receive<GetDataRequest>()
                if (!Utils.sessionBad(request.sessionId, request.session, call)) {
                    val entry = Utils.getUser(request.session.username)
                    call.respondText(ContentType.Application.Json) {
                        val response = JsonObject()
                        response.addProperty("ok", true)
                        response.addProperty("message", "Valid Session")
                        response.addProperty("data", entry!![data])
                        response.toString()
                    }
                }
            }
            post("/data") {
                val request = call.receive<SetDataRequest>()
                if (!Utils.sessionBad(request.sessionId, request.session, call)) {
                    Utils.setUserData(request.session.username, request.data)
                    call.respondText(ContentType.Application.Json) {
                        val response = JsonObject()
                        response.addProperty("ok", true)
                        response.addProperty("message", "Successfully Updated")
                        response.addProperty("data", request.data)
                        response.toString()
                    }
                }
            }
        }
    }.start(wait = true)
}

