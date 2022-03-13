package db.dao.tag

import db.dao.OsmDao
import db.transaction.TransactionManager
import model.TagEntity
import utils.escapeQuotes

class StatementTagDao(
    transactionManager: TransactionManager
) : OsmDao<TagEntity> {

    private val statement = transactionManager.runInTransaction {
        createStatement()
    }

    override fun save(entity: TagEntity) {
        statement.execute(
            """INSERT INTO TAGS(key, value, nodeId) VALUES(
                    '${entity.key?.escapeQuotes()}',
                    '${entity.value?.escapeQuotes()}',
                    ${entity.nodeId}
            )""".trimIndent()
        )
    }

    override fun close() = statement.close()
}