package me.melijn.melijnbot.database.prefix

import me.melijn.melijnbot.database.Dao
import me.melijn.melijnbot.database.DriverManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserPrefixDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "userPrefixes"
    override val tableStructure: String = "userId bigint, prefixes varchar(256)"
    override val primaryKey: String = "userId"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }

    suspend fun set(userId: Long, prefixes: String) {
        driverManager.executeUpdate("INSERT INTO $table (userId, prefixes) VALUES (?, ?) ON CONFLICT ($primaryKey) DO UPDATE SET prefixes = ?",
            userId, prefixes, prefixes)
    }

    suspend fun get(userId: Long): String = suspendCoroutine {
        driverManager.executeQuery("SELECT * FROM $table WHERE userId = ?", { resultSet ->
            if (resultSet.next()) {
                it.resume(resultSet.getString("prefixes"))
            } else {
                it.resume("")
            }
        }, userId)
    }
}