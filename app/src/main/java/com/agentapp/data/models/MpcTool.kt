package com.agentapp.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "mpc_tools",
    foreignKeys = [
        ForeignKey(
            entity = MpcServer::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serverId"])]
)
data class MpcTool(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val uid: Long = 0,
    val toolId: String = java.util.UUID.randomUUID().toString(),
    val serverId: String,
    val name: String,
    val description: String,
    val inputSchema: String,  // JSON string of the schema
    val enabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
