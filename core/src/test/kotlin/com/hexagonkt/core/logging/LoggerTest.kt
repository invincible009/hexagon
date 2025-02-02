package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.junit.jupiter.api.Test
import kotlin.IllegalStateException
import kotlin.reflect.KClass
import kotlin.test.*

internal class LoggerTest {

    @Suppress("RedundantExplicitType", "UNUSED_VARIABLE") // Ignored for examples generation
    @Test fun loggerUsage() {
        // logger
        val classLogger: Logger = Logger(Runtime::class) // Logger for the `Runtime` class
        val instanceLogger: Logger = Logger(this::class) // Logger for this instance's class

        logger.info {
            """
            You can add a quick log without declaring a Logger with
            'com.hexagonkt.core.logging.logger'. It is a default logger created with a custom name
            (same as `Logger(LoggingManager.defaultLoggerName)`).
            """
        }

        classLogger.trace { "Message only evaluated if trace enabled" }
        classLogger.debug { "Message only evaluated if debug enabled" }
        classLogger.warn { "Message only evaluated if warn enabled" }
        classLogger.info { "Message only evaluated if info enabled" }

        val exception = IllegalStateException("Exception")
        classLogger.warn(exception) { "Warning with exception" }
        classLogger.error(exception) { "Error message with exception" }
        classLogger.warn(exception)
        classLogger.error(exception)
        classLogger.error { "Error without an exception" }

        // Logging level can be changed programmatically
        LoggingManager.setLoggerLevel(ERROR)
        LoggingManager.setLoggerLevel(classLogger::class, DEBUG)
        LoggingManager.setLoggerLevel("com.hexagonkt", INFO)
        // logger
    }

    /**
     * As the logger is only a facade, and it is hard to check outputs, the only check is that no
     * exceptions are thrown.
     */
    @Test fun `Messages are logged without errors using JUL`() {

        val logger = Logger(this::class)

        logger.trace { 42 }
        logger.debug { true }
        logger.info { 0.0 }
        logger.warn(RuntimeException())
        logger.error(RuntimeException())
        logger.warn { listOf(0, 1) }
        logger.error { mapOf(0 to 1, 2 to 3) }
        logger.warn(RuntimeException()) { 'c' }
        logger.error(RuntimeException()) { 0..100 }
        logger.warn(null) { assertNull(it) }
        logger.error(null) { assertNull(it) }
        logger.log(TRACE) { "message" }
        logger.log(ERROR, RuntimeException()) { 0..100 }
    }

    @Test fun `A logger for a custom name has the proper name`() {
        assert(Logger("name").name == "name")
        assert(Logger("name"::class).name == "kotlin.String")
    }

    @Test fun `A logger level can be changed`() {
        val l = Logger("l")

        l.setLoggerLevel(TRACE)
        assert(l.name == "l")
        assert(l.isTraceEnabled())
        assert(l.isDebugEnabled())
        assert(l.isInfoEnabled())
        assert(l.isWarnEnabled())
        assert(l.isErrorEnabled())

        l.setLoggerLevel(DEBUG)
        assert(l.name == "l")
        assertFalse(l.isTraceEnabled())
        assert(l.isDebugEnabled())
        assert(l.isInfoEnabled())
        assert(l.isWarnEnabled())
        assert(l.isErrorEnabled())

        l.setLoggerLevel(INFO)
        assert(l.name == "l")
        assertFalse(l.isTraceEnabled())
        assertFalse(l.isDebugEnabled())
        assert(l.isInfoEnabled())
        assert(l.isWarnEnabled())
        assert(l.isErrorEnabled())

        l.setLoggerLevel(WARN)
        assert(l.name == "l")
        assertFalse(l.isTraceEnabled())
        assertFalse(l.isDebugEnabled())
        assertFalse(l.isInfoEnabled())
        assert(l.isWarnEnabled())
        assert(l.isErrorEnabled())

        l.setLoggerLevel(ERROR)
        assert(l.name == "l")
        assertFalse(l.isTraceEnabled())
        assertFalse(l.isDebugEnabled())
        assertFalse(l.isInfoEnabled())
        assertFalse(l.isWarnEnabled())
        assert(l.isErrorEnabled())
    }

    @Test
    @DisabledInNativeImage
    fun `Invalid class name raises error`() {
        val kc = mockk<KClass<*>>()
        every { kc.qualifiedName } returns null
        val e = assertFailsWith<IllegalStateException> { Logger(kc) }
        assertEquals("Cannot get qualified name of type", e.message)
    }
}
