package au.org.ala.cas.session

//import org.springframework.beans.BeansException
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.boot.web.servlet.FilterRegistrationBean
//import org.springframework.context.ApplicationContext
//import org.springframework.context.ApplicationContextAware
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.session.ExpiringSession
//import org.springframework.session.SessionRepository
//import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices
//import org.springframework.session.web.http.*
//import org.springframework.util.ClassUtils
//import org.springframework.util.ObjectUtils
//import java.util.*
//import javax.annotation.PostConstruct
//import javax.servlet.DispatcherType
//import javax.servlet.ServletContext

/**
 * Attempts to apply SpringSessionRepositoryFilter to ERROR, FORWARD and INCLUDE
 * dispatchers.
 */
//@Configuration
//@AutoConfigureAfter(
//    DataSourceAutoConfiguration::class,
//    HazelcastAutoConfiguration::class,
//    JdbcTemplateAutoConfiguration::class,
//    MongoAutoConfiguration::class,
//    RedisAutoConfiguration::class,
//    AlaSpringSessionConfiguration::class
////)
//@ConditionalOnProperty(prefix = "spring.session", value = ["store-type"], havingValue = "redis")
//class AlaSpringSessionFilterConfiguration : ApplicationContextAware {
//
//    private val defaultHttpSessionStrategy = CookieHttpSessionStrategy()
//
//    private var usesSpringSessionRememberMeServices: Boolean = false
//
//    @Autowired(required = false)
//    var servletContext: ServletContext? = null
//    @Autowired(required = false)
//    var cookieSerializer: CookieSerializer? = null
//    @Autowired(required = false)
//    var httpSessionStrategy: HttpSessionStrategy = this.defaultHttpSessionStrategy
//
//    @PostConstruct
//    fun init() {
//        if (this.cookieSerializer != null) {
//            this.defaultHttpSessionStrategy.setCookieSerializer(this.cookieSerializer)
//        } else if (this.usesSpringSessionRememberMeServices) {
//            val cookieSerializer = DefaultCookieSerializer()
//            cookieSerializer.setRememberMeRequestAttribute(
//                SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR
//            )
//            this.defaultHttpSessionStrategy.setCookieSerializer(cookieSerializer)
//        }
//    }
//
//    @Bean
//    fun <S : ExpiringSession> springSessionRepositoryFilterRegistration(sessionRepository: SessionRepository<S>): FilterRegistrationBean {
//        val filter = SessionRepositoryFilter<S>(sessionRepository)
//
//        filter.setServletContext(this.servletContext)
//        if (this.httpSessionStrategy is MultiHttpSessionStrategy) {
//            filter.setHttpSessionStrategy(
//                this.httpSessionStrategy as MultiHttpSessionStrategy
//            )
//        } else {
//            filter.setHttpSessionStrategy(this.httpSessionStrategy)
//        }
//
//        return FilterRegistrationBean(filter).apply {
//            setName("sessionRepositoryFilter")
//            setDispatcherTypes(EnumSet.of(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR, DispatcherType.ASYNC, DispatcherType.REQUEST))
//            addUrlPatterns("*")
//            order = SessionRepositoryFilter.DEFAULT_ORDER
//            isAsyncSupported = true
//            isEnabled = true
//        }
//    }
//
//    @Throws(BeansException::class)
//    override fun setApplicationContext(applicationContext: ApplicationContext) {
//        if (ClassUtils.isPresent(
//                "org.springframework.security.web.authentication.RememberMeServices",
//                null
//            )
//        ) {
//            this.usesSpringSessionRememberMeServices = !ObjectUtils
//                .isEmpty(
//                    applicationContext
//                        .getBeanNamesForType(SpringSessionRememberMeServices::class.java)
//                )
//        }
//    }
//}