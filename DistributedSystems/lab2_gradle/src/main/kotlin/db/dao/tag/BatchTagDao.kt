package db.dao.tag

import db.dao.OsmDao
import db.transaction.TransactionManager
import model.TagEntity
import java.sql.PreparedStatement

class BatchTagDao(
    private val transactionWrapper: TransactionManager
) : OsmDao<TagEntity> {

    private val dao = PreparedStatementTagDao(transactionWrapper)

    override fun save(entity: TagEntity) = dao.save(entity)

    override fun saveAll(entities: Iterable<TagEntity>) {
        dao.preparedStatement.let { statement ->
            addBatches(statement, entities)
            statement.executeBatch()
        }
    }

    private fun addBatches(statement: PreparedStatement, tags: Iterable<TagEntity>) {
        tags.forEach { tag ->
            dao.setStatementVariables(statement, tag)
            statement.addBatch()
        }
    }

    override fun close() = dao.close()
}