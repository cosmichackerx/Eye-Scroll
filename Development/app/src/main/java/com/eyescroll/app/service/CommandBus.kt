package com.eyescroll.app.service

import com.eyescroll.app.domain.model.GestureCommand
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

object CommandBus {
    private val latest = AtomicReference<GestureCommand?>(null)
    private val listeners = CopyOnWriteArrayList<(GestureCommand) -> Unit>()

    @Volatile
    var accessibilityConnected: Boolean = false

    fun publish(command: GestureCommand) {
        latest.set(command)
        listeners.forEach { listener ->
            runCatching { listener(command) }
        }
    }

    fun addListener(listener: (GestureCommand) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (GestureCommand) -> Unit) {
        listeners.remove(listener)
    }
}
