package comiam.osm.osmmap.service

import comiam.osm.osmmap.api.data.request.SearchRequest
import comiam.osm.osmmap.api.data.response.*
import comiam.osm.osmmap.model.repo.NodeRepo
import comiam.osm.osmmap.model.repo.TagRepo
import org.springframework.stereotype.Service

@Service
class NodeService(
    private val nodeRepo: NodeRepo,
    private val tagRepo: TagRepo
) {
    fun getNodeBySearchParams(request: SearchRequest): List<NodeResponse> {
        val nodes = nodeRepo.searchNodeByPointAndDistance(request.latitude, request.longitude, request.radius)

        return nodes.map { node ->
            val tags = tagRepo.findAllByNodeid(node)
            val responceTags = tags.map { tag ->
                TagResponse(tag.id!!, tag.key!!, tag.value!!)
            }

            NodeResponse(node.id!!, node.user!!, node.latitude!!, node.longitude!!, responceTags)
        }
    }
}