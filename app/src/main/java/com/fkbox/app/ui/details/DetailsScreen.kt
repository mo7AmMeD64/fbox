package com.fkbox.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fkbox.app.R
import com.fkbox.app.data.moviebox.Details
import com.fkbox.app.ui.common.CoverImage
import com.fkbox.app.ui.common.FavoriteButton
import com.fkbox.app.ui.common.Heading
import com.fkbox.app.ui.common.LocalSnack
import com.fkbox.app.ui.common.MessageState
import com.fkbox.app.ui.common.PosterCard
import com.fkbox.app.ui.common.WavyProgress
import com.fkbox.app.ui.common.pressScale

/** What the player needs to start an episode or a movie. */
data class PlayRequest(
    val id: String,
    val title: String,
    val season: Int,
    val episode: Int,
    val maxEpisode: Int,
)

@Composable
fun DetailsScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onPlay: (PlayRequest) -> Unit,
    vm: DetailsViewModel = viewModel(),
) {
    val saved by vm.isSaved.collectAsStateWithLifecycle()
    val snack = LocalSnack.current
    val undo = stringResource(R.string.undo)
    val addedFmt = stringResource(R.string.saved_added, "%s")
    val removedFmt = stringResource(R.string.saved_removed, "%s")

    val toggle: () -> Unit = {
        val item = vm.savedItem()
        val nowSaved = vm.toggleSaved()
        if (item != null) {
            if (nowSaved) snack(addedFmt.replace("%s", item.title), null, null)
            else snack(removedFmt.replace("%s", item.title), undo) { vm.toggleSaved() }
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
        when (val ui = vm.ui) {
            is DetailsUi.Loading -> LoadingState(contentPadding, onBack)
            is DetailsUi.Error -> Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(contentPadding.calculateTopPadding() + 8.dp))
                BackButton(onBack, Modifier.padding(start = 16.dp))
                MessageState(
                    icon = Icons.Rounded.Search,
                    title = stringResource(R.string.error_generic, ui.message),
                    actionLabel = stringResource(R.string.retry),
                    onAction = vm::load,
                    modifier = Modifier.weight(1f),
                )
            }
            is DetailsUi.Ready -> ReadyContent(
                d = ui.details,
                vm = vm,
                saved = saved,
                contentPadding = contentPadding,
                onBack = onBack,
                onToggleSaved = toggle,
                onOpen = onOpen,
                onPlay = onPlay,
            )
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val source = remember { MutableInteractionSource() }
    FilledTonalIconButton(onClick = onBack, modifier = modifier.pressScale(source), interactionSource = source) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}

/** Placeholder layout + wavy circular progress near the bottom while the title loads. */
@Composable
private fun LoadingState(contentPadding: PaddingValues, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp)
            .padding(horizontal = 16.dp),
    ) {
        Box {
            CoverImage(
                url = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(360f / 208f).clip(RoundedCornerShape(32.dp)),
            )
            BackButton(onBack, Modifier.padding(8.dp))
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            WavyProgress(Modifier.size(56.dp))
        }
    }
}

@Composable
private fun ReadyContent(
    d: Details,
    vm: DetailsViewModel,
    saved: Boolean,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onOpen: (String) -> Unit,
    onPlay: (PlayRequest) -> Unit,
) {
    val season = d.seasons.firstOrNull { it.season == vm.selectedSeason } ?: d.seasons.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Hero image (360x208 ratio) with back + save buttons on top.
        item {
            Box(Modifier.padding(horizontal = 16.dp)) {
                CoverImage(
                    url = d.posterUrl,
                    contentDescription = d.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(360f / 208f).clip(RoundedCornerShape(32.dp)),
                )
                BackButton(onBack, Modifier.align(Alignment.TopStart).padding(8.dp))
                FavoriteButton(saved, onToggleSaved, Modifier.align(Alignment.TopEnd).padding(8.dp))
            }
        }

        // Title + rating always share one centered row and never wrap.
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = d.title,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = d.imdb?.let { stringResource(R.string.imdb_rating, String.format("%.1f", it)) }
                        ?: stringResource(R.string.imdb_unrated),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }

        item {
            val meta = listOfNotNull(
                d.year?.toString(),
                d.duration,
                d.country,
                d.genres.takeIf { it.isNotEmpty() }?.joinToString(", "),
            ).joinToString(" • ")
            if (meta.isNotEmpty()) {
                Text(
                    meta,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item { InsetDivider() }

        item { Heading(stringResource(R.string.about), Modifier.fillMaxWidth().padding(horizontal = 16.dp), TextAlign.Center) }
        item {
            Text(
                text = d.description ?: stringResource(R.string.no_story),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 26.sp,
            )
        }
        if (d.dubs.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.available_audio, d.dubs.joinToString(", ") { it.language }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (d.actors.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.cast),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(d.actors.take(20)) { actor ->
                        Column(Modifier.width(84.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CoverImage(actor.image, Modifier.size(72.dp).clip(CircleShape), actor.name)
                            Text(
                                actor.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        item { InsetDivider() }

        item { Heading(stringResource(R.string.episodes), Modifier.padding(horizontal = 16.dp)) }

        if (!d.isSeries) {
            item {
                val src = remember { MutableInteractionSource() }
                Button(
                    onClick = { onPlay(PlayRequest(d.id, d.title, 0, 0, 0)) },
                    interactionSource = src,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp).pressScale(src),
                ) {
                    Icon(Icons.Rounded.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.play_movie))
                }
            }
        } else if (season != null) {
            if (d.seasons.size > 1) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(d.seasons, key = { it.season }) { s ->
                            FilterChip(
                                selected = s.season == season.season,
                                onClick = { vm.selectedSeason = s.season },
                                label = { Text(stringResource(R.string.season_n, s.season)) },
                                shape = CircleShape,
                            )
                        }
                    }
                }
            }
            items((1..season.maxEpisode).toList(), key = { "s${season.season}e$it" }) { ep ->
                EpisodeRow(
                    label = stringResource(R.string.episode_n, ep),
                    onClick = { onPlay(PlayRequest(d.id, d.title, season.season, ep, season.maxEpisode)) },
                )
            }
        } else {
            item {
                Text(
                    stringResource(R.string.nothing_here),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        if (vm.recommendations.isNotEmpty()) {
            item { InsetDivider() }
            item {
                Text(
                    stringResource(R.string.more_like_this),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(vm.recommendations, key = { it.id }) { r ->
                        PosterCard(item = r, onClick = { onOpen(r.id) }, modifier = Modifier.width(150.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InsetDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun EpisodeRow(label: String, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).pressScale(source),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        interactionSource = source,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(Icons.Rounded.PlayCircle, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
