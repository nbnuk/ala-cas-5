SELECT CCSA.character_set_name FROM information_schema.`TABLES` T,
       information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` CCSA
WHERE CCSA.collation_name = T.table_collation
  AND T.table_schema = "emmet"
  AND T.table_name = "users";
  

var fixDate = function(e, i, c) {
  var expireAt = e.expireAt.replace(/\[\w+\/\w+\]$/, '');
  var date = new Date(Date.parse(expireAt));
  print(date)
  var javaParseFormat = date.toUTCString();
  e.expireAt = javaParseFormat;  
  c.save(e);
}

  
db.ticketGrantingTicketsCollection.find().forEach(function(e,i) { fixDate(e,i,db.ticketGrantingTicketsCollection) })
db.serviceTicketsCollection.find().forEach(function(e,i) { fixDate(e,i,db.serviceTicketsCollection) })
db.transientSessionTicketsCollection.find().forEach(function(e,i) { fixDate(e,i,db.transientSessionTicketsCollection) })
db.proxyGrantingTicketsCollection.find().forEach(function(e,i) { fixDate(e,i,db.proxyGrantingTicketsCollection) })
db.proxyTicketsCollection.find().forEach(function(e,i) { fixDate(e,i,db.proxyTicketsCollection) })
db.oauthAccessTokensCache.find().forEach(function(e,i) { fixDate(e,i,db.oauthAccessTokensCache) })
db.oauthCodesCache.find().forEach(function(e,i) { fixDate(e,i,db.oauthCodesCache) })
db.oauthDeviceTokensCache.find().forEach(function(e,i) { fixDate(e,i,db.oauthDeviceTokensCache) })
db.oauthDeviceUserCodesCache.find().forEach(function(e,i) { fixDate(e,i,db.oauthDeviceUserCodesCache) })
db.oauthRefreshTokensCache.find().forEach(function(e,i) { fixDate(e,i,db.oauthRefreshTokensCache) })

var fixTicketClass = function(e,i,c) {
  var newJson = e.json.replace('\"org.apereo.cas.ticket.registry.EncodedTicket\"', '\"org.apereo.cas.ticket.registry.DefaultEncodedTicket\"');
  //print(newJson);
  e.json = newJson;
  c.save(e);
}

db.ticketGrantingTicketsCollection.find().forEach(function(e,i) { fixTicketClass(e,i,db.ticketGrantingTicketsCollection) })
db.serviceTicketsCollection.find().forEach(function(e,i) { fixTicketClass(e,i,db.serviceTicketsCollection) })
db.transientSessionTicketsCollection.find().forEach(function(e,i) { fixTicketClass(e,i,db.transientSessionTicketsCollection) })
db.proxyGrantingTicketsCollection.find().forEach(function(e,i) { fixTicketClass(e,i,db.proxyGrantingTicketsCollection) })
db.proxyTicketsCollection.find().forEach(function(e,i) { fixTicketClass(e,i,db.proxyTicketsCollection) })
db.oauthAccessTokensCache.find().forEach(function(e,i) { fixTicketClass(e,i,db.oauthAccessTokensCache) })
db.oauthCodesCache.find().forEach(function(e,i) { fixTicketClass(e,i,db.oauthCodesCache) })
db.oauthDeviceTokensCache.find().forEach(function(e,i) { fixTicketClass(e,i,db.oauthDeviceTokensCache) })
db.oauthDeviceUserCodesCache.find().forEach(function(e,i) { fixTicketClass(e,i,db.oauthDeviceUserCodesCache) })
db.oauthRefreshTokensCache.find().forEach(function(e,i) { fixTicketClass(e,i,db.oauthRefreshTokensCache) })


db.ticketGrantingTicketsCollection.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.serviceTicketsCollection.find().update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.transientSessionTicketsCollection.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.proxyGrantingTicketsCollection.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.proxyTicketsCollection.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.oauthAccessTokensCache.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.oauthCodesCache.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.oauthDeviceTokensCache.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.oauthDeviceUserCodesCache.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })
db.oauthRefreshTokensCache.update({}, { $set: { "type": "org.apereo.cas.ticket.registry.DefaultEncodedTicket" } })

todo encode tickets?

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

