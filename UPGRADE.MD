Upgrade notes from CAS 5.3 to 6.4
=================================

1. Spring Boot 2 application.yml config key updates:
    - flyway.* -> spring.flyway.*
    - server.context-path -> server.servlet.context-path
    - TODO More to come
2. Fix application.yml config keys:
    - endpoints...
    - spring.endpoints?

3. Update cas-service-registry in mongo: `db.services.update({ "multifactorPolicy.failureMode": "NOT_SET"}, { $set: { "multifactorPolicy.failureMode": "UNDEFINED" } }, { multi: true });`
4. Remove existing tickets in mongo (optionally, drop collections as well):
```javascript
    db.ticketGrantingTicketsCollection.remove({})
    db.serviceTicketsCollection.remove({})
    db.transientSessionTicketsCollection.remove({})
    db.proxyGrantingTicketsCollection.remove({})
    db.proxyTicketsCollection.remove({})
    db.oauthAccessTokensCache.remove({})
    db.oauthCodesCache.remove({})
    db.oauthDeviceTokensCache.remove({})
    db.oauthDeviceUserCodesCache.remove({})
    db.oauthRefreshTokensCache.remove({})
```
This is to prevent the ticket cleaner from crashing.  To keep tickets, need to figure out the change in date format and
write a script to update it on all tickets.
6. Update log4j config, make Spring Boot logger sync instead of async so that startup failures are logged.