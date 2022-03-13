package model

import model.TagEntity
import models.Node
import java.math.BigInteger

class NodeEntity {
    lateinit var id: BigInteger

    lateinit var user: String

    var latitude: Double = 0.0

    var longitude: Double = 0.0

    lateinit var tags: List<TagEntity>

    companion object {
        fun toDbNode(node: Node): NodeEntity =
            NodeEntity().apply {
                this.id = node.id
                this.latitude = node.lat
                this.longitude = node.lon
                this.user = node.user
                this.tags = node.tag.map {
                    TagEntity(
                        key = it.k,
                        value = it.v,
                        nodeId = node.id
                    )
                }
            }
    }
}
