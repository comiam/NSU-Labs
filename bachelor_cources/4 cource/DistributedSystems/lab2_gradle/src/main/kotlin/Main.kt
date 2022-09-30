import db.DBManager
import db.DbProps
import db.dao.NodeServiceFactory
import db.datasource.HikariConnectionManager
import db.datasource.SingleConnectionManager

class ResourceLoader {
    val path: String? = "/home/maxim/git/NSU-Labs/4 cource/DistributedSystems/lab2_gradle/build/resources/main/RU-NVS.osm.bz2"
}

// docker run --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=Osm -e POSTGRES_USER=postgres -e POSTGRES_INITDB_ARGS="-E UTF8" -d -p 5432:5432 postgres
fun main() {
    val resourceLoader = ResourceLoader()
    DBManager.runInsertionTest(resourceLoader.path!!,
        {SingleConnectionManager(DbProps.parseDBProps())},
        NodeServiceFactory.Strategy.BATCH
    )
}