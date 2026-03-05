package com.superandroid.gbadex.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.superandroid.gbadex.data.model.Game
import com.superandroid.gbadex.data.model.toGameTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Scans a user-selected folder (via Storage Access Framework) for .gba files
 * and returns them as a list of Game objects.
 */
class RomRepository(private val context: Context) {

    /**
     * Scan a folder URI (from SAF folder picker) for GBA ROMs.
     * Returns games sorted alphabetically by title.
     */
    suspend fun scanFolder(folderUri: Uri): List<Game> = withContext(Dispatchers.IO) {
        val games = mutableListOf<Game>()

        try {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                folderUri,
                DocumentsContract.getTreeDocumentId(folderUri)
            )

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
            )

            context.contentResolver.query(childrenUri, projection, null, null, null)
                ?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(1) ?: continue
                        val docId = cursor.getString(0) ?: continue

                        // Only pick up .gba and .zip files
                        if (!name.endsWith(".gba", ignoreCase = true) &&
                            !name.endsWith(".zip", ignoreCase = true)) continue

                        val fileUri = DocumentsContract.buildDocumentUriUsingTree(
                            folderUri, docId
                        )

                        games.add(
                            Game(
                                id = docId.hashCode().toString(),
                                title = name.toGameTitle(),
                                filePath = fileUri.toString(),
                                boxArtPath = null,
                                lastPlayed = null,
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        games.sortedBy { it.title }
    }

    /**
     * Scan a regular File directory (for direct storage paths).
     */
    suspend fun scanDirectory(directory: File): List<Game> = withContext(Dispatchers.IO) {
        directory.walkTopDown()
            .filter { it.extension.lowercase() in listOf("gba", "zip") }
            .map { file ->
                Game(
                    id = file.absolutePath.hashCode().toString(),
                    title = file.name.toGameTitle(),
                    filePath = file.absolutePath,
                    boxArtPath = null,
                    lastPlayed = null,
                )
            }
            .sortedBy { it.title }
            .toList()
    }
}
