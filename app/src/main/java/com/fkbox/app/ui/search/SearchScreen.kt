package com.fkbox.app.ui.search

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fkbox.app.R
import com.fkbox.app.data.moviebox.Item
import com.fkbox.app.fkApp
import com.fkbox.app.ui.common.EmptyState
import com.fkbox.app.ui.common.LoadingBlock
import com.fkbox.app.ui.common.PosterCard
import com.fkbox.app.ui.common.SectionHeader
import com.fkbox.app.ui.common.SkeletonPoster
import com.fkbox.app.ui.common.pressScale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

private const val PAGE_SIZE = 20

class SearchViewModel(app: Application, handle: SavedStateHandle) : AndroidViewModel(app) {
    private val ctx = app.fkApp

    var query by mutableStateOf(handle.get<String>("query").orEmpty())
        private set
    var results by mutableStateOf<List<Item>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var searchedFor by mutableStateOf<String?>(null)
        private set
    var canLoadMore by mutableStateOf(false)
        private set

    private var page = 1
    private var job: Job? = null

    init {
        if (query.isNotBlank()) run(query.trim())
        viewModelScope.launch {
            ctx.prefs.adult.drop(1).collect { if (query.isNotBlank()) run(query.trim()) }
        }
    }

    fun onQueryChange(q: String) {
        query = q
        job?.cancel()
        if (q.isBlank()) {
            results = emptyList(); searchedFor = null; error = null; loading = false; canLoadMore = false
            return
        }
        job = viewModelScope.launch {
            delay(450) // debounce typing
            doSearch(q.trim(), 1)
        }
    }

    fun submit() {
        if (query.isBlank()) return
        job?.cancel()
        run(query.trim())
    }

    private fun run(q: String) {
        job?.cancel()
        job = viewModelScope.launch { doSearch(q, 1) }
    }

    fun loadMore() {
        val q = searchedFor ?: return
        if (loadingMore || loading || !canLoadMore) return
        job = viewModelScope.launch { doSearch(q, page + 1) }
    }

    private suspend fun doSearch(q: String, p: Int) {
        if (p == 1) { loading = true; error = null } else loadingMore = true
        try {
            val found = withContext(Dispatchers.IO) { ctx.client.search(q, p) }
            page = p
            results = if (p == 1) found else (results + found).distinctBy { it.id }
            searchedFor = q
            canLoadMore = found.size >= PAGE_SIZE / 2 && found.isNotEmpty()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: e.javaClass.simpleName
            if (p == 1) results = emptyList()
        } finally {
            loading = false
            loadingMore = false
        }
    }
}

@Composable
fun SearchScreen(
    contentPadding: PaddingValues,
    onOpen: (String) -> Unit,
    vm: SearchViewModel = viewModel(),
) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { if (vm.query.isBlank()) focus.requestFocus() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding() + 8.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = vm.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth().height(56.dp).focusRequester(focus),
                singleLine = true,
                shape = CircleShape,
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = {
                    if (vm.query.isNotEmpty()) {
                        IconButton(onClick = { vm.onQueryChange("") }) { Icon(Icons.Rounded.Close, null) }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    vm.submit()
                    keyboard?.hide()
                }),
                colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )

            Box(Modifier.weight(1f).fillMaxWidth()) {
                val bottom = contentPadding.calculateBottomPadding() + 16.dp
                when {
                    vm.loading && vm.results.isEmpty() -> LoadingBlock(Modifier.align(Alignment.Center))
                    vm.error != null -> com.fkbox.app.ui.common.ErrorState(
                        message = vm.error.orEmpty(),
                        onRetry = vm::submit,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    vm.searchedFor == null -> EmptyState(
                        icon = Icons.Rounded.Search,
                        title = stringResource(R.string.search_hint_idle),
                        modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                    )
                    vm.results.isEmpty() -> EmptyState(
                        icon = Icons.Rounded.Search,
                        title = stringResource(R.string.search_no_results, vm.searchedFor.orEmpty()),
                        modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottom),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(vm.results, key = { it.id }) { item ->
                            PosterCard(item = item, onClick = { onOpen(item.id) })
                        }
                        if (vm.canLoadMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                    if (vm.loadingMore) LoadingBlock() else {
                                        val src = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                        Button(
                                            onClick = vm::loadMore,
                                            interactionSource = src,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .pressScale(src),
                                        ) { Text(stringResource(R.string.load_more)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}