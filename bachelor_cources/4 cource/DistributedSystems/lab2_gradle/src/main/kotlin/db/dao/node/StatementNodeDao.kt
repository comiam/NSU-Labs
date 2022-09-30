package db.dao.node

import db.dao.OsmDao
import db.transaction.TransactionManager
import model.NodeEntity
import utils.escapeQuotes

class StatementNodeDao(
    transactionManager: TransactionManager
) : OsmDao<NodeEntity> {
    private val statement = transactionManager.runInTransaction {
        createStatement()
    }

    override fun save(entity: NodeEntity) {
        statement.execute(
            """INSERT INTO NODES VALUES(
                ${entity.id},
                '${entity.user.escapeQuotes()}',
                ${entity.latitude},
                ${entity.longitude}
            )""".trimIndent()
        )
    }

    override fun close() = statement.close()
}