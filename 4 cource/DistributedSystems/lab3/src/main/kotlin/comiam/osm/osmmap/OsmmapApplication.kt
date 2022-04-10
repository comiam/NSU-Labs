package comiam.osm.osmmap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OsmmapApplication

/*
docker run --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=Osm -e POSTGRES_USER=postgres -e POSTGRES_INITDB_ARGS="-E UTF8" -d -p 5432:5432 postgres
exec in db:
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;

run 2nd lab before start
execute post request localhost:8080/v1/nodes/search with example body
{
    "latitude": 82.2662868,
    "longitude": 56.0371736,
    "radius": 1700.0
}
 */
fun main(args: Array<String>) {
    runApplication<OsmmapApplication>(*args)
}
