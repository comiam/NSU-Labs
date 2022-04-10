package comiam.osm.osmmap.model.entity

import org.hibernate.annotations.Type
import javax.persistence.*

@Table(name = "tags")
@Entity
open class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "key", nullable = false)
    open var key: String? = null

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "value", nullable = false)
    open var value: String? = null

    @ManyToOne
    @JoinColumn(name = "nodeid")
    open var nodeid: Node? = null
}