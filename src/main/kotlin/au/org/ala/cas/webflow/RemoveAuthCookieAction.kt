package au.org.ala.cas.webflow

import au.org.ala.utils.logger
import org.apereo.cas.web.cookie.CasCookieBuilder
import org.apereo.cas.web.support.WebUtils
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext

open class RemoveAuthCookieAction(
    val alaProxyAuthenticationCookieGenerator: CasCookieBuilder
) : AbstractAction() {

    companion object {
        val log = logger()
    }

    override fun doExecute(context: RequestContext?): Event {
        log.debug("RemoveAuthCookieAction running")
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context)
        alaProxyAuthenticationCookieGenerator.removeCookie(response)
        return success() // unnecessary?
    }

}