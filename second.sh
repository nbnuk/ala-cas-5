#./mvnw package -DskipTests=true
java -Xmx2g -Xms2g -Dcas.standalone.configurationDirectory=/data/cas2/config -Dala.password.properties=/data/cas2/config/pwe.properties -jar /home/bea18c/dev/github/Atlas/ala-cas-5/target/cas-exec.war
