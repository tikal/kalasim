package org.kalasim.animation

import kotlinx.coroutines.*
import org.kalasim.*
import org.kalasim.misc.AmbiguousDuration
import org.kalasim.misc.DependencyContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


fun <T : Environment> T.startSimulation(tickMillis: Duration = 50.milliseconds) {
    apply {
        ClockSync(tickDuration = tickMillis, syncsPerTick = 10)

        dependency { AsyncAnimationStop() }

        CoroutineScope(Dispatchers.Default).launch {
            DependencyContext.setKoin(getKoin())
            run()
        }
    }
}

fun <K, V> Map<K, V>.cmeAvoidingCopy(): Map<K, V> {
    while(true) {
        try {
            return toMap()
        } catch(_: ConcurrentModificationException) {
        }
    }
}

fun <K> List<K>.cmeAvoidingCopy(): List<K> {
    while(true) {
        try {
            return toList()
        } catch(_: ConcurrentModificationException) {
        }
    }
}


fun <K, V> Map<K, V>.asyncCopy(): Map<K, V> {
    while(true) {
        try {
            return toMap()
        } catch(_: ConcurrentModificationException) {
        }
    }
}

fun <T> Set<T>.asyncCopy(): Set<T> {
    while(true) {
        try {
            return toSet()
        } catch(_: ConcurrentModificationException) {
        }
    }
}


fun <T> List<T>.asyncCopy(): List<T> {
    while(true) {
        try {
            return toMutableList()
        } catch(_: ConcurrentModificationException) {
        }
    }
}

fun <T> cmeGuard(function: () -> List<T>): List<T> {
    while(true) {
        try {
            return function().toMutableList()
        } catch(_: ConcurrentModificationException) {
        } catch(_: NoSuchElementException) {
        }
    }
}


class AsyncAnimationStop(val rate: Double = 1.0) : Component() {
    private var stop = false

    fun stop() {
        stop = true
    }

    @OptIn(AmbiguousDuration::class)
    override fun repeatedProcess() = sequence {
        if(stop) {
            stopSimulation()
        }

        hold(env.asDuration(1 / rate))
    }
}