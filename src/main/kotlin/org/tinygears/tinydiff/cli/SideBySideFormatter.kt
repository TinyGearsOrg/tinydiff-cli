/*
 * Copyright (c) 2022 Thomas Neidhart.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinygears.tinydiff.cli

import org.tinygears.tinydiff.Patch
import org.tinygears.tinydiff.format.OutputReplacementsHandler
import org.tinygears.tinydiff.format.PatchFormatter

enum class Command {
    INSERTED,
    DELETED,
    CHANGED,
    KEPT
}

data class Content(val command: Command, val contentA: String, val contentB: String)

class SideBySideFormatter constructor(private val contentList: MutableList<Content>): PatchFormatter {

    override fun format(patch: Patch) {
        patch.acceptReplacementHandler(SideBySideReplacementsHandler())
    }

    private inner class SideBySideReplacementsHandler: OutputReplacementsHandler() {

        public override fun handleReplacement(from: List<String>, to: List<String>) {
            if (from.isEmpty()) {
                handleInsert(to)
            } else if (to.isEmpty()) {
                handleDelete(from)
            } else {
                var i = 0
                var j = 0
                while (i < from.size && j < to.size) {
                    val a = from[i]
                    val b = to[i]

                    val aWithoutCRLF = replaceCRLF(a)
                    val bWithoutCRLF = replaceCRLF(b)
                    contentList.add(Content(Command.CHANGED, aWithoutCRLF, bWithoutCRLF))

                    i++
                    j++
                }

                handleDelete(from.subList(i, from.size))
                handleInsert(to.subList(j, to.size))
            }
        }

        override fun handleKeep(origObj: String?, modifiedObj: String?) {
            if (origObj == null || modifiedObj == null) {
                return
            }
            val origObjWithoutCRLF = removeCRLF(origObj)
            val modifiedObjWithoutCRLF = removeCRLF(modifiedObj)
            contentList.add(Content(Command.KEPT, origObjWithoutCRLF, modifiedObjWithoutCRLF))
        }

        private fun handleInsert(insert: List<String>) {
            for (line in insert) {
                val lineWithoutCRLF = removeCRLF(line)
                contentList.add(Content(Command.INSERTED, "", lineWithoutCRLF))
            }
        }

        private fun handleDelete(delete: List<String>) {
            for (line in delete) {
                val lineWithoutCRLF = removeCRLF(line)
                contentList.add(Content(Command.DELETED, lineWithoutCRLF, ""))
            }
        }

        private fun removeCRLF(input: String): String {
            return input.removeSuffix("\n").removeSuffix("\r")
        }

        private fun replaceCRLF(input: String): String {
            return input.replace("\n", "\u21b5")
        }
    }
}