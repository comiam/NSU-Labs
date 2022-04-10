package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LoggerFactory : ReadOnlyProperty<Any?, Logger> {
    companion object {
        private fun <T> createLogger(clazz: Class<T>) = LoggerFactory.getLogger(clazz).apply {}
    }

    private var logger: Logger? = null

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        if (logger == null) {
            logger = createLogger(thisRef!!.javaClass)
        }
        return logger!!
    }
}