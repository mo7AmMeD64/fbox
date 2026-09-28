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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
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
import com.fkbox.app.ui.common.BackdropImage
import com.fkbox.app.ui.common.Body
import com.fkbox.app.ui.common.CoverImage
import com.fkbox.app.ui.common.EmptyState
import com.fkbox.app.ui.common.FavoriteButton
import com.fkbox.app.ui.common.Heading
import com.fkbox.app.ui.common.Metadata
import com.fkbox.app.ui.common.PosterCard
import com.fkbox.app.ui.common.PrimaryButton
import com.fkbox.app.ui.common.RatingBadge
import com.fkbox.app.ui.common.SectionHeader
import com.fkbox.app.ui.common.WavyProgress
import com.fkbox.app.ui.common.pressScale
import com.fkbox.app.ui.theme.DesignTokens

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
    val snack = com.fkbox.app.ui.common.LocalSnack.current
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

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val ui = vm.ui) {
            is DetailsUi.Loading -> LoadingState(contentPadding, onBack)
            is DetailsUi.Error -> Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(contentPadding.calculateTopPadding() + 8.dp))
                BackButton(onBack, Modifier.padding(start = 16.dp))
                com.fkbox.app.ui.common.ErrorState(
                    message = ui.message,
                    onRetry = vm::load,
                    modifier = Modifier.weight(1f).padding(16.dp),
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
            BackdropImage(
                url = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(360f / 208f).clip(DesignTokens.Shape.LG),
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
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Hero Section with Backdrop + Poster
        item {
            HeroSection(
                details = d,
                onBack = onBack,
                onPlay = { onPlay(PlayRequest(d.id, d.title, 0, 0, 0)) },
                onFavorite = onToggleSaved,
                isFavorite = saved,
            )
        }

        // About Section
        item {
            AboutSection(details = d)
        }

        // Cast Section
        if (d.actors.isNotEmpty()) {
            item {
                CastSection(actors = d.actors)
            }
        }

        // Episodes / Seasons Section
        item {
            EpisodesSection(
                details = d,
                vm = vm,
                onPlay = onPlay,
            )
        }

        // Recommendations
        if (vm.recommendations.isNotEmpty()) {
            item {
                RecommendationsSection(
                    recommendations = vm.recommendations,
                    onOpen = onOpen,
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    details: Details,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    isFavorite: Boolean,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Backdrop
        BackdropImage(
            url = details.posterUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            overlay = true,
        )

        // Back button overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 0.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            BackButton(onBack)
        }

        // Poster + Metadata + Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Poster
                CoverImage(
                    url = details.posterUrl,
                    contentDescription = details.title,
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(2f / 3f)
                        .clip(DesignTokens.Shape.MD),
                )

                // Title + Metadata + Actions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start,
                ) {
                    // Title & Rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = details.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        details.imdb?.let {
                            RatingBadge(rating = it, size = 18)
                        }
                    }

                    // Metadata chips
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        details.year?.let { Metadata(text = it.toString()) }
                        details.duration?.let { Metadata(text = it) }
                        details.genres.take(3).joinToString(" • ").let { if (it.isNotBlank()) Metadata(text = it) }
                        com.fkbox.app.ui.common.ContentTypeBadge(
                            text = stringResource(if (details.isSeries) R.string.type_series else R.string.type_movie),
                        )
                    }

                    // Action buttons
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PrimaryButton(
                            text = stringResource(if (details.isSeries) R.string.play else R.string.play_movie),
                            onClick = onPlay,
                            icon = Icons.Rounded.PlayArrow,
                            modifier = Modifier.weight(1f),
                        )
                        com.fkbox.app.ui.common.SecondaryButton(
                            text = stringResource(R.string.more_details),
                            onClick = { /* Already on details */ },
                            icon = Icons.Rounded.PlayCircle,
                            modifier = Modifier.weight(1f),
                        )
                        FavoriteButton(
                            saved = isFavorite,
                            onClick = onFavorite,
                            size = 28.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSection(details: Details) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Heading(text = stringResource(R.string.about), style = "headlineMedium")

        Body(
            text = details.description ?: stringResource(R.string.no_story),
            style = "bodyLarge",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (details.dubs.isNotEmpty()) {
            Body(
                text = stringResource(R.string.available_audio, details.dubs.joinToString(", ") { it.language }),
                style = "bodySmall",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }

        // Metadata grid
        MetadataGrid(details = details)
    }
}

@Composable
private fun MetadataGrid(details: Details) {
    val metadata = mutableListOf<Pair<String, String>>()
    details.year?.let { metadata.add("Year" to it.toString()) }
    details.duration?.let { metadata.add("Runtime" to it) }
    details.country?.let { metadata.add("Country" to it) }
    details.language?.let { metadata.add("Language" to it) }
    details.genres.takeIf { it.isNotEmpty() }?.joinToString(", ")?.let { metadata.add("Genres" to it) }

    if (metadata.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            metadata.forEachIndexed { index, (label, value) ->
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun CastSection(actors: List<com.fkbox.app.data.moviebox.Actor>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(title = stringResource(R.string.cast))

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(actors.take(20), key = { it.name }) { actor ->
                Column(
                    modifier = Modifier.width(84.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CoverImage(
                        url = actor.image,
                        contentDescription = actor.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = actor.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                    actor.character?.let { character ->
                        Text(
                            text = character,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodesSection(
    details: Details,
    vm: DetailsViewModel,
    onPlay: (PlayRequest) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!details.isSeries) {
            // Movie - single play button
            PrimaryButton(
                text = stringResource(R.string.play_movie),
                onClick = { onPlay(PlayRequest(details.id, details.title, 0, 0, 0)) },
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // Series - season selector + episodes
            SectionHeader(title = stringResource(R.string.episodes))

            if (details.seasons.size > 1) {
                // Season selector
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(details.seasons, key = { it.season }) { s ->
                        FilterChip(
                            selected = s.season == vm.selectedSeason,
                            onClick = { vm.selectedSeason = s.season },
                            label = { Text(stringResource(R.string.season_n, s.season)) },
                            shape = DesignTokens.Shape.Pill,
                        )
                    }
                }
            }

            val season = details.seasons.firstOrNull { it.season == vm.selectedSeason } ?: details.seasons.firstOrNull()
            season?.let { s ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (1..s.maxEpisode).forEach { ep ->
                        EpisodeRow(
                            label = stringResource(R.string.episode_n, ep),
                            onClick = { onPlay(PlayRequest(details.id, details.title, s.season, ep, s.maxEpisode)) },
                        )
                    }
                }
            } ?: EmptyState(
                icon = Icons.Rounded.PlayCircle,
                title = stringResource(R.string.nothing_here),
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            )
        }
    }
}

@Composable
private fun EpisodeRow(label: String, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().pressScale(source),
        shape = DesignTokens.Shape.MD,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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

@Composable
private fun RecommendationsSection(
    recommendations: List<com.fkbox.app.data.moviebox.Item>,
    onOpen: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(title = stringResource(R.string.more_like_this))

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(recommendations, key = { it.id }) { item ->
                PosterCard(item = item, onClick = { onOpen(item.id) }, modifier = Modifier.width(150.dp))
            }
        }
    }
}