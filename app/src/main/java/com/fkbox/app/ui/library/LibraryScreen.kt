package com.fkbox.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fkbox.app.R
import com.fkbox.app.fkApp
import com.fkbox.app.ui.common.EmptyState
import com.fkbox.app.ui.common.FavoriteButton
import com.fkbox.app.ui.common.HorizontalPosterCard
import com.fkbox.app.ui.common.LoadingBlock
import com.fkbox.app.ui.common.PosterCard
import com.fkbox.app.ui.common.SectionHeader
import com.fkbox.app.ui.common.SkeletonHorizontalPoster

@Composable
fun LibraryScreen(contentPadding: PaddingValues, onOpen: (String) -> Unit) {
    val prefs = LocalContext.current.fkApp.prefs
    val saved by prefs.favorites.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(LibraryTab.Favorites) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab.index,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                indicatorColor = MaterialTheme.colorScheme.primary,
                dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
            ) {
                LibraryTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(stringResource(tab.label), style = MaterialTheme.typography.labelLarge) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                    )
                }
            }

            // Content
            androidx.compose.foundation.layout.Box(Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    LibraryTab.Favorites -> FavoritesTab(saved = saved, onOpen = onOpen, onToggleFavorite = { item ->
                        prefs.toggleFavorite(item)
                    })
                    LibraryTab.History -> HistoryTab(onOpen = onOpen)
                    LibraryTab.ContinueWatching -> ContinueWatchingTab(onOpen = onOpen)
                }
            }
        }
    }
}

private enum class LibraryTab(val label: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector, val index: Int) {
    Favorites(R.string.favorites, Icons.Rounded.Favorite, 0),
    History(R.string.history, Icons.Rounded.History, 1),
    ContinueWatching(R.string.continue_watching, Icons.Rounded.PlayCircle, 2),
}

@Composable
private fun FavoritesTab(
    saved: List<com.fkbox.app.data.moviebox.Item>,
    onOpen: (String) -> Unit,
    onToggleFavorite: (com.fkbox.app.data.moviebox.Item) -> Unit,
) {
    if (saved.isEmpty()) {
        EmptyState(
            icon = Icons.Rounded.Favorite,
            title = stringResource(R.string.saved_empty_title),
            description = stringResource(R.string.saved_empty_body),
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 8.dp,
                bottom = 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(saved, key = { it.id }) { item ->
                PosterCard(
                    item = item,
                    onClick = { onOpen(item.id) },
                    trailing = {
                        FavoriteButton(
                            saved = true,
                            onClick = { onToggleFavorite(item) },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryTab(onOpen: (String) -> Unit) {
    // TODO: Implement watch history from local storage
    EmptyState(
        icon = Icons.Rounded.History,
        title = "No Watch History",
        description = "Your recently watched content will appear here",
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun ContinueWatchingTab(onOpen: (String) -> Unit) {
    // TODO: Implement continue watching from local storage
    val continueWatching = emptyList<com.fkbox.app.data.moviebox.Item>()

    if (continueWatching.isEmpty()) {
        EmptyState(
            icon = Icons.Rounded.PlayCircle,
            title = stringResource(R.string.continue_watching),
            description = "Content you've started watching will appear here",
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(continueWatching, key = { it.id }) { item ->
                HorizontalPosterCard(
                    item = item,
                    progress = 0f, // TODO: Load from local storage
                    onClick = { onOpen(item.id) },
                )
            }
        }
    }
}