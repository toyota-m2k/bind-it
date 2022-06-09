package io.github.toyota32k.bindit

import io.github.toyota32k.utils.UtObservableCounter
import io.github.toyota32k.utils.UtObservableFlag
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ObservableFlagTest {
    @Before
    fun setup() {
    }

    @Test
    fun flagTest() {
        val flag = UtObservableFlag()
        assertFalse(flag.value)
        assertTrue(flag.set())
        assertFalse(flag.set())
        assertTrue(flag.reset())
        assertTrue(flag.set())

        var b = flag.ifSet(false) { true }
        assertFalse(b)
        flag.reset()
        b = flag.ifSet(false) { true }
        assertTrue(b)
        assertFalse(flag.value)

        b = false
        flag.ifSet { b = true }
        assertTrue(b)
        assertFalse(flag.value)

        var u = flag.closeableSetFlag()
        assertNotNull(u)
        assertTrue(flag.value)
        u?.close()
        assertFalse(flag.value)

        assertTrue(flag.set())
        u = flag.closeableSetFlag()
        assertNull(u)

        assertTrue(flag.reset())
        flag.closeableSetFlag().use {
            assertTrue(flag.value)
        }
        assertFalse(flag.value)
    }

    @Test
    fun counterTest() {
        val counter = UtObservableCounter()
        assertEquals(counter.count, 0)
        counter.set()
        assertEquals(counter.count, 1)
        counter.set()
        assertEquals(counter.count, 2)
        counter.reset()
        assertEquals(counter.count, 1)
        counter.onSet { ->
            assertEquals(counter.count, 2)
        }
        assertEquals(counter.count, 1)
        counter.withSetCounter {
            assertEquals(it, 2)
        }
        assertEquals(counter.count, 1)

        var u = counter.closeableSet()
        assertEquals(counter.count, 2)
        u.close()
        assertEquals(counter.count, 1)

        counter.closeableSet().use {
            assertEquals(counter.count, 2)
        }
        assertEquals(counter.count, 1)
    }
}