package au.org.ala.cas.oidc

import au.org.ala.utils.logger
import org.apereo.cas.oidc.OidcConstants
import org.apereo.cas.services.OidcRegisteredService
import org.apereo.cas.services.RegisteredService

val log = logger("oidcUtils")

fun determineValidScopes(registeredService: RegisteredService, requestedScopes: MutableCollection<String>): Collection<String> {
    return if (registeredService is OidcRegisteredService) {
        val serviceScopes: Set<String> = registeredService.scopes
        log.trace("Scopes assigned to service definition [{}] are [{}]", registeredService.name, serviceScopes)
        val retainedScopes = serviceScopes.ifEmpty {
            OidcConstants.StandardScopes.values().map { it.scope }.toSet()
        }
        requestedScopes.retainAll(retainedScopes)
        requestedScopes
    } else {
        emptySet()
    }
}