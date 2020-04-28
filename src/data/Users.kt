package com.llamalad7.data

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val username = text("username")
    val password = text("password")
    val data = text("data")
}