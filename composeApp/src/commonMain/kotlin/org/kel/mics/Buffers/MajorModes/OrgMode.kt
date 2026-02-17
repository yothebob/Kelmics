package org.kel.mics.Buffers.MajorModes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


sealed class OrgNode {
    data class Heading(
        val level: Int,
        val text: List<InlineNode>,
        val children: MutableList<OrgNode> = mutableListOf(),
        var collapsed: Boolean = false
    ) : OrgNode()
    data class Paragraph(val text: List<InlineNode>) : OrgNode()
    data class UnorderedList(val items: List<List<InlineNode>>) : OrgNode()
}

sealed class InlineNode {
    data class Text(val value: String) : InlineNode()
    data class Bold(val value: String) : InlineNode()
    data class Italic(val value: String) : InlineNode()
}

fun parseInline(text: String): List<InlineNode> {
    val result = mutableListOf<InlineNode>()
    var remaining = text

    val boldRegex = Regex("""\*(.*?)\*""")
    val italicRegex = Regex("""/(.*?)/""")

    while (remaining.isNotEmpty()) {
        val boldMatch = boldRegex.find(remaining)
        val italicMatch = italicRegex.find(remaining)

        val nextMatch = listOfNotNull(boldMatch, italicMatch)
            .minByOrNull { it.range.first }

        if (nextMatch == null) {
            result.add(InlineNode.Text(remaining))
            break
        }

        if (nextMatch.range.first > 0) {
            result.add(
                InlineNode.Text(
                    remaining.substring(0, nextMatch.range.first)
                )
            )
        }

        val content = nextMatch.groupValues[1]

        if (nextMatch.value.startsWith("*")) {
            result.add(InlineNode.Bold(content))
        } else {
            result.add(InlineNode.Italic(content))
        }

        remaining = remaining.substring(nextMatch.range.last + 1)
    }

    return result
}


fun parseOrg(input: String): List<OrgNode> {
    val lines = input.lines()
    val root = mutableListOf<OrgNode>()

    val headingStack = mutableListOf<OrgNode.Heading>()

    fun addNode(node: OrgNode) {
        if (headingStack.isEmpty()) {
            root.add(node)
        } else {
            headingStack.last().children.add(node)
        }
    }

    var i = 0
    while (i < lines.size) {
        val line = lines[i]

        when {
            line.startsWith("*") -> {
                val level = line.takeWhile { it == '*' }.length
                val text = line.drop(level).trim()

                val heading = OrgNode.Heading(
                    level = level,
                    text = parseInline(text)
                )

                while (headingStack.isNotEmpty() &&
                    headingStack.last().level >= level
                ) {
                    headingStack.removeLast()
                }

                if (headingStack.isEmpty()) {
                    root.add(heading)
                } else {
                    headingStack.last().children.add(heading)
                }

                headingStack.add(heading)
            }

            line.startsWith("- ") -> {
                val items = mutableListOf<List<InlineNode>>()
                var j = i
                while (j < lines.size && lines[j].startsWith("- ")) {
                    val itemText = lines[j].removePrefix("- ").trim()
                    items.add(parseInline(itemText))
                    j++
                }

                addNode(OrgNode.UnorderedList(items))
                i = j - 1
            }

            line.isNotBlank() -> {
                addNode(
                    OrgNode.Paragraph(
                        text = parseInline(line.trim())
                    )
                )
            }
        }

        i++
    }

    return root
}

@Composable
fun OrgDocument(nodes: List<OrgNode>) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        nodes.forEach { node ->
            OrgNodeView(node)
        }
    }
}

@Composable
fun OrgNodeView(node: OrgNode) {
    when (node) {
        is OrgNode.Heading -> FoldableHeading(node)
        is OrgNode.Paragraph -> ParagraphNode(node)
        is OrgNode.UnorderedList -> ListNode(node)
    }
}

@Composable
fun FoldableHeading(node: OrgNode.Heading) {
    var collapsed by remember { mutableStateOf(node.collapsed) }

    val fontSize = when (node.level) {
        1 -> 28.sp
        2 -> 22.sp
        3 -> 18.sp
        else -> 16.sp
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    collapsed = !collapsed
                    node.collapsed = collapsed
                }
                .padding(vertical = 4.dp)
        ) {
            Text(
                if (collapsed) "▶ " else "▼ ",
                fontSize = fontSize
            )

            Text(
                buildAnnotatedString {
                    appendInlineNodes(node.text)
                },
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }

        if (!collapsed) {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                node.children.forEach {
                    OrgNodeView(it)
                }
            }
        }
    }
}

@Composable
fun ParagraphNode(node: OrgNode.Paragraph) {
    Text(
        buildAnnotatedString {
            appendInlineNodes(node.text)
        },
        fontSize = 16.sp
    )
}

@Composable
fun ListNode(node: OrgNode.UnorderedList) {
    Column {
        node.items.forEach { item ->
            Row {
                Text("• ")
                Text(
                    buildAnnotatedString {
                        appendInlineNodes(item)
                    }
                )
            }
        }
    }
}

fun AnnotatedString.Builder.appendInlineNodes(nodes: List<InlineNode>) {
    nodes.forEach { node ->
        when (node) {
            is InlineNode.Text -> append(node.value)

            is InlineNode.Bold -> withStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold)
            ) {
                append(node.value)
            }

            is InlineNode.Italic -> withStyle(
                style = SpanStyle(fontStyle = FontStyle.Italic)
            ) {
                append(node.value)
            }
        }
    }
}

@Composable
fun OrgMode(modifier: Modifier = Modifier, content: String) {
    val nodes = remember(content) {
        parseOrg(content)
    }
    OrgDocument(nodes)
}
