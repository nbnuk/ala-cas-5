package au.org.ala.cas

import org.apereo.cas.authentication.Authentication
import org.apereo.cas.authentication.principal.Principal
import java.util.LinkedList

fun Authentication.alaUserId(): Long? = alaUserId(this.principal?.attributes?.get("userid"))
fun Authentication.stringAttribute(name: String): String? = this.principal.stringAttribute(name)
fun Authentication.booleanAttribute(name: String): Boolean? = this.principal.booleanAttribute(name)

fun Principal?.stringAttribute(name: String): String? = singleStringAttributeValue(this?.attributes?.get(name))
fun Principal?.booleanAttribute(name: String): Boolean? = singleBooleanAttributeValue(this?.attributes?.get(name))

fun alaUserId(value: Any?): Long? = singleLongAttributeValue(value)

fun singleLongAttributeValue(value: Any?): Long? = when (value) {
    is Int -> value.toLong()
    is Long -> value
    is String -> value.toLong()
    is Collection<*> -> singleLongAttributeValue(value.firstOrNull())
    is Array<*> -> singleLongAttributeValue(value.firstOrNull())
    else -> null
}

fun singleStringAttributeValue(value: Any?): String? = when (value) {
    is String -> value
    is Collection<*> -> singleStringAttributeValue(value.firstOrNull())
    is Array<*> -> singleStringAttributeValue(value.firstOrNull())
    else -> value?.toString()
}

fun singleBooleanAttributeValue(value: Any?): Boolean? = when(value) {
    is Boolean -> value
    is String -> setOf("1", "true", "t", "yes", "y").contains(value.lowercase())
    is Number -> value == 1
    is Collection<*> -> singleBooleanAttributeValue(value.firstOrNull())
    is Array<*> -> singleBooleanAttributeValue(value.firstOrNull())
    else -> null
}

fun MutableMap<String, List<Any?>>.setSingleAttributeValue(name: String, value: Any?) {
    when(val oldValue = this[name]) {
        is MutableList<*> -> if (oldValue.size > 0) (oldValue as MutableList<Any?>)[0] = value else (oldValue as MutableCollection<Any?>).add(value)
        null -> {
            val list = LinkedList<Any?>()
            list.add(value)
            this[name] = list
        }
    }
}