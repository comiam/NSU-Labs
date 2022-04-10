package comiam.osm.osmmap.model.repo

import comiam.osm.osmmap.model.entity.Node
import comiam.osm.osmmap.model.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TagRepo: JpaRepository<Tag, Long> {
    fun findAllByNodeid(node: Node): Iterable<Tag>
}