package comiam.osm.osmmap.api.data.response

import java.math.BigDecimal

data class NodeResponse(
    val id: Long,
    val user:String,
    val latitude:BigDecimal,
    val longitude: BigDecimal,
    val tags: Iterable<TagResponse>,
)
