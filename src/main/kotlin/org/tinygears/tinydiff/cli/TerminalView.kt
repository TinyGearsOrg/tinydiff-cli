package org.tinygears.tinydiff.cli

import java.lang.StringBuilder

interface TerminalView {
    fun updateSize(width: Int, height: Int)
    fun drawContent(builder: StringBuilder)
    fun drawCursor(builder: StringBuilder)
    fun getStatusMessage(): String
    fun handleKey(key: Int)
}