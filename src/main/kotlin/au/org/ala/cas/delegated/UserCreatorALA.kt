package au.org.ala.cas.delegated

import au.org.ala.utils.logger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.SqlOutParameter
import org.springframework.jdbc.core.SqlParameter
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.sql.Types
import javax.sql.DataSource

class UserCreatorALA(
    val dataSource: DataSource,
    val createUserProcedure: String,
    val userCreatePassword: String,
    val passwordEncoder: PasswordEncoder = NoOpPasswordEncoder.getInstance()
) : UserCreator {

    companion object {
        /** Log instance.  */
        private val log = logger()
        private val EMAIL = "email"
        private val FIRST_NAME = "firstname"
        private val LAST_NAME = "lastname"
        private val PASSWORD = "password"
        private val ORGANISATION = "organisation"
        private val CITY = "city"
        private val STATE = "state"
        private val COUNTRY = "country"
        private val USER_ID = "user_id"
    }

    private val jdbcTemplate = JdbcTemplate(dataSource)

    override fun createUser(email: String, firstName: String, lastName: String): Long? {
        log.debug("createUser: {} {} {}", email, firstName, lastName)

        val password = passwordEncoder.encode(userCreatePassword)

        // Have to provide stored procedure in/out params as the MySQL 8 driver + RDS doesn't provide access to the
        // information schema or something.
        val call = SimpleJdbcCall(jdbcTemplate)
            .withProcedureName(createUserProcedure)
            .withoutProcedureColumnMetaDataAccess()
            .useInParameterNames(EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, ORGANISATION, CITY, STATE, COUNTRY)
            .declareParameters(
                SqlParameter(EMAIL, Types.VARCHAR),
                SqlParameter(FIRST_NAME, Types.VARCHAR),
                SqlParameter(LAST_NAME, Types.VARCHAR),
                SqlParameter(PASSWORD, Types.VARCHAR),
                SqlParameter(ORGANISATION, Types.VARCHAR),
                SqlParameter(CITY, Types.VARCHAR),
                SqlParameter(STATE, Types.VARCHAR),
                SqlParameter(COUNTRY, Types.VARCHAR),
                SqlOutParameter(USER_ID, Types.BIGINT)
            )

        val inParams = MapSqlParameterSource()
            .addValue(EMAIL, email.toLowerCase())
            .addValue(FIRST_NAME, firstName)
            .addValue(LAST_NAME, lastName)
            .addValue(PASSWORD, password)
            .addValue(ORGANISATION, "")
            .addValue(CITY, "")
            .addValue(STATE, "")
            .addValue(COUNTRY, "")

        val result = call.execute(inParams)

        val userId = result[USER_ID]

        log.debug("createUser created user id: {}", userId)

        return userId as? Long ?: (userId as? Int)?.toLong() ?: null.also {
            log.warn("Couldn't extract userId from {}", result)
        }
    }

}