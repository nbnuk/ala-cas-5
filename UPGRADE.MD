Upgrade notes from CAS 6.5.6-1 to 6.5.6-2
====================================

1. Default attribute releases have been removed, must add attribute releases to CAS services entries in CAS Mgmt application.
  a. Upgrade existing service to support Attribute resolution:
```json
db.services.updateOne({serviceId: '^https?://.*'}, {'$set': { 'attributeReleasePolicy._class': 'org.apereo.cas.services.ReturnAllAttributeReleasePolicy' } });
```
  b. Delete the default IMAPS/HTTPS service provided by Apereo:
```json
db.services.deleteOne({serviceId: '^(https|imaps)://.*'})
```


Upgrade notes from CAS 5.3 to 6.5
=================================

1. Create a Mongo database for Spring Sessions, eg spring-sessions and assign the CAS mongo user readWrite access to it.

2. Update cas-service-registry in mongo: `db.services.update({ "multifactorPolicy.failureMode": "NOT_SET"}, { $set: { "multifactorPolicy.failureMode": "UNDEFINED" } }, { multi: true });`
3. Remove existing tickets in mongo (optionally, drop collections as well):
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

4. Generic OIDC callback URLs changed from v5 to v6, need to update your OIDC provider to use https://<host>/cas/login/<oidc_client_name>
5. Health checks changed to `/<app>/actuator/health`
6. Drop this index from the services collection:
```json
	{
		"v" : 2,
		"unique" : true,
		"key" : {
			"serviceId" : 1
		},
		"name" : "serviceId_1",
		"ns" : "cas-service-registry.services"
	},

```
