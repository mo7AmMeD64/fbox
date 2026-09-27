package com.fkbox.app.player

import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fkbox.app.R
import com.fkbox.app.data.AppPrefs
import com.fkbox.app.data.moviebox.StreamLink
import com.fkbox.app.ui.common.pressScale
import com.fkbox.app.ui.details.PlayRequest
import `is`.xyz.mpv.MPVLib
import kotlinx.coroutines.delay

private enum class Sheet { SOURCES, SUBTITLES, AUDIO, QUALITY, SPEED }

@Composable
fun PlayerScreen(
    req: PlayRequest,
    prefs: AppPrefs,
    onExit: () -> Unit,
    vm: PlayerViewModel = viewModel(),
) {
    var episode by rememberSaveable { mutableIntStateOf(req.episode) }
    LaunchedEffect(episode) { vm.load(req.id, req.season, episode) }

    val hasNext = req.maxEpisode > 0 && episode < req.maxEpisode
    val title = when {
        req.season > 0 -> "${req.title} • S${req.season}E$episode"
        else -> req.title
    }

    Box(Modifier.fillMaxSize().background(com.fkbox.app.ui.theme.DesignTokens.Surface.Background)) {
        if (vm.links.isNotEmpty()) {
            PlayerContent(
                links = vm.links,
                index = vm.index,
                generation = vm.generation,
                onIndexChange = { vm.index = it },
                title = title,
                hasNext = hasNext,
                onNext = { episode += 1 },
                onExit = onExit,
                prefs = prefs,
            )
        }

        when {
            vm.loading -> Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                )
                Spacer(Modifier.size(16.dp))
                Text(
                    stringResource(R.string.player_loading_sources),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            vm.error != null && vm.links.isEmpty() -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (vm.error == PlayerViewModel.NO_SOURCES) stringResource(R.string.player_no_sources)
                    else stringResource(R.string.player_error, vm.error.orEmpty()),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onExit) { Text(stringResource(R.string.back)) }
                    val src = remember { MutableInteractionSource() }
                    Button(
                        onClick = { vm.load(req.id, req.season, episode) },
                        interactionSource = src,
                        modifier = Modifier.pressScale(src),
                    ) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
}

@Composable
private fun PlayerContent(
    links: List<StreamLink>,
    index: Int,
    generation: Int,
    onIndexChange: (Int) -> Unit,
    title: String,
    hasNext: Boolean,
    onNext: () -> Unit,
    onExit: () -> Unit,
    prefs: AppPrefs,
) {
    val context = LocalContext.current
    val safeIndex = index.coerceIn(0, links.lastIndex)
    val link = links[safeIndex]

    val state = remember { PlayerState() }
    val sub = prefs.subLangs.value.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
    var visible by remember { mutableStateOf(true) }
    var tick by remember { mutableIntStateOf(0) }
    var sheet by remember { mutableStateOf<Sheet?>(null) }

    val currentHasNext by androidx.compose.runtime.rememberUpdatedState(hasNext)
    val currentOnNext by androidx.compose.runtime.rememberUpdatedState(onNext)
    val controller = remember {
        MpvController(state, sub) {
            if (currentHasNext) currentOnNext() else visible = true
        }
    }

    // What is loaded right now, so switching source keeps the position but a new episode starts at 0.
    val loaded = remember { arrayOf<String?>(null) }
    val loadedGeneration = remember { intArrayOf(-1) }

    LaunchedEffect(link.url, generation) {
        val first = loaded[0] == null
        if (!first && (loaded[0] != link.url || loadedGeneration[0] != generation) && MpvGuard.alive) {
            val keep = if (loadedGeneration[0] == generation) state.position else 0.0
            state.position = keep
            controller.load(link, keep)
            loaded[0] = link.url
            loadedGeneration[0] = generation
        }
    }

    // Auto-hide the controls while playing.
    LaunchedEffect(visible, state.paused, tick, sheet) {
        if (visible && !state.paused && sheet == null) {
            delay(3000)
            visible = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val v = LayoutInflater.from(ctx).inflate(R.layout.view_mpv, null, false) as FkMpvView
                v.hwdec = prefs.hwdec.value
                v.strictTls = prefs.strictTls.value
                MPVLib.addObserver(controller)
                v.initialize(ctx.filesDir.path, ctx.cacheDir.path)
                MpvGuard.alive = true
                controller.subtitles = link.subtitles
                controller.applyLink(link, 0.0)
                v.playFile(link.url)
                loaded[0] = link.url
                loadedGeneration[0] = generation
                v
            },
            onRelease = { v ->
                MpvGuard.alive = false
                MPVLib.removeObserver(controller)
                v.destroy()
            },
        )

        // Tap to toggle controls, double-tap left/right to seek.
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { visible = !visible },
                        onDoubleTap = { offset ->
                            if (MpvGuard.alive) {
                                val back = offset.x < size.width / 2f
                                MPVLib.command(arrayOf("seek", if (back) "-10" else "10", "relative"))
                            }
                            visible = true
                            tick++
                        },
                    )
                }
        )

        if (state.buffering && state.error == null) {
            Box(Modifier.align(Alignment.Center)) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = com.fkbox.app.ui.theme.DesignTokens.Motion.SpringSmooth),
            exit = fadeOut(animationSpec = com.fkbox.app.ui.theme.DesignTokens.Motion.SpringSmooth),
        ) {
            Controls(
                title = title,
                state = state,
                hasNext = hasNext,
                onBack = onExit,
                onNext = onNext,
                onOpenSheet = { sheet = it; tick++ },
                onInteract = { tick++ },
            )
        }

        state.error?.let { err ->
            Column(
                Modifier
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, com.fkbox.app.ui.theme.DesignTokens.Shape.XL)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.player_error, err),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                if (safeIndex < links.lastIndex) {
                    val src = remember { MutableInteractionSource() }
                    Button(
                        onClick = { state.error = null; onIndexChange(safeIndex + 1) },
                        interactionSource = src,
                        modifier = Modifier.pressScale(src),
                    ) { Text(stringResource(R.string.player_next_source)) }
                }
            }
        }
    }

    sheet?.let { type ->
        PickerSheet(
            type = type,
            links = links,
            currentIndex = safeIndex,
            state = state,
            controller = controller,
            onSelectSource = { onIndexChange(it) },
            onDismiss = { sheet = null },
        )
    }
}

