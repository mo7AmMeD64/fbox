package com.fkbox.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fkbox.app.R
import com.fkbox.app.data.moviebox.Item
import com.fkbox.app.ui.common.CoverImage
import com.fkbox.app.ui.common.Heading
import com.fkbox.app.ui.common.LoadingBlock
import com.fkbox.app.ui.common.MessageState
import com.fkbox.app.ui.common.PosterCard
import com.fkbox.app.ui.common.pressScale

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onOpen: (String) -> Unit,
    onSearch: (String) -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { HomeSearchField(onSearch) }
            item { GenreDropdown(vm) }
            item { FeaturedCard(vm.featured, loading = vm.movies.loading && vm.series.loading, onOpen = onOpen) }

            item { Heading(stringResource(R.string.movie)) }
            item { SectionRow(vm.movies, onOpen, onRetry = vm::load) }

            item { Heading(stringResource(R.string.series)) }
            item { SectionRow(vm.series, onOpen, onRetry = vm::load) }
        }
    }
}

@Composable
private fun HomeSearchField(onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        singleLine = true,
        shape = CircleShape,
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, null) }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            if (query.isNotBlank()) onSearch(query.trim())
        }),
    )
}

@Composable
private fun GenreDropdown(vm: HomeViewModel) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = vm.selectedGenre?.label ?: "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            shape = CircleShape,
            label = { Text(stringResource(R.string.genres)) },
            leadingIcon = { Icon(Icons.Rounded.PlayCircle, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .height(64.dp),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            vm.genres.forEach { genre ->
                DropdownMenuItem(
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

/** Filled 240dp card: image on top, headline + supporting text below. Shows the top trending title. */
@Composable
private fun FeaturedCard(item: Item?, loading: Boolean, onOpen: (String) -> Unit) {
    val source = remember { MutableInteractionSource() }
    Card(
        onClick = { item?.let { onOpen(it.id) } },
        enabled = item != null,
        modifier = Modifier.fillMaxWidth().height(240.dp).pressScale(source),
        shape = RoundedCornerShape(35.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        interactionSource = source,
    ) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            CoverImage(
                url = item?.posterUrl,
                contentDescription = item?.title,
                modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(20.dp)),
            )
            Column(Modifier.padding(top = 12.dp)) {
                Text(
                    text = item?.title ?: stringResource(if (loading) R.string.loading else R.string.trending),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item?.let { featuredSubtitle(it) } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun featuredSubtitle(item: Item): String {
    val type = stringResource(if (item.isSeries) R.string.type_series else R.string.type_movie)
    return if (item.imdb != null) "${stringResource(R.string.trending)} • $type • ${String.format("%.1f", item.imdb)} imdb"
    else "${stringResource(R.string.trending)} • $type"
}

@Composable
private fun SectionRow(section: Section, onOpen: (String) -> Unit, onRetry: () -> Unit) {
    when {
        section.loading -> LoadingBlock()
        section.error != null -> MessageState(
            icon = Icons.Rounded.Search,
            title = stringResource(R.string.error_generic, section.error),
            actionLabel = stringResource(R.string.retry),
            onAction = onRetry,
        )
        section.items.isEmpty() -> Text(
            stringResource(R.string.nothing_here),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(section.items, key = { it.id }) { item ->
                PosterCard(item = item, onClick = { onOpen(item.id) }, modifier = Modifier.width(150.dp))
            }
        }
    }
}
