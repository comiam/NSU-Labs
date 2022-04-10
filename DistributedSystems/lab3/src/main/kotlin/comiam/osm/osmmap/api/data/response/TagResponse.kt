package comiam.osm.osmmap.api.data.response

import comiam.osm.osmmap.model.entity.Node
import javax.persistence.Column
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne

data class TagResponse(
    val id: Long,
    val key: String,
    val value: String,
)