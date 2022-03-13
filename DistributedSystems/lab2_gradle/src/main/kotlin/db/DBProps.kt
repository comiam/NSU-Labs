package db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

class DbProps(
    var jdbcUrl: String = "jdbc:postgresql://localhost:5432/Osm",
    var userName: String = "postgres",
    var password: String = "password",
) {
    companion object {
        fun parseDBProps(
            propertiesPath: String = DBManager.DEFAULT_PROPERTIES_PATH
        ): DbProps {
            val mapper = ObjectMapper(YAMLFactory())
            return mapper.readValue(
                this.javaClass
                    .classLoader
                    .getResourceAsStream(propertiesPath),
                DbProps::class.java
            )
        }
    }
}