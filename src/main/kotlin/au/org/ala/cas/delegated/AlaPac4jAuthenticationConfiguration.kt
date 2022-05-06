package au.org.ala.cas.delegated

import au.org.ala.cas.AlaCasProperties
import au.org.ala.utils.logger
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.scribejava.core.model.OAuth1RequestToken
import org.apereo.cas.authentication.principal.PrincipalFactory
import org.apereo.cas.authentication.principal.PrincipalResolver
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils
import org.apereo.cas.configuration.support.JpaBeans
import org.apereo.cas.ticket.TransientSessionTicket
import org.apereo.cas.ticket.TransientSessionTicketImpl
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlan
import org.apereo.cas.ticket.serialization.serializers.TransientSessionTicketStringSerializer
import org.apereo.services.persondir.IPersonAttributeDao
import org.pac4j.core.client.Clients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import javax.annotation.PostConstruct

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
class AlaPac4jAuthenticationConfiguration {
    companion object {
        val log = logger()
    }

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Autowired
    lateinit var clients: Clients

    @Autowired
    lateinit var ticketSerializationExecutionPlan: TicketSerializationExecutionPlan

    @Bean
    @Qualifier("userCreatorDataSource")
    fun userCreatorDataSource() = JpaBeans.newDataSource(alaCasProperties.userCreator.jdbc)

    @Bean
    fun userCreatorTransactionManager() = DataSourceTransactionManager(userCreatorDataSource())

    @Bean
    fun userCreator(): UserCreator = UserCreatorALA(
        dataSource = userCreatorDataSource(),
        userCreatePassword = alaCasProperties.userCreator.userCreatePassword,
        createUserProcedure = alaCasProperties.userCreator.jdbc.createUserProcedure,
        passwordEncoder = PasswordEncoderUtils.newPasswordEncoder(alaCasProperties.userCreator.passwordEncoder, applicationContext)
    )

    @Bean(name = ["clientPrincipalFactory"])
    @RefreshScope
    fun clientPrincipalFactory(
        @Autowired @Qualifier("personDirectoryAttributeRepositoryPrincipalResolver") personDirectoryPrincipalResolver: PrincipalResolver,
        @Autowired @Qualifier("cachingAttributeRepository") cachingAttributeRepository: IPersonAttributeDao, //CachingPersonAttributeDaoImpl,
        @Autowired userCreator: UserCreator
    ): PrincipalFactory = AlaPrincipalFactory(personDirectoryPrincipalResolver, cachingAttributeRepository, userCreator)

    // TODO How to replicate this?
    @PostConstruct
    fun sortClients() {
        log.info("Sorting clients")
        this.clients.findAllClients()?.sortBy { client ->
            val idx = alaCasProperties.clientSortOrder.indexOf(client.name)
            if (idx == -1) alaCasProperties.clientSortOrder.size else idx
        }
        log.info("Sorted clients: {}", clients.clients)
    }

    @PostConstruct
    fun updateTickets() {
        log.info("Updating ticket plans to fix CAS bugs")
        val serializer = ticketSerializationExecutionPlan.getTicketSerializer(TransientSessionTicketImpl::class.java)
        if (serializer is TransientSessionTicketStringSerializer) {
            serializer.objectMapper.addMixIn(OAuth1RequestToken::class.java, OAuth1RequestTokenMixin::class.java)
        } else {
            log.warn("TST serializer {} is not TransientSessionTicketStringSerializer :(", serializer)
        }
    }

}

abstract class OAuth1RequestTokenMixin @JsonCreator constructor(
    @JsonProperty("token") token: String,
    @JsonProperty("tokenSecret") tokenSecret: String,
    @JsonProperty("oauthCallbackConfirmed") oauthCallbackConfirmed: Boolean,
    @JsonProperty("rawResponse") rawResponse: String?) {

}