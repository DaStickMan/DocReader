package com.docreader.core.ports

import java.time.Instant

/** Fonte de tempo injetável para tornar os casos de uso determinísticos em teste. */
fun interface Clock {
    fun now(): Instant
}
