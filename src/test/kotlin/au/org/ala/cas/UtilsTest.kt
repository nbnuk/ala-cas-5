package au.org.ala.cas

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {

    @Test
    fun setSingleAttributeValue() {
        val map = mutableMapOf<String, List<Any?>>("a" to mutableListOf("b"), "c" to mutableListOf(""), "d" to mutableListOf<String>())
        map.setSingleAttributeValue("a", "c")
        map.setSingleAttributeValue("b", "b")
        map.setSingleAttributeValue("c", "d")
        map.setSingleAttributeValue("d", "e")

        assertEquals(listOf("c"), map["a"])
        assertEquals(listOf("b"), map["b"])
        assertEquals(listOf("d"), map["c"])
        assertEquals(listOf("e"), map["d"])
    }

    @Test
    fun getSingleStringAttribute() {
        val map = mutableMapOf<String, List<Any?>>("a" to mutableListOf("b"), "c" to mutableListOf(""), "d" to mutableListOf<String>())
        map.setSingleAttributeValue("a", mutableListOf("c"))
        map.setSingleAttributeValue("b", mutableListOf("b"))
        map.setSingleAttributeValue("c", mutableListOf("d"))
        map.setSingleAttributeValue("d", mutableListOf("e"))

        assertEquals("c", singleStringAttributeValue(map["a"]))
        assertEquals("b", singleStringAttributeValue(map["b"]))
        assertEquals("d", singleStringAttributeValue(map["c"]))
        assertEquals("e", singleStringAttributeValue(map["d"]))
    }
}