@Composable
private fun Controls(
    title: String,
    state: PlayerState,
    hasNext: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onOpenSheet: (Sheet) -> Unit,
    onInteract: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        // Top bar
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                title,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TopAction(Icons.Rounded.Language, stringResource(R.string.player_sources)) { onOpenSheet(Sheet.SOURCES) }
            TopAction(Icons.Rounded.Subtitles, stringResource(R.string.player_subtitles)) { onOpenSheet(Sheet.SUBTITLES) }
            TopAction(Icons.Rounded.Audiotrack, stringResource(R.string.player_audio)) { onOpenSheet(Sheet.AUDIO) }
            TopAction(Icons.Rounded.HighQuality, stringResource(R.string.player_quality)) { onOpenSheet(Sheet.QUALITY) }
            TopAction(Icons.Rounded.Speed, stringResource(R.string.player_speed)) { onOpenSheet(Sheet.SPEED) }
            if (hasNext) TopAction(Icons.Rounded.SkipNext, stringResource(R.string.player_next_episode), onNext)
        }

        // Centre transport
        Row(
            Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            IconButton(onClick = {
                if (MpvGuard.alive) MPVLib.command(arrayOf("seek", "-10", "relative"))
                onInteract()
            }, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.Replay10, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            val src = remember { MutableInteractionSource() }
            FilledIconButton(
                onClick = {
                    if (MpvGuard.alive) MPVLib.setPropertyBoolean("pause", !state.paused)
                    onInteract()
                },
                modifier = Modifier.size(72.dp).pressScale(src),
                interactionSource = src,
            ) {
                Icon(
                    if (state.paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
            }
            IconButton(onClick = {
                if (MpvGuard.alive) MPVLib.command(arrayOf("seek", "10", "relative"))
                onInteract()
            }, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.Forward10, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        // Seek bar
        var dragging by remember { mutableStateOf(false) }
        var dragValue by remember { mutableFloatStateOf(0f) }
        val max = state.duration.toFloat().coerceAtLeast(1f)
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatTime(if (dragging) dragValue.toDouble() else state.position),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
            )
            Slider(
                value = (if (dragging) dragValue else state.position.toFloat()).coerceIn(0f, max),
                onValueChange = { dragging = true; dragValue = it; onInteract() },
                onValueChangeFinished = {
                    if (MpvGuard.alive) MPVLib.command(arrayOf("seek", dragValue.toDouble().toString(), "absolute"))
                    dragging = false
                },
                valueRange = 0f..max,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                ),
            )
            Text(
                formatTime(state.duration),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun TopAction(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, description, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PickerSheet(
    type: Sheet,
    links: List<StreamLink>,
    currentIndex: Int,
    state: PlayerState,
    controller: MpvController,
    onSelectSource: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val off = stringResource(R.string.player_off)
    val auto = stringResource(R.string.player_auto)

    // Each row: label, selected?, action
    data class Option(val label: String, val selected: Boolean, val action: () -> Unit)

    val rows: List<Option> = when (type) {
        Sheet.SOURCES -> links.mapIndexed { i, l ->
            Option(l.name, i == currentIndex) { onSelectSource(i) }
        }
        Sheet.SUBTITLES -> {
            val subs = state.tracks.filter { it.type == "sub" }
            listOf(Option(off, subs.none { it.selected }) {
                if (MpvGuard.alive) MPVLib.setPropertyString("sid", "no")
            }) + subs.map { t ->
                Option(trackLabel(t), t.selected) {
                    if (MpvGuard.alive) MPVLib.setPropertyString("sid", t.id.toString())
                }
            }
        }
        Sheet.AUDIO -> state.tracks.filter { it.type == "audio" }.map { t ->
            Option(trackLabel(t), t.selected) {
                if (MpvGuard.alive) MPVLib.setPropertyString("aid", t.id.toString())
            }
        }
        Sheet.QUALITY -> {
            val videos = state.tracks.filter { it.type == "video" && it.height != null }
                .sortedByDescending { it.height }
            if (videos.size <= 1) listOf(Option(auto, true) {})
            else videos.map { t ->
                Option("${t.height}p", t.selected) {
                    if (MpvGuard.alive) MPVLib.setPropertyString("vid", t.id.toString())
                }
            }
        }
        Sheet.SPEED -> listOf(0.5, 0.75, 1.0, 1.25, 1.5, 2.0).map { s ->
            Option("${s}x", kotlin.math.abs(state.speed - s) < 0.01) {
                if (MpvGuard.alive) MPVLib.setPropertyDouble("speed", s)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (type) {
                    Sheet.SOURCES -> stringResource(R.string.player_sources)
                    Sheet.SUBTITLES -> stringResource(R.string.player_subtitles)
                    Sheet.AUDIO -> stringResource(R.string.player_audio)
                    Sheet.QUALITY -> stringResource(R.string.player_quality)
                    Sheet.SPEED -> stringResource(R.string.player_speed)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).fillMaxWidth(),
            )
            LazyColumn(Modifier.navigationBarsPadding()) {
                items(rows) { row ->
                    ListItem(
                        headlineContent = { Text(row.label) },
                        trailingContent = {
                            if (row.selected) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                            else Spacer(Modifier.width(24.dp))
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                row.action()
                                if (type == Sheet.SUBTITLES || type == Sheet.AUDIO || type == Sheet.QUALITY) controller.refreshTracks()
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private fun trackLabel(t: Track): String {
    val parts = listOfNotNull(t.title?.takeIf { it.isNotBlank() }, t.lang?.takeIf { it.isNotBlank() })
    return if (parts.isEmpty()) "Track ${t.id}" else parts.joinToString(" • ")
}