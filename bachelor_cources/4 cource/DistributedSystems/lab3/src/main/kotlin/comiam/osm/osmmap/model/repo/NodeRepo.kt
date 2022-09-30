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
            "where earth_box(ll_to_earth(:latitude, :longitude), :radius) @> ll_to_earth(latitude, longitude)\n" +
            "order by point(longitude, latitude) <@> point(:latitude, :longitude) desc"
    )
    fun searchNodeByPointAndDistance(latitude: Float, longitude: Float, radius: Float): Iterable<Node>
}
