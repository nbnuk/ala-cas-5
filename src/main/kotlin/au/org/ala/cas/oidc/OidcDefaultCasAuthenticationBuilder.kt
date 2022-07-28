package au.org.ala.cas.oidc

import au.org.ala.utils.logger
import org.apache.commons.lang3.StringUtils
import org.apereo.cas.authentication.Authentication
import org.apereo.cas.authentication.CoreAuthenticationUtils
import org.apereo.cas.authentication.DefaultAuthenticationBuilder
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData
import org.apereo.cas.authentication.principal.PrincipalFactory
import org.apereo.cas.authentication.principal.Service
import org.apereo.cas.authentication.principal.ServiceFactory
import org.apereo.cas.authentication.principal.WebApplicationService
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.support.oauth.OAuth20Constants
import org.apereo.cas.support.oauth.authenticator.OAuth20DefaultCasAuthenticationBuilder
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter
import org.apereo.cas.support.oauth.services.OAuthRegisteredService
import org.apereo.cas.support.oauth.util.OAuth20Utils
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.BasicUserProfile
import org.pac4j.core.profile.UserProfile
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * This is like the OAuth20DefaultCasAuthenticationBuilder except it can handle OIDC registered service scopes
 * and for regular OAuth Services it just removes all scopes since the OAuth Services don't contain a list
 * of scopes.
 */
class OidcDefaultCasAuthenticationBuilder(
    principalFactory: PrincipalFactory?,
    webApplicationServiceServiceFactory: ServiceFactory<WebApplicationService>?,
    scopeToAttributesFilter: OAuth20ProfileScopeToAttributesFilter?,
    casProperties: CasConfigurationProperties?
) : OAuth20DefaultCasAuthenticationBuilder(
    principalFactory,
    webApplicationServiceServiceFactory,
    scopeToAttributesFilter,
    casProperties
) {

    companion object {
        val log = logger()
    }

    override fun build(
        profile: UserProfile,
        registeredService: OAuthRegisteredService,
        context: WebContext,
        service: Service?
    ): Authentication? {
        val attrs = HashMap(profile.attributes)
        val profileAttributes = CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(attrs)
        val newPrincipal = principalFactory.createPrincipal(profile.id, profileAttributes)
        log.debug(
            "Created final principal [{}] after filtering attributes based on [{}]",
            newPrincipal,
            registeredService
        )
        val authenticator = profile.javaClass.canonicalName
        val metadata = BasicCredentialMetaData(BasicIdentifiableCredential(profile.id))
        val handlerResult =
            DefaultAuthenticationHandlerExecutionResult(authenticator, metadata, newPrincipal, ArrayList(0))
//        val scopes = OAuth20Utils.getRequestedScopes(context)
        val scopes = determineValidScopes(registeredService, OAuth20Utils.getRequestedScopes(context))

        val state = context.getRequestParameter(OAuth20Constants.STATE)
            .map { obj: String? ->
                java.lang.String.valueOf(
                    obj
                )
            }
            .or {
                OAuth20Utils.getRequestParameter(
                    context,
                    OAuth20Constants.STATE
                )
            }
            .orElse(StringUtils.EMPTY)
        val nonce = context.getRequestParameter(OAuth20Constants.NONCE)
            .map { obj: String? ->
                java.lang.String.valueOf(
                    obj
                )
            }
            .or {
                OAuth20Utils.getRequestParameter(
                    context,
                    OAuth20Constants.NONCE
                )
            }
            .orElse(StringUtils.EMPTY)
        log.debug(
            "OAuth [{}] is [{}], and [{}] is [{}]",
            OAuth20Constants.STATE,
            state,
            OAuth20Constants.NONCE,
            nonce
        )
        val builder = DefaultAuthenticationBuilder.newInstance()
        if (profile is BasicUserProfile) {
            val authenticationAttributes = profile.authenticationAttributes
            builder.addAttributes(authenticationAttributes)
        }
        builder
            .addAttribute("permissions", LinkedHashSet(profile.permissions))
            .addAttribute("roles", LinkedHashSet(profile.roles))
            .addAttribute("scopes", scopes)
            .addAttribute(OAuth20Constants.STATE, state)
            .addAttribute(OAuth20Constants.NONCE, nonce)
            .addAttribute(OAuth20Constants.CLIENT_ID, registeredService.clientId)
            .addCredential(metadata)
            .setPrincipal(newPrincipal)
            .setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC))
            .addSuccess(profile.javaClass.canonicalName, handlerResult)
        context.getRequestParameter(OAuth20Constants.ACR_VALUES)
            .ifPresent { value: String? ->
                builder.addAttribute(
                    OAuth20Constants.ACR_VALUES,
                    value
                )
            }
        return builder.build()
    }

}