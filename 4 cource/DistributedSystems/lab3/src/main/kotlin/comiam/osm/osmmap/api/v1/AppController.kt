package comiam.osm.osmmap.api.v1

import comiam.osm.osmmap.api.data.request.SearchRequest
import comiam.osm.osmmap.api.data.response.NodeResponse
import comiam.osm.osmmap.service.NodeService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/nodes")
class AppController(private val nodeService: NodeService) {
    @PostMapping("/search")
    fun findNodes(@RequestBody data: SearchRequest): List<NodeResponse> {
        return nodeService.getNodeBySearchParams(data)
    }
}