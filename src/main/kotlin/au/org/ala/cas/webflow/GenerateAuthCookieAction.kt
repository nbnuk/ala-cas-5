package au.org.ala.cas.webflow

import au.org.ala.cas.stringAttribute
import au.org.ala.utils.logger
import org.apereo.cas.authentication.AuthenticationException
import org.apereo.cas.ticket.InvalidTicketException
import org.apereo.cas.ticket.registry.TicketRegistrySupport
import org.apereo.cas.web.cookie.CasCookieBuilder
import org.apereo.cas.web.support.WebUtils
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class GenerateAuthCookieAction(
    val ticketRegistrySupport: TicketRegistrySupport,
    val alaProxyAuthenticationCookieGenerator: CasCookieBuilder,
    val quoteCookieValue: Boolean,
    val encodeCookieValue: Boolean
) : AbstractAction() {

    companion object {
        val log = logger()
    }

    override fun doExecute(context: RequestContext): Event {

        log.debug("GenerateAuthCookieAction running")
        //
        // Create ALA specific cookie that any ALA web application can read
        //
        val ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context)

        if (!ticketGrantingTicket.isNullOrBlank()) {
            log.debug("Ticket-granting ticket found in the context is [{}]", ticketGrantingTicket)
            val authentication =
                this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket) ?: throw InvalidTicketException(
                    AuthenticationException("No authentication found for ticket $ticketGrantingTicket"),
                    ticketGrantingTicket
                )
            val email = authentication.stringAttribute("email") ?: authentication.principal.id ?: throw IllegalStateException("Principal id is missing?!")

            alaProxyAuthenticationCookieGenerator.addCookie(
                context.externalContext.nativeRequest as? HttpServletRequest,
                context.externalContext.nativeResponse as? HttpServletResponse,
                quoteValue(encodeValue(email))
            )
        } else {
            log.debug("Ticket-granting ticket ID is blank")
        }

        return success()
    }

    private fun quoteValue(value: String) = if (quoteCookieValue) "\"$value\"" else value
    // TODO Custom encoder that doesn't encode valid cookie characters
    private fun encodeValue(value: String) = if (encodeCookieValue) URLEncoder.encode(value, "UTF-8") else value
}