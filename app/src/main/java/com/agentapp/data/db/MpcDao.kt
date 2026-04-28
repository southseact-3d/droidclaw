package com.agentapp.data.db

import androidx.room.*
import com.agentapp.data.models.MpcServer
import com.agentapp.data.models.MpcTool
import kotlinx.coroutines.flow.Flow

@Dao
interface MpcDao {
    // Servers
    @Query("SELECT * FROM mpc_servers ORDER BY priority ASC, name ASC")
    fun getServers(): Flow<List<MpcServer>>

    @Query("SELECT * FROM mpc_servers WHERE enabled = 1 ORDER BY priority ASC, name ASC")
    fun getEnabledServers(): Flow<List<MpcServer>>

    @Query("SELECT * FROM mpc_servers WHERE id = :id")
    suspend fun getServer(id: String): MpcServer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(server: MpcServer)

    @Update
    suspend fun update(server: MpcServer)

    @Query("DELETE FROM mpc_servers WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM mpc_servers")
    suspend fun deleteAll()

    // Tools
    @Query("SELECT * FROM mpc_tools WHERE serverId = :serverId ORDER BY name ASC")
    suspend fun getToolsForServer(serverId: String): List<MpcTool>

    @Query("SELECT * FROM mpc_tools WHERE serverId IN (:serverIds) AND enabled = 1 ORDER BY name ASC")
    fun getToolsForServers(serverIds: List<String>): Flow<List<MpcTool>>

    @Query("SELECT * FROM mpc_tools WHERE toolId = :toolId")
    suspend fun getTool(toolId: String): MpcTool?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: MpcTool)

    @Update
    suspend fun updateTool(tool: MpcTool)

    @Query("DELETE FROM mpc_tools WHERE serverId = :serverId")
    suspend fun deleteToolsForServer(serverId: String)

    @Query("DELETE FROM mpc_tools WHERE toolId = :toolId")
    suspend fun deleteTool(toolId: String)

    @Query("DELETE FROM mpc_tools")
    suspend fun deleteAllTools()
}
