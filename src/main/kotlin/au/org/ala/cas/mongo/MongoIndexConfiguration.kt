package au.org.ala.cas.mongo

import au.org.ala.cas.jndi.JndiConfigurationProperties
import au.org.ala.utils.logger
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Collation
import com.mongodb.client.model.IndexOptions
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.TicketCatalog
import org.apereo.cas.ticket.registry.TicketHolder
import org.apereo.cas.ticket.registry.TicketRegistry
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.util.ObjectUtils
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Configuration
@EnableConfigurationProperties(JndiConfigurationProperties::class, CasConfigurationProperties::class)
class MongoIndexConfiguration {

    companion object {
        private val log = logger()
        private val MONGO_INDEX_KEYS = setOf("v", "key", "name", "ns")
        private val EMPTY_OPTIONS = BasicDBObject()
    }

    @Autowired
    lateinit var mongoDbTicketRegistryTemplate: MongoTemplate

    @Autowired
    lateinit var ticketRegistry: TicketRegistry

    @Autowired
    lateinit var ticketCatalog: TicketCatalog

    @PostConstruct
    fun initMongoIndices() {
        val definitions = ticketCatalog.findAll()
        definitions.forEach { t ->
            val name = t.properties.storageName
            val c = mongoDbTicketRegistryTemplate.db.getCollection(name)
            c.ensureIndex(BasicDBObject(TicketHolder.FIELD_NAME_ID, 1), BasicDBObject("unique", 1))
            log.debug("Ensured MongoDb collection {} index for [{}]", TicketHolder.FIELD_NAME_ID, c.namespace.fullName)
        }
    }

    private fun <T> MongoCollection<T>.ensureIndex(indexKey: BasicDBObject, options: BasicDBObject = EMPTY_OPTIONS) {
        // Mongo will throw an Exception if attempting to create an index whose key already exists
        // as an index but has different options.  No exception is thrown if the index is exactly the same.
        // So drop existing indices on the same keys but with different options before recreating them.
        val existingMismatch = this.listIndexes().any { existing ->
            val keyMatches = existing["key"] == indexKey
            val optionsMatch = options.entries.all { entry -> entry.value == existing[entry.key] }
            val noExtraOptions = existing.keys.all { key -> MONGO_INDEX_KEYS.contains(key) || options.keys.contains(key) }

            keyMatches && !(optionsMatch && noExtraOptions)
        }
//        val existingMismatch = indexInfo.any { existing ->
//            val keyMatches = existing["key"] == indexKey
//            val optionsMatch = options.entries.all { entry -> entry.value == existing[entry.key] }
//            val noExtraOptions = existing.keySet().all { key -> MONGO_INDEX_KEYS.contains(key) || options.keys.contains(key) }
//
//            keyMatches && !(optionsMatch && noExtraOptions)
//        }
        if (existingMismatch) {
            log.debug("Removing MongoDb index [{}] from [{}] because it appears to already exist in a different form", indexKey, this.namespace.fullName)
            dropIndex(indexKey)
        }

        createIndex(indexKey.toBsonDocument(), indexDefinitionIndexOptionsConverter.convert(options))
    }

    val indexDefinitionIndexOptionsConverter: Converter<BasicDBObject, IndexOptions> =
        Converter { indexOptions: BasicDBObject ->
            var ops = IndexOptions()
            if (indexOptions.containsKey("name")) {
                ops = ops.name(indexOptions["name"].toString())
            }
            if (indexOptions.containsKey("unique")) {
                ops = ops.unique(indexOptions.getBoolean("unique"))
            }
            if (indexOptions.containsKey("sparse")) {
                ops = ops.sparse(indexOptions["sparse"] as Boolean)
            }
            if (indexOptions.containsKey("background")) {
                ops = ops.background(indexOptions["background"] as Boolean)
            }
            if (indexOptions.containsKey("expireAfterSeconds")) {
                ops =
                    ops.expireAfter(indexOptions["expireAfterSeconds"] as Long, TimeUnit.SECONDS)
            }
            if (indexOptions.containsKey("min")) {
                ops = ops.min((indexOptions["min"] as Number).toDouble())
            }
            if (indexOptions.containsKey("max")) {
                ops = ops.max((indexOptions["max"] as Number).toDouble())
            }
            if (indexOptions.containsKey("bits")) {
                ops = ops.bits(indexOptions["bits"] as Int)
            }
            if (indexOptions.containsKey("bucketSize")) {
                ops = ops.bucketSize((indexOptions["bucketSize"] as Number).toDouble())
            }
            if (indexOptions.containsKey("default_language")) {
                ops = ops.defaultLanguage(indexOptions["default_language"].toString())
            }
            if (indexOptions.containsKey("language_override")) {
                ops = ops.languageOverride(indexOptions["language_override"].toString())
            }
            if (indexOptions.containsKey("weights")) {
                ops = ops.weights(indexOptions["weights"] as Document)
            }
            for (key in indexOptions.keys) {
                if (ObjectUtils.nullSafeEquals("2dsphere", indexOptions[key])) {
                    ops = ops.sphereVersion(2)
                }
            }
            if (indexOptions.containsKey("partialFilterExpression")) {
                ops = ops.partialFilterExpression(indexOptions["partialFilterExpression"] as Document)
            }
            if (indexOptions.containsKey("collation")) {
                ops = ops.collation(
                    fromDocument(
                        indexOptions["collation"] as? Document
                    )
                )
            }
            ops
        }

    fun fromDocument(source: Document?): Collation? {
        return if (source == null) {
            null
        } else org.springframework.data.mongodb.core.query.Collation.from(source).toMongoCollation()
    }
}