package db.datasource

import db.DbProps
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class SingleConnectionManager(
    private val properties: DbProps
) : IConnectionManager {
    private var connection: Connection? = null

    override fun getConnection(): Connection = connection
        ?: createConnection().also { connection = it }

    override fun closeConnection(connection: Connection) {}

    override fun close() {
        connection?.close()
    }

    private fun createConnection() = DriverManager.getConnection(
        properties.jdbcUrl,
        Properties().apply {
            setProperty("user", properties.userName)
            setProperty("password", properties.password)
            setProperty("prepareThreshold", "1")
        }
    )
}