package db.dao

import db.dao.node.BatchNodeDao
import db.dao.node.PreparedStatementNodeDao
import db.dao.node.StatementNodeDao
import db.dao.tag.BatchTagDao
import db.dao.tag.PreparedStatementTagDao
import db.dao.tag.StatementTagDao
import db.transaction.TransactionManager
import model.NodeEntity
import model.TagEntity
import service.BatchNodeService
import service.NodeService

object NodeServiceFactory {
    enum class Strategy {
        STATEMENT,
        PREPARED_STATEMENT,
        BATCH
    }

    private val creators = mapOf(
        Strategy.STATEMENT to OsmDaoCreators(
            ::StatementNodeDao,
            ::StatementTagDao,
            ::NodeService
        ),
        Strategy.PREPARED_STATEMENT to OsmDaoCreators(
            ::PreparedStatementNodeDao,
            ::PreparedStatementTagDao,
            ::NodeService
        ),
        Strategy.BATCH to OsmDaoCreators(
            ::BatchNodeDao,
            ::BatchTagDao,
            ::BatchNodeService
        )
    )

    fun createService(strategy: Strategy, manager: TransactionManager): NodeService {
        return creators[strategy]?.let {
            it.serviceCreator(
                manager,
                it.nodeDaoCreator(manager),
                it.tagDaoCreator(manager)
            )
        } ?: throw IllegalArgumentException("Wrong strategy: $strategy")
    }
}

private class OsmDaoCreators(
    val nodeDaoCreator: (TransactionManager) -> OsmDao<NodeEntity>,
    val tagDaoCreator: (TransactionManager) -> OsmDao<TagEntity>,
    val serviceCreator: (
        TransactionManager,
        OsmDao<NodeEntity>,
        OsmDao<TagEntity>
    ) -> NodeService
)

