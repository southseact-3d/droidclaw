package com.agentapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mpc_servers")
data class MpcServer(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val secret: String,
    val enabled: Boolean = true,
    val priority: Int = 0
)
