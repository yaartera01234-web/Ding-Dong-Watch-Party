package app.player.mpv

import android.content.Context
import android.view.Surface
import `is`.xyz.mpv.MPV
import `is`.xyz.mpv.MPVNode

/**
 * LEGACY ENGINE ADAPTER (temporary build for old devices).
 *
 * Exposes the same static `MPVLib` surface the app code uses with libmpvKt, but delegates to the
 * older `is.xyz.mpv.MPV` engine from `io.github.abdallahmehiz:mpv-android-lib:0.1.12`, whose mpv
 * build still runs on the older/weaker phones that crash with the new engine. The player code
 * (MPVView / MpvImpl) stays byte-identical apart from its import lines.
 */
object MPVLib {

    /** mpv_format ordinals from client.h, matching what the old JNI observeProperty expects. */
    object MpvFormat {
        const val MPV_FORMAT_NONE = 0
        const val MPV_FORMAT_FLAG = 1
        const val MPV_FORMAT_STRING = 2
        const val MPV_FORMAT_INT64 = 3
        const val MPV_FORMAT_DOUBLE = 4
    }

    /** mpv_event_id ordinals from client.h. */
    object MpvEvent {
        const val MPV_EVENT_START_FILE = 6
        const val MPV_EVENT_END_FILE = 7
    }

    /** The observer contract the rest of the app implements. */
    interface EventObserver {
        fun eventProperty(property: String)
        fun eventProperty(property: String, value: Long)
        fun eventProperty(property: String, value: Boolean)
        fun eventProperty(property: String, value: String)
        fun eventProperty(property: String, value: Double)
        fun event(eventId: Int)
    }

    private val mpv = MPV()
    private val wrappers = HashMap<EventObserver, MPV.EventObserver>()

    fun create(context: Context) = mpv.create(context)
    fun init() = mpv.init()
    fun destroy() = mpv.destroy()
    fun attachSurface(surface: Surface) = mpv.attachSurface(surface)
    fun detachSurface() = mpv.detachSurface()

    fun setOptionString(name: String, value: String) {
        mpv.setOptionString(name, value)
    }

    fun setPropertyString(name: String, value: String) = mpv.setPropertyString(name, value)
    fun setPropertyBoolean(name: String, value: Boolean) = mpv.setPropertyBoolean(name, value)
    fun setPropertyDouble(name: String, value: Double) = mpv.setPropertyDouble(name, value)
    fun setPropertyInt(name: String, value: Int) = mpv.setPropertyInt(name, value)

    fun getPropertyString(name: String): String? = mpv.getPropertyString(name)
    fun getPropertyDouble(name: String): Double? = mpv.getPropertyDouble(name)
    fun getPropertyBoolean(name: String): Boolean? = mpv.getPropertyBoolean(name)
    fun getPropertyInt(name: String): Int? = mpv.getPropertyInt(name)

    fun command(args: Array<String>) {
        mpv.command(*args)
    }

    fun observeProperty(name: String, format: Int) = mpv.observeProperty(name, format)

    fun addObserver(observer: EventObserver) {
        val wrapped = object : MPV.EventObserver {
            override fun eventProperty(property: String) = observer.eventProperty(property)
            override fun eventProperty(property: String, value: Long) = observer.eventProperty(property, value)
            override fun eventProperty(property: String, value: Boolean) = observer.eventProperty(property, value)
            override fun eventProperty(property: String, value: String) = observer.eventProperty(property, value)
            override fun eventProperty(property: String, value: Double) = observer.eventProperty(property, value)
            override fun eventProperty(property: String, value: MPVNode?) = Unit
            override fun event(eventId: Int, node: MPVNode?) = observer.event(eventId)
        }
        wrappers[observer] = wrapped
        mpv.addObserver(wrapped)
    }

    fun removeObserver(observer: EventObserver) {
        wrappers.remove(observer)?.let { mpv.removeObserver(it) }
    }
}
