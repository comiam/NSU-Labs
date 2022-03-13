package service

import db.dao.OsmDao
import db.transaction.TransactionManager
import model.TagEntity
import model.NodeEntity

open class NodeService(
    private val transactionManager: TransactionManager,
    protected val nodeDao: OsmDao<NodeEntity>,
    protected val tagDao: OsmDao<TagEntity>
) : INodeService {

    override fun save(entity: NodeEntity) {
        transactionManager.runInTransaction {
            nodeDao.save(entity)
            entity.tags.forEach(tagDao::save)
        }
    }

    override fun close() {
        nodeDao.close()
        tagDao.close()
    }
}