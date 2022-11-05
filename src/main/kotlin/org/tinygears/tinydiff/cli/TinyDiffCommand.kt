/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tinygears.tinydiff.cli

import picocli.CommandLine
import java.nio.file.Path

/**
 * Command-line tool to diff files.
 */
@CommandLine.Command(
    name                 = "tinydiff",
    description          = ["shows file differences"],
    parameterListHeading = "%nParameters:%n",
    optionListHeading    = "%nOptions:%n")
class TinyDiffCommand : Runnable {

    @CommandLine.Parameters(index = "0", arity = "1..*", paramLabel = "inputfile", description = ["input file(s)"])
    private lateinit var inputPath: List<Path>

    override fun run() {
        require(inputPath.size == 2)

        val screen = TerminalScreen()
        screen.setView(DiffView(inputPath[0].toString(), inputPath[1].toString()))
        screen.use { it.show() }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmdLine = CommandLine(TinyDiffCommand())
            cmdLine.execute(*args)
        }
    }
}