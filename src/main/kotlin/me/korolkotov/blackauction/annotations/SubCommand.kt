package me.korolkotov.blackauction.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(
    val commands: Array<String> = [],
    val subCommands: Array<String> = [],

    val permissionNode: String
)