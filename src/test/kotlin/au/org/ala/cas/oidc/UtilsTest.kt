package au.org.ala.cas.oidc

import org.apereo.cas.services.OidcRegisteredService
import org.apereo.cas.support.oauth.services.OAuthRegisteredService
import org.junit.Test
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun testScopeResolution() {
        val service = OidcRegisteredService()
        service.scopes = setOf("openid", "test", "test2")
        val scopes = determineValidScopes(service, mutableListOf("openid", "test", "invalid"))
        assertEquals(listOf("openid", "test"), scopes)
    }

    @Test
    fun testOauthScopeDenial() {
        val service = OAuthRegisteredService()
//            service.scopes = setOf("openid", "test", "test2")
        val scopes = determineValidScopes(service, mutableListOf("openid", "test", "invalid"))
        assertEquals(emptySet(), scopes)

    }
}