package au.org.ala.cas.flyway

import au.org.ala.cas.AlaCasProperties
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.mysql.cj.jdbc.MysqlDataSource
import org.apereo.cas.configuration.CasConfigurationProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@AutoConfigureBefore(FlywayAutoConfiguration::class)
@EnableConfigurationProperties(AlaCasProperties::class, CasConfigurationProperties::class, FlywayProperties::class)
class AlaFlywayConfiguration {

    @Autowired
    lateinit var flywayProperties: FlywayProperties

    /**
     * This @Bean (or at least a single DataSource @Bean) is required for FlywayAutoConfiguration to execute,
     * even if Flyway is configured to create its own DataSource.
     */
    @FlywayDataSource
    @Qualifier("alaCasFlywayDataSource")
    fun alaCasFlywayDataSource(): DataSource {
        return MysqlDataSource().apply {
            setUrl(flywayProperties.url)
            user = flywayProperties.user
            password = flywayProperties.password
        }
//        val hc = HikariConfig()
//        hc.jdbcUrl = flywayProperties.url
//        hc.username = flywayProperties.user
//        hc.password = flywayProperties.password
//        return HikariDataSource(hc)
    }
}