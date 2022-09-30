package comiam.osm.osmmap.model.entity

import org.hibernate.annotations.Type
import java.math.BigDecimal
import javax.persistence.*

@Table(name = "nodes")
@Entity
open class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "\"user\"", nullable = false)
    open var user: String? = null

    @Column(name = "latitude", nullable = false)
    open var latitude: BigDecimal? = null

    @Column(name = "longitude", nullable = false)
    open var longitude: BigDecimal? = null
}