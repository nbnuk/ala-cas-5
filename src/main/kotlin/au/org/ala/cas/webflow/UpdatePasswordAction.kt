package au.org.ala.cas.webflow

import au.org.ala.cas.AlaCasProperties
import au.org.ala.cas.alaUserId
import au.org.ala.cas.booleanAttribute
import au.org.ala.utils.logger
import org.apereo.cas.authentication.credential.UsernamePasswordCredential
import org.apereo.cas.web.support.WebUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext
import javax.sql.DataSource

class UpdatePasswordAction(
    val alaCasProperties: AlaCasProperties,
    val passwordEncoder: PasswordEncoder,
    dataSource: DataSource,
    transactionManager: PlatformTransactionManager
) : AbstractAction() {

    companion object {
        val log = logger()
    }

    val transactionTemplate = TransactionTemplate(transactionManager)
    val template = NamedParameterJdbcTemplate((transactionManager as? DataSourceTransactionManager)?.dataSource ?: dataSource)

    override fun doExecute(context: RequestContext): Event {
        val credential = WebUtils.getCredential(context)
        val authentication = WebUtils.getAuthentication(context)
        val userid = authentication.alaUserId()
        val legacyPassword = authentication.booleanAttribute("legacyPassword") ?: false
        if (credential != null && credential is UsernamePasswordCredential && !credential.password.isNullOrBlank() && legacyPassword && userid != null) {
            log.info("Upgrading legacy password for {} ({})", credential.username, userid)
            val params = mapOf(
                "userid" to userid,
                "password" to passwordEncoder.encode(credential.password)
            )
            transactionTemplate.execute { status ->
                try {
                    alaCasProperties.userCreator.jdbc.updatePasswordSqls.forEach { sql ->
                        val rowsUpdated = template.update(sql, params)
                        if (rowsUpdated == 0) {
                            log.warn("SQL {} with params {} returned 0 updated rows", sql, params)
                        }
                    }
                } catch (e: Exception) {
                    log.warn("Couldn't update password for {} ({})", credential.username, userid, e)
                    status.setRollbackOnly()
                }
            }
        }
        return success()
    }
}