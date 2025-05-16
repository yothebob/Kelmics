// Ideas from chatgippty

// data class Cursor(var position: Long) {
//     fun move(delta: Long, max: Long) {
//         position = (position + delta).coerceIn(0, max)
//     }

//     fun set(pos: Long, max: Long) {
//         position = pos.coerceIn(0, max)
//     }
// }

// sealed class EditAction {
//     abstract fun undo(buffer: TextBuffer)
//     abstract fun redo(buffer: TextBuffer)

//     data class Insert(val offset: Long, val text: String) : EditAction() {
//         override fun undo(buffer: TextBuffer) {
//             buffer.delete(offset, text.length.toLong())
//         }

//         override fun redo(buffer: TextBuffer) {
//             buffer.insert(offset, text)
//         }
//     }

//     data class Delete(val offset: Long, val deleted: String) : EditAction() {
//         override fun undo(buffer: TextBuffer) {
//             buffer.insert(offset, deleted)
//         }

//         override fun redo(buffer: TextBuffer) {
//             buffer.delete(offset, deleted.length.toLong())
//         }
//     }

//     data class Replace(val offset: Long, val oldText: String, val newText: String) : EditAction() {
//         override fun undo(buffer: TextBuffer) {
//             buffer.replace(offset, newText.length.toLong(), oldText)
//         }

//         override fun redo(buffer: TextBuffer) {
//             buffer.replace(offset, oldText.length.toLong(), newText)
//         }
//     }
// }

// class UndoManager {
//     private val undoStack = mutableListOf<EditAction>()
//     private val redoStack = mutableListOf<EditAction>()

//     fun apply(action: EditAction, buffer: TextBuffer) {
//         action.redo(buffer)
//         undoStack.add(action)
//         redoStack.clear()
//     }

//     fun undo(buffer: TextBuffer) {
//         if (undoStack.isNotEmpty()) {
//             val action = undoStack.removeLast()
//             action.undo(buffer)
//             redoStack.add(action)
//         }
//     }

//     fun redo(buffer: TextBuffer) {
//         if (redoStack.isNotEmpty()) {
//             val action = redoStack.removeLast()
//             action.redo(buffer)
//             undoStack.add(action)
//         }
//     }

//     fun clear() {
//         undoStack.clear()
//         redoStack.clear()
//     }
// }

// val buffer = OkioTextBuffer()
// val cursor = Cursor(0)
// val undoManager = UndoManager()

// fun insertText(text: String) {
//     val action = EditAction.Insert(cursor.position, text)
//     undoManager.apply(action, buffer)
//     cursor.move(text.length.toLong(), buffer.length)
// }

// fun deleteText(length: Long) {
//     val deleted = buffer.read(cursor.position, length)
//     val action = EditAction.Delete(cursor.position, deleted)
//     undoManager.apply(action, buffer)
// }

// fun replaceText(length: Long, newText: String) {
//     val oldText = buffer.read(cursor.position, length)
//     val action = EditAction.Replace(cursor.position, oldText, newText)
//     undoManager.apply(action, buffer)
// }


// 🧩 What You Can Add Later
// - Selections & multiple cursors
// - Persistent markers (like Emacs' markers)
// - Batched edits (beginTransaction() / commit())
// - Search / syntax parsing
// - File I/O using Okio's Source/Sink

