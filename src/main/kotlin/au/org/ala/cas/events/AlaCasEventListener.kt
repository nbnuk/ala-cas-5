package au.org.ala.cas.events

import au.org.ala.cas.alaUserId
import au.org.ala.cas.stringAttribute
import au.org.ala.utils.logger
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent
import org.apereo.services.persondir.IPersonAttributeDao
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import java.util.concurrent.ExecutorService
import javax.sql.DataSource

open class AlaCasEventListener(
    val dataSource: DataSource,
    val updateSql: String,
    val executorService: ExecutorService,
    val cachingAttributeRepository: IPersonAttributeDao //CachingPersonAttributeDaoImpl
) {

    companion object {
        val log = logger()
    }

    @EventListener
    open fun handleCasTicketGrantingTicketCreatedEvent(casTicketGrantingTicketCreatedEvent: CasTicketGrantingTicketCreatedEvent) {
        val authentication = casTicketGrantingTicketCreatedEvent.ticketGrantingTicket?.authentication
        log.debug("Handling CAS TGT created event for : {}", authentication)
        val userid = authentication?.alaUserId()
        val email = authentication?.stringAttribute("email")
        if (userid != null) {
            executorService.execute {
                try {
                    val template = JdbcTemplate(dataSource)
                    template.update(updateSql, userid)
//                    email?.let { cachingAttributeRepository.removeUserAttributes(it) }
                } catch (e: Exception) {
                    log.error("Couldn't update last login time for {} using SQL {}", userid, updateSql, e)
                }
            }
        }
    }

}