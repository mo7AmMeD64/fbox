package com.fkbox.app.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fkbox.app.R
import com.fkbox.app.fkApp
import com.fkbox.app.ui.common.FavoriteButton
import com.fkbox.app.ui.common.LocalSnack
import com.fkbox.app.ui.common.MessageState
import com.fkbox.app.ui.common.PosterCard

@Composable
fun SavedScreen(contentPadding: PaddingValues, onOpen: (String) -> Unit) {
    val prefs = LocalContext.current.fkApp.prefs
    val saved by prefs.favorites.collectAsStateWithLifecycle()
    val snack = LocalSnack.current
    val removedLabel = stringResource(R.string.saved_removed, "%s")
    val undo = stringResource(R.string.undo)

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        if (saved.isEmpty()) {
            MessageState(
                icon = Icons.Rounded.Favorite,
                title = stringResource(R.string.saved_empty_title),
                body = stringResource(R.string.saved_empty_body),
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(saved, key = { it.id }) { item ->
                    PosterCard(
                        item = item,
                        onClick = { onOpen(item.id) },
                        trailing = {
                            FavoriteButton(saved = true, onClick = {
                                prefs.toggleFavorite(item)
                                snack(removedLabel.replace("%s", item.title), undo) { prefs.addFavorite(item) }
                            })
                        },
                    )
                }
            }
        }
    }
}
