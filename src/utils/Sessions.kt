package com.llamalad7.utils

import com.llamalad7.data.Session
import java.security.SecureRandom
import java.time.Instant
import java.util.*

class Sessions {
    companion object {
        val sessionList = mutableMapOf<UUID, Session>()
        fun newSession(username: String): UUID {
            sessionList.filterValues { it.username == username }.forEach { sessionList.remove(it.key) }
            var id: UUID?
            do {
                val byteArray = ByteArray(32)
                SecureRandom().nextBytes(byteArray)
                id = UUID.nameUUIDFromBytes(byteArray)
            } while (id in sessionList)
            val session = Session(username, Instant.now().plusSeconds(1800))
            sessionList[id!!] = session
            return id
        }
        fun Session.hasExpired(id: UUID): Boolean {
            return if (Instant.now() > this.expiry) {
                sessionList.remove(id)
                true
            } else {
                false
            }
        }
    }
}