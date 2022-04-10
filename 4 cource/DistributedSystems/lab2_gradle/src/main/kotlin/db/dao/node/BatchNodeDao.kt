package db.dao.node

import db.dao.OsmDao
import db.transaction.TransactionManager
import model.NodeEntity
import java.sql.PreparedStatement

class BatchNodeDao(
    transactionManager: TransactionManager
) : OsmDao<NodeEntity> {
    private val dao = PreparedStatementNodeDao(transactionManager)

    override fun save(entity: NodeEntity) = dao.save(entity)

    override fun saveAll(entities: Iterable<NodeEntity>) {
        dao.preparedStatement.let { statement ->
            addBatches(statement, entities)
            statement.executeBatch()
        }
    }

    private fun addBatches(statement: PreparedStatement, nodes: Iterable<NodeEntity>) {
        nodes.forEach { node ->
            dao.setStatementVariables(statement, node)
            statement.addBatch()
        }
    }

    override fun close() = dao.close()
}