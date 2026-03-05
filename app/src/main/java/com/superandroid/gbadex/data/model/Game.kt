package com.superandroid.gbadex.data.model

/**
 * Represents a single GBA game / ROM in the library.
 */
data class Game(
    val id: String,           // Unique ID — derived from file path hash
    val title: String,        // Display name (cleaned from filename)
    val filePath: String,     // Absolute path to the .gba file
    val boxArtPath: String?,  // Local path to cached box art image (null = no art yet)
    val lastPlayed: Long?,    // Unix timestamp, null if never played
    val playTimeMs: Long = 0, // Total playtime in milliseconds
    val isFavorite: Boolean = false,
)

/**
 * Cleans a raw filename into a readable game title.
 * e.g. "pokemon_emerald_(U)(E)[!].gba" -> "Pokemon Emerald"
 */
fun String.toGameTitle(): String {
    return this
        .removeSuffix(".gba")
        .removeSuffix(".zip")
        .replace(Regex("\\(.*?\\)"), "")   // Remove (U), (E), (USA), etc.
        .replace(Regex("\\[.*?\\]"), "")   // Remove [!], [T+Eng], etc.
        .replace(Regex("[_\\-]+"), " ")    // Underscores/dashes to spaces
        .trim()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
        .trim()
}
