package com.superandroid.gbadex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.superandroid.gbadex.data.model.Game
import com.superandroid.gbadex.viewmodel.LibraryViewModel
import com.superandroid.gbadex.viewmodel.SortMode
import com.superandroid.gbadex.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onGameLaunch: (Game) -> Unit,
    onChangeBoxArt: (Game) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }
    var contextMenuGame by remember { mutableStateOf<Game?>(null) }

    // SAF folder picker
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.onFolderSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GBADex") },
                actions = {
                    // Search
                    var showSearch by remember { mutableStateOf(false) }
                    if (showSearch) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = { Text("Search games...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    }

                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, "Search")
                    }

                    // Grid / List toggle
                    IconButton(onClick = {
                        viewModel.onViewModeChanged(
                            if (uiState.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                        )
                    }) {
                        Icon(
                            if (uiState.viewMode == ViewMode.GRID) Icons.Default.ViewList
                            else Icons.Default.GridView,
                            "Toggle view"
                        )
                    }

                    // Sort menu
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, "Sort")
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("A–Z") },
                            onClick = { viewModel.onSortModeChanged(SortMode.ALPHABETICAL); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.SortByAlpha, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Last Played") },
                            onClick = { viewModel.onSortModeChanged(SortMode.LAST_PLAYED); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.History, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Favorites") },
                            onClick = { viewModel.onSortModeChanged(SortMode.FAVORITES); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.Favorite, null) }
                        )
                    }

                    // Add ROM folder
                    IconButton(onClick = { folderPicker.launch(null) }) {
                        Icon(Icons.Default.CreateNewFolder, "Add ROM folder")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.games.isEmpty() -> {
                    EmptyLibraryPrompt(onPickFolder = { folderPicker.launch(null) })
                }

                else -> {
                    val games = viewModel.filteredGames()

                    if (uiState.viewMode == ViewMode.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(games, key = { it.id }) { game ->
                                GameGridCard(
                                    game = game,
                                    onClick = { onGameLaunch(game) },
                                    onLongClick = { contextMenuGame = game },
                                    onFavoriteToggle = { viewModel.toggleFavorite(game) }
                                )
                            }
                        }
                    } else {
                        // List view — we'll build this next phase
                    }
                }
            }

            uiState.errorMessage?.let { msg ->
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                    Text(msg)
                }
            }
        }
    }

    // Context menu (long press on a game)
    contextMenuGame?.let { game ->
        GameContextMenu(
            game = game,
            onDismiss = { contextMenuGame = null },
            onLaunch = { onGameLaunch(game); contextMenuGame = null },
            onFavoriteToggle = { viewModel.toggleFavorite(game); contextMenuGame = null },
            onChangeBoxArt = { onChangeBoxArt(game); contextMenuGame = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameGridCard(
    game: Game,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column {
            // Box art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f) // GBA box art ratio
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                if (game.boxArtPath != null) {
                    AsyncImage(
                        model = game.boxArtPath,
                        contentDescription = game.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder when no box art
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.SportsEsports,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Favorite badge
                if (game.isFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(18.dp)
                    )
                }
            }

            // Title
            Text(
                text = game.title,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun EmptyLibraryPrompt(onPickFolder: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text("No ROMs yet", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap below to pick your GBA ROM folder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onPickFolder) {
            Icon(Icons.Default.CreateNewFolder, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Pick ROM Folder")
        }
    }
}

@Composable
fun GameContextMenu(
    game: Game,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onChangeBoxArt: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(game.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column {
                TextButton(onClick = onLaunch, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Launch")
                }
                TextButton(onClick = onFavoriteToggle, modifier = Modifier.fillMaxWidth()) {
                    Icon(if (game.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (game.isFavorite) "Remove Favorite" else "Add to Favorites")
                }
                TextButton(onClick = onChangeBoxArt, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Image, null); Spacer(Modifier.width(8.dp)); Text("Change Box Art")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
