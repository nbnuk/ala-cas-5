package au.org.ala.cas.session

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import org.apereo.cas.configuration.CasConfigurationProperties
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.mongo.MongoHealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.autoconfigure.mongo.MongoPropertiesClientSettingsBuilderCustomizer
import org.springframework.boot.autoconfigure.session.SessionProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.session.data.mongo.JdkMongoSessionConverter
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession
import java.time.Duration
import java.util.function.Supplier
import java.util.stream.Collectors


@Configuration(
    value = "mongoSessionConfiguration",
    proxyBeanMethods = false
)
@ConditionalOnProperty(value = ["spring.session.enabled"], havingValue = "true", matchIfMissing = false)
@EnableMongoHttpSession
@EnableConfigurationProperties(MongoProperties::class, SessionProperties::class, ServerProperties::class, CasConfigurationProperties::class)
class MongoSpringSessionConfiguration {

    @Bean
    @Qualifier("mongoOperations")
    @Primary
    fun mongoOperations(mongoDatabaseFactory: MongoDatabaseFactory): MongoOperations {
//        val factory = SimpleMongoClientDatabaseFactory(mongoClient, properties.mongoClientDatabase)
        return MongoTemplate(mongoDatabaseFactory)
    }

    @Bean
    fun mongoOperationsHealthIndicator(@Qualifier("mongoOperations") mongoOperations: MongoTemplate): MongoHealthIndicator {
        return MongoHealthIndicator(mongoOperations)
    }

    @Bean
    fun mongoDatabaseFactory(mongoClient: MongoClient, properties: MongoProperties): MongoDatabaseFactory? {
        return SimpleMongoClientDatabaseFactory(mongoClient, properties.mongoClientDatabase)
    }

    @Bean
    fun jdkMongoSessionConverter(sessionProperties: SessionProperties, serverProperties: ServerProperties, casConfigurationProperties: CasConfigurationProperties): JdkMongoSessionConverter {
        return JdkMongoSessionConverter(sessionProperties.determineTimeout {
            serverProperties.servlet.session.timeout
        })
    }


    @Bean
    @ConditionalOnMissingBean(MongoClient::class)
    fun mongo(
        builderCustomizers: ObjectProvider<MongoClientSettingsBuilderCustomizer?>,
        settings: MongoClientSettings?
    ): MongoClient {
        return MongoClientFactory(builderCustomizers.orderedStream().collect(Collectors.toList()))
            .createMongoClient(settings)
    }

    @Bean
    @ConditionalOnMissingBean(MongoClientSettings::class)
    fun mongoClientSettings(): MongoClientSettings {
        return MongoClientSettings.builder().build()
    }

    @Bean
    fun mongoPropertiesCustomizer(
        properties: MongoProperties?,
        environment: Environment?
    ): MongoPropertiesClientSettingsBuilderCustomizer {
        return MongoPropertiesClientSettingsBuilderCustomizer(properties, environment)
    }
}

