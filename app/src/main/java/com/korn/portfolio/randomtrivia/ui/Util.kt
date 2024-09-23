package com.korn.portfolio.randomtrivia.ui

fun hhmmssFrom(second: Int): String {
    fun format(value: Int): String = if (value < 10) "0$value" else value.toString()
    val ss = format(second % 60)
    val mm = format((second / 60) % 60)
    val hh = format(second / 3600)
    return if (hh.toInt() < 100) "$hh:$mm:$ss" else ">100 hrs"
}