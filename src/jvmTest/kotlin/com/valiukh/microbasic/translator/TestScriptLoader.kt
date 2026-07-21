package com.valiukh.microbasic.translator

private const val TEST_SCRIPTS_DIR = "test_scripts"

internal fun loadTestScript(scriptName: String): String {
    val path = "$TEST_SCRIPTS_DIR/$scriptName"
    val stream = Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
        ?: object {}.javaClass.classLoader?.getResourceAsStream(path)
        ?: error("Test script not found: $path")

    return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
}
