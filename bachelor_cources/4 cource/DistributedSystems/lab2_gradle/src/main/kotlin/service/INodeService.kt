package service

import model.NodeEntity

interface INodeService : AutoCloseable {
    fun save(entity: NodeEntity)
}