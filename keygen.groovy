#!/usr/bin/env groovy
/*
This script can be used to generate secure keys for the various CAS encryption and signing key parameters.
 */
@Grapes(
        @Grab(group='org.bitbucket.b_c', module='jose4j', version='0.7.12')
)
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.OctJwkGenerator

def size = 256
if (args.length == 0) {
    println "No size specified, using 256 bits as default"
} else {
    size = Integer.parseInt(args[0])
}

final String JSON_WEB_KEY = "k"
def octetKey = OctJwkGenerator.generateJwk(size)
def params = octetKey.toParams(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC)
println params.get(JSON_WEB_KEY).toString()