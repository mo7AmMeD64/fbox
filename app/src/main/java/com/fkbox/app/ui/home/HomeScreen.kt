package com.fkbox.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fkbox.app.R
import com.fkbox.app.data.moviebox.HomeRequests
import com.fkbox.app.data.moviebox.Item
import com.fkbox.app.ui.common.EmptyState
import com.fkbox.app.ui.common.FeaturedHeroCard
import com.fkbox.app.ui.common.Heading
import com.fkbox.app.ui.common.HorizontalPosterCard
import com.fkbox.app.ui.common.PosterCard
import com.fkbox.app.ui.common.PrimaryButton
import com.fkbox.app.ui.common.RatingBadge
import com.fkbox.app.ui.common.SectionHeader
import com.fkbox.app.ui.common.SkeletonPoster
import com.fkbox.app.ui.common.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onOpen: (String) -> Unit,
    onSearch: (String) -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val genres = HomeRequests.genres
    val selectedGenre = vm.selectedGenre ?: genres.first()

    // Determine featured item (first from movies or series)
    val featuredItem = vm.movies.items.firstOrNull() ?: vm.series.items.firstOrNull()
    val isLoadingFeatured = vm.movies.loading || vm.series.loading
    val showFeatured = featuredItem != null || isLoadingFeatured

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Hero Section
            if (showFeatured) {
                item {
                    HeroSection(
                        item = featuredItem,
                        loading = isLoadingFeatured,
                        onPlay = { featuredItem?.let { onOpen(it.id) } },
                        onDetails = { featuredItem?.let { onOpen(it.id) } },
                        onFavorite = { featuredItem?.let { /* TODO: Toggle favorite */ } },
                        isFavorite = false,
                    )
                }
            }

            // Continue Watching
            item {
                ContinueWatchingSection(
                    items = emptyList(), // TODO: Load from local storage
                    onOpen = onOpen,
                )
            }

            // Trending
            item {
                TrendingSection(
                    items = vm.movies.items,
                    loading = vm.movies.loading,
                    error = vm.movies.error,
                    onRetry = vm::load,
                    onOpen = onOpen,
                )
            }

            // Popular Movies
            item {
                ContentSection(
                    title = stringResource(R.string.popular_movies),
                    items = vm.movies.items,
                    loading = vm.movies.loading,
                    error = vm.movies.error,
                    onRetry = vm::load,
                    onOpen = onOpen,
                    showSeeAll = true,
                    onSeeAll = { /* TODO: Navigate to full list */ },
                )
            }

            // Popular TV Shows
            item {
                ContentSection(
                    title = stringResource(R.string.popular_shows),
                    items = vm.series.items,
                    loading = vm.series.loading,
                    error = vm.series.error,
                    onRetry = vm::load,
                    onOpen = onOpen,
                    showSeeAll = true,
                    onSeeAll = { /* TODO: Navigate to full list */ },
                )
            }

            // Genres
            item {
                GenresSection(
                    genres = genres,
                    selectedGenre = vm.selectedGenre,
                    onGenreClick = vm::selectGenre,
                )
            }

            // Recommended (placeholder for now)
            item {
                RecommendedSection(
                    items = vm.movies.items.take(10), // Placeholder
                    onOpen = onOpen,
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    item: Item?,
    loading: Boolean,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    onFavorite: () -> Unit,
    isFavorite: Boolean,
) {
    if (loading && item == null) {
        // Loading skeleton for hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(com.fkbox.app.ui.theme.DesignTokens.Shape.LG)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            // Shimmer placeholder
        }
        return
    }

    item?.let { movie ->
        com.fkbox.app.ui.common.FeaturedHeroCard(
            item = movie,
            onPlay = onPlay,
            onDetails = onDetails,
            onFavorite = onFavorite,
            isFavorite = isFavorite,
        )
    }
}

@Composable
private fun ContinueWatchingSection(
    items: List<Item>,
    onOpen: (String) -> Unit,
) {
    if (items.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(
            title = stringResource(R.string.continue_watching),
            actionLabel = stringResource(R.string.see_all),
            onAction = { /* TODO */ },
        )
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { item ->
                HorizontalPosterCard(
                    item = item,
                    progress = 0f, // TODO: Load from local storage
                    onClick = { onOpen(item.id) },
                )
            }
        }
    }
}

@Composable
private fun TrendingSection(
    items: List<Item>,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onOpen: (String) -> Unit,
) {
    if (items.isEmpty() && !loading && error == null) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(
            title = stringResource(R.string.trending_now),
        )

        if (loading) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(6) { 
                    SkeletonPoster(modifier = Modifier.width(150.dp))
                }
            }
        } else if (error != null) {
            com.fkbox.app.ui.common.ErrorState(
                message = error,
                onRetry = onRetry,
            )
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    PosterCard(item = item, onClick = { onOpen(item.id) }, modifier = Modifier.width(150.dp))
                }
            }
        }
    }
}

@Composable
private fun ContentSection(
    title: String,
    items: List<Item>,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onOpen: (String) -> Unit,
    showSeeAll: Boolean = false,
    onSeeAll: (() -> Unit)? = null,
) {
    if (items.isEmpty() && !loading && error == null) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(
            title = title,
            actionLabel = if (showSeeAll) stringResource(R.string.see_all) else null,
            onAction = onSeeAll,
        )

        if (loading) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(6) { 
                    SkeletonPoster(modifier = Modifier.width(150.dp))
                }
            }
        } else if (error != null) {
            com.fkbox.app.ui.common.ErrorState(
                message = error,
                onRetry = onRetry,
            )
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    PosterCard(item = item, onClick = { onOpen(item.id) }, modifier = Modifier.width(150.dp))
                }
            }
        }
    }
}

@Composable
private fun GenresSection(
    genres: List<HomeRequests.Genre>,
    selectedGenre: HomeRequests.Genre?,
    onGenreClick: (HomeRequests.Genre) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(title = stringResource(R.string.genres))

        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(genres, key = { it.label }) { genre ->
                FilterChip(
                    label = genre.label,
                    selected = selectedGenre?.label == genre.label,
                    onClick = { onGenreClick(genre) },
                )
            }
        }
    }
}

@Composable
private fun RecommendedSection(
    items: List<Item>,
    onOpen: (String) -> Unit,
) {
    if (items.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(
            title = stringResource(R.string.recommended_for_you),
        )

        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { item ->
                PosterCard(item = item, onClick = { onOpen(item.id) }, modifier = Modifier.width(150.dp))
            }
        }
    }
}

// Search field component
@Composable
fun HomeSearchField(onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        singleLine = true,
        shape = CircleShape,
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }) {
                    Icon(Icons.Rounded.Close, null)
                }
            }
        },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = {
            if (query.isNotBlank()) onSearch(query.trim())
        }),
        colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

@Composable
fun GenreDropdown(vm: HomeViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val selected = vm.selectedGenre ?: HomeRequests.genres.first()

    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            shape = CircleShape,
            label = { Text(stringResource(R.string.genres)) },
            leadingIcon = { Icon(Icons.Rounded.PlayCircle, null) },
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .height(64.dp),
            colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        )
        androidx.compose.material3.ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            vm.genres.forEach { genre ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(genre.label) },
                    onClick = {
                        vm.selectGenre(genre)
                        expanded = false
                    },
                    modifier = Modifier.height(48.dp),
                )
            }
        }
    }
}