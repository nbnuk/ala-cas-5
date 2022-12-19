ALA CAS 5.3 [![Build Status](https://travis-ci.org/nbnuk/ala-cas-5.svg?branch=master-nbn)](https://travis-ci.org/AtlasOfLivingAustralia/ala-cas-5)
============================

The ALA CAS 5.3 deployment is based off the generic CAS WAR overlay.

CAS documentation is available [here](https://apereo.github.io/cas/5.3.x/index.html)

In addition to upgrading the core CAS components this upgrade hopes to minimise overrides of existing CAS classes as 
used in previous versions of ALA CAS.  Customising these classes by overriding the classes directly complicates future
updates, complicates maintenance and risks introducing bugs.  Some alternative strategies include:
 
 - providing alternative versions of Spring beans (many CAS components use `@ConditionalOnMissingBean`)
 - delegation to the original class (see Kotlin built in `delegates to` feature)

# Configuration

CAS 5.0+ uses the Spring Boot configuration mechanism.  This means that it will load `application.yml` (among other 
things) from the classpath *and then* use any properties provided by `Spring Cloud` (which at the moment is set to use 
`native` aka the local filesystem).  The upshot of this, is that many properties are already set or have a sensible 
default in `src/main/resources/application.yml` and won't need to be added to the external config file.

The `etc` directory contains the configuration files and directories that need to be copied to `/etc/cas/config`.

By default, CAS will load properties from `/etc/cas/config` but can be changed by specifying 
`-Dcas.standalone.config=/data/cas/config` or by adding in `src/main/resources/bootstrap.properties`.

`application.yml` **will** require some customisation:
 
  - `jndi.hikari[0]` should have `url`, `user` and `password` set for the database connection to the CAS user database.
  - `flyway` should have the same `url`, `user` and `password` set for the same database connection as `jndi.hikari[0]`.
    - By default flyway is configured to be run against a pre-populated DB from ala-cas 2.0.
    - If that's not the case, set `flyway.baseline-on-migrate` to `false`, this will setup the database from scratch.
  - `cas.ticket.registry.jpa` should have `url`, `user` and `password` set for the ticket database.  For the first run 
    of each version `ddl-auto` should be set to `update` to get the latest changes from JPA (or run the provided SQL file to upgrade a CAS 4 JPA ticket registry to CAS 5).
  - `cas.authn.pac4j.facebook|google|twitter` will require id and secret to be set.

**NOTE:** The MySQL driver used by this CAS version is 5.1.43.  This DB driver requires that the server returns a timezone
in the form `Australia/Sydney`, whereas the MySQL versions available on Ubuntu 16.04 will tell the client `AEST` (which
causes an Exception in the client).  The simplest fix is to override the server timezone in the JDBC URL by appending 
`?serverTimezone=Australia/Sydney` to the URL.

*IF USING MYSQL Connector 6.0+* note that the `UserCreatorALA` uses Spring JDBC's `SimpleJdbcCall` to execute a stored procedure.
Spring JDBC makes some assumptions about the behaviour of the JDBC driver which [changed in `mysql-connector-java` 6.0+](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-properties-changed.html)
and as such the following driver properties are also required:
 - `nullCatalogMeansCurrent=true`
 - `nullNamePatternMatchesAll=true`

## Legacy passwords

CAS no longer provides an MD5 type password encoder.  To that end a custom password encoder is used by the system, 
however, due to being constructed outside of Spring it does not have access to the Spring properties
mechanism.  An additional config file must be placed at `/etc/cas/config/pwe.properties` (the location can be specified with `-Dala.password.properties` with two attributes:

  - md5.secret
  - md5.base64Encode
  
These correspond to the values used in ALA CAS 2.0 installs.

## Signing keys

Ensure that the keys are set for the following properties.  If left blank, then CAS will generate new keys for
each property and print them to the log.  After the first run, copy these values into `application.yml` and / or the
Ansible inventory for the CAS deployment.

**NOTE** the Ticket Granting Cookie crypto is disabled by default in ALA CAS as it requires the same IP and user agent which can be
confusing for users whose IP address changes regularly.  You may re-enable TGC crypto by setting `cas.tgc.crypto.enabled=true`. 

 - ~~Ticket-granting Cookie encryption (cas.tgc.crypto.encryption.key)~~
 - ~~Ticket-granting Cookie signing (cas.tgc.crypto.encryption.signing.key)~~
 - Webflow cookie encryption (cas.webflow.crypto.signing.key)
 - Webflow cookie signing (cas.webflow.crypto.encryption.key)
 - Ticket registry encryption (cas.ticket.registry.jpa.crypto.signing.key)
 - Ticket registry signing (cas.ticket.registry.jpa.crypto.encryption.key)
 
## JNDI Datasources

**NOTE** that JNDI appears to be the only way to share a connection pool between various JDBC based CAS sub systems.  In 
the default ALA CAS configuration a datasource is used to share the same connection pool between:
 
 - the bcrypt password query
 - legacy password query
 - attribute repository query
 - inserting a new user record
 - monitoring

If using the executable JAR (with embedded Tomcat) to run ALA CAS, then a Hikari JNDI datasource will be automatically
created from `application.yml` using the properties defined in `au.org.ala.cas.jndi.JndiConfigurationProperties`.
The code that enables the naming context in the embedded Tomcat instance is located in 
`au.org.ala.cas.jndi.ServletContextConfig`.

If, however, CAS is deployed to a regular Tomcat instance, you need to create the JNDI datasource manually in Tomcat.
For example, you can use `META-INF/context.xml` as a base, place it in `$TOMCAT_BASE/conf/Catalina/localhost/cas/context.xml`
and edit as necessary.  Then add the JARs for Hikari CP and the MySQL driver to `$TOMCAT_BASE/lib`.  Finally, ensure that
the datasource name is set in each instance in the application configuration (eg `application.yml`).  The datasource name
is `java:comp/env/$name` where `$name` is the `name` attribute on the `<Resource>` tag and the properties to set are:

 - ala.userCreator.jdbc.dataSourceName
 - cas.authn.jdbc.query[0].dataSourceName
 - cas.authn.jdbc.query[1].dataSourceName
 - cas.auth.attributeRepository.jdbc[0].dataSourceName
 - cas.monitor.jdbc.dataSourceName

## Database Migrations

ALA CAS is setup to use [Flyway](https://flywaydb.org/) to handle database updates.  Migrations are stored in 
`src/main/resources/db/migration/`.

The simple version of how this works is that `.sql` files starting with `V#__` are run in numeric order, 
with a table in the DB (`schema_version`) tracking which versions have been applied.  Files that begin `R__##` are 
"repeatable" migrations that can run multiple times (eg for Views or Stored Procedures), these are run in alphabetical 
order and will be re-run anytime the file changes.

**NOTE** *Always* test migrations before running them on the production database.  *TODO* Investigate how to run 
migrations from the command line.

## Attributes

ALA CAS 5 adds additional attributes to the CAS assertion compared to previous versions.  It adds `inetOrgPerson` style 
versions of `uid`, `givenName`, `sn`, `affiliation` and `mail`.  It also provides the users roles as multiple values 
instead of a comma separated list because by the generic (non-ALA) CAS client expects role attributes to be provided in
a list, so from the ALA CAS 5 release the `authority` attribute is effectively deprecated and future versions of ALA CAS 
Client can remove the custom `AlaHttpServletWrapper` filter. 

## Spring Boot @Configuration

**NOTE** `@Configuration` annotated classes must be listed in `src/main/resources/META-INF/spring.factories` to be found by
Spring now.

## TODO

A non-exhaustive list of stuff and/or things:

 - [x] Add ALA UI
    - [x] Enable remember me
 - [x] Add `ALA-Auth` cookie to [Web Flow](https://apereo.github.io/cas/5.3.x/installation/Webflow-Customization.html)
 - [x] Add [delegated authentication](https://apereo.github.io/cas/5.3.x/integration/Delegate-Authentication.html) (Facebook, Twitter, Google)
 - [ ] Investigate [monitoring options](https://apereo.github.io/cas/5.3.x/installation/Monitoring-Statistics.html)
 - [x] Create a [CAS management server](https://github.com/apereo/cas-services-management-overlay) application.
 - [x] Move the Service Registry out of `src/main/resources/services` and somewhere the CAS management web app can access them
 - [x] Enable OpenID Connect server
 - [ ] -Enable password management?-
 - [x] Update user details to include password type changes
 - [x] [High availability](https://apereo.github.io/cas/5.2.x/planning/High-Availability-Guide.html)

# Build

To see what commands are available to the build script, run:

```bash
./build.sh help
```

To package the final web application, run:

```bash
./build.sh package
```

To update `SNAPSHOT` versions run:

```bash
./build.sh package -U
```

# Deployment

- Create a keystore file `thekeystore` under `/etc/cas`. Use the password `changeit` for both the keystore and the key/certificate entries.
- Ensure the keystore is loaded up with keys and certificates of the server.

On a successful deployment via the following methods, CAS will be available at:

* `http://cas.server.name:8080/cas`
* `https://cas.server.name:8443/cas`

## Executable WAR

Run the CAS web application as an executable WAR.

```bash
./build.sh run
```

## Spring Boot

Run the CAS web application as an executable WAR via Spring Boot. This is most useful during development and testing.

```bash
./build.sh bootrun
```

### Warning!

Be careful with this method of deployment. `bootRun` is not designed to work with already executable WAR artifacts such that CAS server web application. YMMV. Today, uses of this mode ONLY work when there is **NO OTHER** dependency added to the build script and the `cas-server-webapp` is the only present module. See [this issue](https://github.com/apereo/cas/issues/2334) and [this issue](https://github.com/spring-projects/spring-boot/issues/8320) for more info.


## Spring Boot App Server Selection

There is an app.server property in the `pom.xml` that can be used to select a spring boot application server.
It defaults to `-tomcat` but `-jetty` and `-undertow` are supported. 
It can also be set to an empty value (nothing) if you want to deploy CAS to an external application server of your choice.

```xml
<app.server>-tomcat<app.server>
```

## Windows Build

If you are building on windows, try `build.cmd` instead of `build.sh`. Arguments are similar but for usage, run:  

```
build.cmd help
```
## External

Deploy resultant `target/cas.war`  to a servlet container of choice.

## Command Line Shell

Invokes the CAS Command Line Shell. For a list of commands either use no arguments or use `-h`. To enter the interactive shell use `-sh`.

```bash
./build.sh cli
```
