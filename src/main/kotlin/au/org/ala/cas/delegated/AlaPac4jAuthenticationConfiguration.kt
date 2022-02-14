package au.org.ala.cas.delegated

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.authentication.principal.PrincipalFactory
import org.apereo.cas.authentication.principal.PrincipalResolver
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils
import org.apereo.cas.configuration.support.JpaBeans
import org.apereo.services.persondir.IPersonAttributeDao
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl
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

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Autowired
    lateinit var clients: Clients

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
        this.clients.findAllClients()?.sortBy { client ->
            val idx = alaCasProperties.clientSortOrder.indexOf(client.name)
            if (idx == -1) alaCasProperties.clientSortOrder.size else idx
        }
    }

}