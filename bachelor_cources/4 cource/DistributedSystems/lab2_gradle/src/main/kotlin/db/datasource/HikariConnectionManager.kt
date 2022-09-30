package db.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import db.DbProps
import java.sql.Connection

class HikariConnectionManager(
    private val properties: DbProps
) : IConnectionManager {
    private val config = HikariConfig()

    private val ds: HikariDataSource = HikariDataSource(
        config.apply {
            jdbcUrl = properties.jdbcUrl
            username = properties.userName
            password = properties.password
            // enable postgresql server-side statement-caching
            dataSourceProperties["prepareThreshold"] = "1"
        }
    )

    override fun getConnection(): Connection = ds.connection

    override fun closeConnection(connection: Connection) = connection.close()

    override fun close() = ds.close()
}