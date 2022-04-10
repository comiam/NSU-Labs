package comiam.osm.osmmap.model.repo

import comiam.osm.osmmap.model.entity.Node
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NodeRepo: JpaRepository<Node, Long> {
    @Query(nativeQuery = true, value =
            "select * from nodes\n" +
            "where point(longitude, latitude) <@> point(:longitude, :latitude) < :radius"
    )
    fun searchNodeByPointAndDistance(latitude: Float, longitude: Float, radius: Float): Iterable<Node>
}
