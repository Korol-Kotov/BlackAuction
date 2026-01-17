package me.korolkotov.blackauction.load

interface LoadManagerInterface<T> {
    fun getInstance(): T

    fun initialize()
    fun terminate() {}
    fun reload() {}
}