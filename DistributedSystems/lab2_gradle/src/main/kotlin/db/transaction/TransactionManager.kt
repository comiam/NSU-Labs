package db.transaction

import db.datasource.IConnectionManager
import java.sql.Connection

data class Transaction(val connection: Connection)

class TransactionManager(
    private val connectionManager: IConnectionManager
) {
    private var currentTransaction: Transaction? = null

    fun getTransaction(): Transaction = currentTransaction ?: create()

    fun commit(transaction: Transaction) {
        currentTransaction
            ?.connection
            ?.commit()
            ?: throw IllegalStateException("")
    }

    fun rollback(transaction: Transaction) {
        currentTransaction
            ?.connection
            ?.rollback()
            ?: throw IllegalStateException("")
    }

    private fun create(): Transaction = Transaction(
        connectionManager.getConnection()
    ).also {
        it.connection.autoCommit = false
        currentTransaction = it
    }

    fun <R> runInTransaction(action: Connection.() -> R): R {
        val transaction = getTransaction()
        try {
            return transaction.connection
                .action()
                .also {
                    commit(transaction)
                }
        } catch (exc: Exception) {
            rollback(transaction)
            throw exc
        }
    }
}