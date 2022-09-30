package db.dao.node

import db.dao.OsmDao
import db.transaction.TransactionManager
import model.NodeEntity
import java.sql.PreparedStatement

class PreparedStatementNodeDao(
    transactionManager: TransactionManager
) : OsmDao<NodeEntity> {
    companion object {
        const val PREPARED_STATEMENT = "INSERT INTO NODES VALUES(?,?,?,?)"
    }

    val preparedStatement = transactionManager.runInTransaction {
        prepareStatement(PREPARED_STATEMENT)
    }

    override fun save(entity: NodeEntity) {
        setStatementVariables(preparedStatement, entity).executeUpdate()
    }

    fun setStatementVariables(statement: PreparedStatement, node: NodeEntity) = statement.apply {
        setObject(1, node.id)
        setString(2, node.user)
        setDouble(3, node.latitude)
        setDouble(4, node.longitude)
    }

    override fun close() {
        preparedStatement.close()
    }
}