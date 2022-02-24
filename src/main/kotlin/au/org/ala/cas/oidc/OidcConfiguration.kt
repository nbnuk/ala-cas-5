package au.org.ala.cas.oidc

import org.apereo.cas.CentralAuthenticationService
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGenerator
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ScopedProxyMode


@Configuration
@EnableConfigurationProperties(CasConfigurationProperties::class)
class OidcConfiguration {

//    @ConditionalOnMissingBean(name = ["oauthTokenGenerator"])
    @Bean("oauthTokenGenerator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    fun oauthTokenGenerator(
        @Qualifier("defaultDeviceUserCodeFactory") defaultDeviceUserCodeFactory: OAuth20DeviceUserCodeFactory,
        @Qualifier("defaultDeviceTokenFactory") defaultDeviceTokenFactory: OAuth20DeviceTokenFactory,
        @Qualifier("defaultRefreshTokenFactory") defaultRefreshTokenFactory: OAuth20RefreshTokenFactory,
        @Qualifier("defaultAccessTokenFactory") defaultAccessTokenFactory: OAuth20AccessTokenFactory,
        @Qualifier(CentralAuthenticationService.BEAN_NAME) centralAuthenticationService: CentralAuthenticationService,
        casProperties: CasConfigurationProperties
    ): OAuth20TokenGenerator {
        return OidcDefaultTokenGenerator(
            defaultAccessTokenFactory, defaultDeviceTokenFactory,
            defaultDeviceUserCodeFactory, defaultRefreshTokenFactory,
            centralAuthenticationService, casProperties
        )
    }
}