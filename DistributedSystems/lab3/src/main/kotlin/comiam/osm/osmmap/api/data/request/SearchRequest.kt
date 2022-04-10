package comiam.osm.osmmap.api.data.request

data class SearchRequest(
    val latitude: Float,
    val longitude: Float,
    val radius: Float
)