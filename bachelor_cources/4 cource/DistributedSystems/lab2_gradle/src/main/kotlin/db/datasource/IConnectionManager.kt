package db.datasource

import java.sql.Connection

interface IConnectionManager : AutoCloseable {
    fun getConnection(): Connection
    fun closeConnection(connection: Connection)
}