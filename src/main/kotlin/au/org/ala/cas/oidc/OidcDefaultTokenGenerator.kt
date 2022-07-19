package au.org.ala.cas.oidc

import au.org.ala.utils.logger
import org.apache.commons.lang3.tuple.Pair
import org.apereo.cas.CentralAuthenticationService
import org.apereo.cas.authentication.DefaultAuthenticationBuilder
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.oidc.OidcConstants
import org.apereo.cas.services.OidcRegisteredService
import org.apereo.cas.support.oauth.OAuth20Constants
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGenerator
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext
import org.apereo.cas.ticket.OAuth20Token
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory
import org.apereo.cas.util.function.FunctionUtils
import org.jooq.lambda.Unchecked
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

/**
 * This is like the OAuth20DefaultTokenGenerator except it can handle OIDC registered service scopes
 * and for regular OAuth Services it just removes all scopes since the OAuth Services don't contain a list
 * of scopes.
 */
class OidcDefaultTokenGenerator(
    accessTokenFactory: OAuth20AccessTokenFactory,
    deviceTokenFactory: OAuth20DeviceTokenFactory,
    deviceUserCodeFactory: OAuth20DeviceUserCodeFactory,
    refreshTokenFactory: OAuth20RefreshTokenFactory,
    centralAuthenticationService: CentralAuthenticationService,
    casProperties: CasConfigurationProperties
) : OAuth20DefaultTokenGenerator(accessTokenFactory, deviceTokenFactory, deviceUserCodeFactory, refreshTokenFactory, centralAuthenticationService, casProperties) {

    companion object {
        val LOGGER = logger()
    }

    @Throws(Exception::class)
    override fun generateAccessTokenOAuthGrantTypes(holder: AccessTokenRequestContext): Pair<OAuth20AccessToken, OAuth20RefreshToken?>? {
        LOGGER.debug("Creating access token for [{}]", holder.service)
        val registeredService = holder.registeredService
        val clientId = registeredService.clientId

        val scopes = determineValidScopes(registeredService, holder.scopes)


        val authnBuilder = DefaultAuthenticationBuilder
            .newInstance(holder.authentication)
            .setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC))
            .addAttribute(OAuth20Constants.GRANT_TYPE, holder.grantType.toString())
            .addAttribute(OAuth20Constants.SCOPE, scopes)
            .addAttribute(OAuth20Constants.CLIENT_ID, clientId)
        val requestedClaims = holder.claims.getOrDefault(OAuth20Constants.CLAIMS_USERINFO, HashMap())
        requestedClaims.forEach { (key: String?, value: Any?) ->
            authnBuilder.addAttribute(
                key,
                value
            )
        }
        val authentication = authnBuilder.build()
        LOGGER.debug("Creating access token for [{}]", holder)
        val ticketGrantingTicket = holder.ticketGrantingTicket
        val accessToken = accessTokenFactory.create(
            holder.service,
            authentication, ticketGrantingTicket, scopes,
            Optional.ofNullable(holder.token).map { obj: OAuth20Token -> obj.id }.orElse(null),
            clientId, holder.claims,
            holder.responseType, holder.grantType
        )
        LOGGER.debug("Created access token [{}]", accessToken)
        addTicketToRegistry(accessToken, ticketGrantingTicket)
        LOGGER.debug("Added access token [{}] to registry", accessToken)
        updateOAuthCode(holder, accessToken)
        val refreshToken = FunctionUtils.doIf(holder.isGenerateRefreshToken,
            Unchecked.supplier {
                generateRefreshToken(
                    holder,
                    accessToken
                )
            }
        ) {
            LOGGER.debug("Service [{}] is not able/allowed to receive refresh tokens", holder.service)
            null
        }.get()
        return Pair.of(accessToken, refreshToken)
    }

}