package org.tinygears.tinydiff.cli

import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.InfoCmp
import org.jline.utils.NonBlockingReader
import java.io.PrintWriter
import java.lang.StringBuilder

class TerminalScreen : AutoCloseable {
    private val terminal: Terminal = TerminalBuilder.terminal()
    private val writer:   PrintWriter

    private var height: Int
    private var width:  Int

    private var activeView: TerminalView? = null

    init {
        terminal.enterRawMode()
        writer = terminal.writer()

        terminal.handle(Terminal.Signal.WINCH) {
            width  = terminal.width
            height = terminal.height

            refreshScreen()
        }

        width  = terminal.width
        height = terminal.height
    }

    fun setView(view: TerminalView) {
        activeView = view
    }

    fun show() {
        val reader = terminal.reader()
        while (true) {
            refreshScreen()
            val key = readKey(reader)
            if (key == 'q'.code) {
                break
            } else {
                activeView?.handleKey(key)
            }
        }
    }

    private fun refreshScreen() {
        val content = StringBuilder()

        // hide cursor while printing content.
        content.append("\u001b[?25l")

        drawScreen(content)
        // show cursor again.
        content.append("\u001b[?25h")

        writer.print(content)
        writer.flush()
    }

    private fun drawScreen(builder: StringBuilder) {
        // move cursor to top left.
        builder.append("\u001b[H")

        activeView?.updateSize(width, height - 1)
        activeView?.drawContent(builder)

        drawStatusBar(builder)

        activeView?.drawCursor(builder)
    }

    private fun drawStatusBar(builder: StringBuilder) {
        builder.apply {
            append("\u001b[7m")
            val statusMessage = activeView?.getStatusMessage() ?: "No view loaded"
            append(statusMessage)
            append(" ".repeat(0.coerceAtLeast(width - statusMessage.length)))
            append("\u001b[0m");
        }
    }

    private fun readKey(reader: NonBlockingReader): Int {
        val key = reader.read(1000)
        return when (key.toChar()) {
            '\u001b' -> {
                val nextKey = reader.read()
                when (nextKey.toChar()) {
                    '[' -> {
                        val yetAnotherKey = reader.read()
                        when (yetAnotherKey.toChar()) {
                            'A'  -> ARROW_UP
                            'B'  -> ARROW_DOWN
                            'C'  -> ARROW_RIGHT
                            'D'  -> ARROW_LEFT
                            'H'  -> HOME
                            'F'  -> END
                            else -> yetAnotherKey

//                            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {  // e.g: esc[5~ == page_up
//                                val yetYetAnotherChar = System.`in`.read()
//                                if (yetYetAnotherChar != '~'.code) {
//                                    yetYetAnotherChar
//                                } else {
//                                    when(yetAnotherKey.toChar()) {
//                                        '1',
//                                        '7'  -> Viewer.HOME
//                                        '3'  -> Viewer.DEL
//                                        '4',
//                                        '8'  -> Viewer.END
//                                        '5'  -> Viewer.PAGE_UP
//                                        '6'  -> Viewer.PAGE_DOWN
//                                        else -> yetAnotherKey
//                                    }
//                                }
//                            }

                        }
                    }
                    '0' -> {
                        val yetAnotherKey = reader.read()
                        when (yetAnotherKey.toChar()) {
                            'H'  -> HOME
                            'F'  -> END
                            else -> yetAnotherKey
                        }
                    }
                    else -> nextKey
                }
            }

            else -> key
        }
    }

    override fun close() {
        terminal.puts(InfoCmp.Capability.clear_screen)
        terminal.flush()
        terminal.close()
    }

    companion object {
        const val ARROW_UP    = 1000
        const val ARROW_DOWN  = 1001
        const val ARROW_LEFT  = 1002
        const val ARROW_RIGHT = 1003

        const val HOME      = 1004
        const val END       = 1005
        const val PAGE_UP   = 1006
        const val PAGE_DOWN = 1007
        const val DEL       = 1008
    }
}