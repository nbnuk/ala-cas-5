package au.org.ala.cas.thymeleaf

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.ticket.registry.TicketRegistry
import org.apereo.cas.web.cookie.CasCookieBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
class AlaThymeleafConfiguration {

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Autowired
    lateinit var ticketRegistry: TicketRegistry

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    lateinit var ticketGrantingTicketCookieGenerator: CasCookieBuilder

    @Bean
    fun alaTemplateClient() = AlaTemplateClient(alaCasProperties.skin, ticketGrantingTicketCookieGenerator, ticketRegistry)

    @Bean
    fun alaDialect(alaTemplateClient: AlaTemplateClient) = AlaDialect(alaTemplateClient)

}