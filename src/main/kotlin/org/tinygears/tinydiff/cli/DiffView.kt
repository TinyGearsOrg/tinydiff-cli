package org.tinygears.tinydiff.cli

import org.fusesource.jansi.Ansi
import org.tinygears.tinydiff.TinyDiff
import org.tinygears.tinydiff.cli.TerminalScreen.Companion.ARROW_DOWN
import org.tinygears.tinydiff.cli.TerminalScreen.Companion.ARROW_LEFT
import org.tinygears.tinydiff.cli.TerminalScreen.Companion.ARROW_RIGHT
import org.tinygears.tinydiff.cli.TerminalScreen.Companion.ARROW_UP
import org.tinygears.tinydiff.cli.TerminalScreen.Companion.END
import org.tinygears.tinydiff.cli.TerminalScreen.Companion.HOME
import org.tinygears.tinydiff.cli.TerminalScreen.Companion.PAGE_DOWN
import org.tinygears.tinydiff.cli.TerminalScreen.Companion.PAGE_UP
import java.nio.file.Files
import java.nio.file.Path

class DiffView(fileA: String, fileB: String): TerminalView {
    private var rows    = 0
    private var columns = 0

    private var cursorX = 0
    private var cursorY = 0

    private var offsetX = 0
    private var offsetY = 0

    private var content: List<Content>

    private val contentSize: Int
        get() = content.size

    private val columnWidth: Int
        get() = columns / 2 - 1

    init {
        content = openFiles(fileA, fileB)
    }

    private fun openFiles(fileA: String, fileB: String): List<Content> {
        val pathA = Path.of(fileA)
        val pathB = Path.of(fileB)

        if (Files.exists(pathA) && Files.isRegularFile(pathA) &&
            Files.exists(pathB) && Files.isRegularFile(pathB)) {

            val patch = TinyDiff.diff(pathA, pathB)

            val contentList = mutableListOf<Content>()
            SideBySideFormatter(contentList).format(patch)

            return contentList
        }

        return emptyList()
    }

    override fun updateSize(width: Int, height: Int) {
        columns = width
        rows    = height
    }

    override fun drawContent(builder: StringBuilder) {
        scroll()

        for (i in 0 until rows) {
            val fileI = offsetY + i

            if (fileI == cursorY) {
                builder.append(Ansi.ansi().bgBrightYellow())
            } else {
                builder.append(Ansi.ansi().bgDefault())
            }

            if (fileI >= contentSize) {
                builder.append("~")
            } else {
                val c = content[fileI]

                val command = c.command
                if (command == Command.DELETED) {
                    builder.append(Ansi.ansi().fgRed())
                } else if (command == Command.CHANGED) {
                    builder.append(Ansi.ansi().fgBlue())
                }

                val lineA = c.contentA
                val lengthA = (lineA.length - offsetX).coerceAtLeast(0).coerceAtMost(columnWidth - 2)
                builder.append(lineA, offsetX, offsetX + lengthA)

                builder.append(Ansi.ansi().fgDefault())

                val remainingLength = columnWidth - lengthA - 2
                val spaces = " ".repeat(remainingLength)
                builder.append(spaces)

                builder.append("\u2503  ")

                if (command == Command.INSERTED) {
                    builder.append(Ansi.ansi().fgGreen())
                } else if (command == Command.CHANGED) {
                    builder.append(Ansi.ansi().fgBlue())
                }

                val lineB = c.contentB
                val lengthB = (lineB.length - offsetX).coerceAtLeast(0).coerceAtMost(columnWidth)
                builder.append(lineB, offsetX, offsetX + lengthB)

                builder.append(Ansi.ansi().fgDefault())
            }
            builder.append("\u001b[K\r\n")
        }

        builder.append(Ansi.ansi().bgDefault())
    }

    override fun drawCursor(builder: StringBuilder) {
        builder.append("\u001b[%d;%dH".format(cursorY - offsetY + 1, cursorX + 1))
    }

    override fun getStatusMessage(): String {
        return "Rows: $rows X: $cursorX Y: $cursorY Offset Y: $offsetY"
    }

    private fun scroll() {
        if (cursorY >= rows + offsetY) {
            offsetY = cursorY - rows + 1
        } else if (cursorY < offsetY) {
            offsetY = cursorY
        }
    }

    override fun handleKey(key: Int) {
        if (listOf(ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT, HOME, END, PAGE_UP, PAGE_DOWN).contains(key)) {
            moveCursor(key)
        }
    }

    private fun moveCursor(key: Int) {
        val line = null //if (cursorY >= contentSize) null else content[cursorY]

        when (key) {
            ARROW_UP -> {
                if (cursorY > 0) {
                    cursorY--
                }
            }

            ARROW_DOWN -> {
                if (cursorY < contentSize - 1) {
                    cursorY++
                }
            }

            // wrap cursor around
//            ARROW_LEFT -> {
//                if (cursorX != 0) {
//                    cursorX--
//                } else if (cursorY > 0) {
//                    cursorY--
//                    //cursorX = content[cursorY].length
//                }
//            }

//            ARROW_RIGHT -> {
//                if (line != null && cursorX < line.length) {
//                    cursorX++
//                } else if (line != null && cursorX == line.length) {
//                    cursorY++
//                    cursorX = 0
//                }
//            }

            PAGE_UP,
            PAGE_DOWN
            -> {
                if (key == PAGE_UP) {
                    cursorY = offsetY
                } else {
                    cursorY = (offsetY + rows - 1).coerceAtMost(contentSize - 1)
                    if (cursorY > contentSize) cursorY = rows
                }

                for (i in 0 until rows) {
                    moveCursor(if (key == PAGE_UP) ARROW_UP else ARROW_DOWN)
                }
            }

//            HOME -> cursorX = 0
//            END -> {
//                if (cursorY < rows) {
//                    cursorX = content[cursorY].length
//                }
//            }
        }

        // Anchor at the end of the line moving up and down
//        val lineLength = if (cursorY > rows) 0 else content[cursorY].length
//        if (cursorX > lineLength) {
//            cursorX = lineLength
//        }
    }
}