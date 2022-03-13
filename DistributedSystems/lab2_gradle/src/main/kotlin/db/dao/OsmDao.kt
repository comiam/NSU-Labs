package db.dao

interface OsmDao<E> : AutoCloseable {
    fun save(entity: E)

    fun saveAll(entities: Iterable<E>) = entities.forEach { save(it) }
}