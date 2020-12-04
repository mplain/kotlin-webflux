package ru.mplain.kotlin.webflux.common

infix fun Any?.and(other: Any?) = listOfNotNull(this, other).joinToString(": ")