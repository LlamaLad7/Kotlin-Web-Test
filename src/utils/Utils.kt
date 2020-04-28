package com.llamalad7.utils

import com.google.gson.JsonObject
import com.llamalad7.data.Session
import com.llamalad7.data.Users
import com.llamalad7.utils.Sessions.Companion.hasExpired
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.sql.Connection
import java.util.*

class Utils {
    companion object {
        fun getUser(username: String) : ResultRow? {
            return transaction(Connection.TRANSACTION_SERIALIZABLE, 1) {
                Users.select { Users.username eq username }.firstOrNull()
            }
        }
        fun setUserData(username: String, data: String) {
            transaction(Connection.TRANSACTION_SERIALIZABLE, 1) {
                Users.update({Users.username eq username}) {
                    it[Users.data] = data
                }
            }
        }
        suspend fun sessionBad(id: UUID, session: Session, call: ApplicationCall): Boolean {
            if (id !in Sessions.sessionList || Sessions.sessionList[id] != session) {
                call.respondText(ContentType.Application.Json) {
                    val response = JsonObject()
                    response.addProperty("ok", false)
                    response.addProperty("message", "Invalid Session")
                    response.toString()
                }
                return true
            }
            if (session.hasExpired(id)) {
                call.respondText(ContentType.Application.Json) {
                    val response = JsonObject()
                    response.addProperty("ok", false)
                    response.addProperty("message", "Session Expired")
                    response.toString()
                }
                return true
            }
            return false
        }
    }
}