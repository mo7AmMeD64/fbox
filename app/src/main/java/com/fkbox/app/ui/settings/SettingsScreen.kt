package com.fkbox.app.ui.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fkbox.app.BuildConfig
import com.fkbox.app.R
import com.fkbox.app.data.moviebox.HOST_POOL
import com.fkbox.app.fkApp
import com.fkbox.app.ui.common.Body
import com.fkbox.app.ui.common.Heading
import com.fkbox.app.ui.common.LocalSnack
import com.fkbox.app.ui.common.PrimaryButton
import com.fkbox.app.ui.common.SectionHeader
import com.fkbox.app.ui.theme.DesignTokens

@Composable
fun SettingsScreen(contentPadding: PaddingValues) {
    val app = LocalContext.current.fkApp
    val prefs = app.prefs
    val snack = LocalSnack.current

    val adult by prefs.adult.collectAsStateWithLifecycle()
    val host by prefs.host.collectAsStateWithLifecycle()
    val hwdec by prefs.hwdec.collectAsStateWithLifecycle()
    val tls by prefs.strictTls.collectAsStateWithLifecycle()
    val subLangs by prefs.subLangs.collectAsStateWithLifecycle()
    val saved by prefs.favorites.collectAsStateWithLifecycle()

    var confirmAdult by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    val clearedMsg = stringResource(R.string.settings_clear_saved_done)
    val resetMsg = stringResource(R.string.settings_reset_token_done)

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item { Heading(stringResource(R.string.nav_settings), style = "headlineLarge") }

            // General Section
            item {
                SettingsSection(title = stringResource(R.string.general)) {
                    SettingRow(
                        title = stringResource(R.string.settings_adult),
                        subtitle = stringResource(R.string.settings_adult_desc),
                        trailing = {
                            Switch(
                                checked = adult,
                                onCheckedChange = { on ->
                                    if (on) confirmAdult = true else prefs.setAdult(false)
                                },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            )
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.language),
                        subtitle = "System default",
                        trailing = {
                            // TODO: Language picker
                            Text("English", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.theme),
                        subtitle = "Dark (OLED optimized)",
                        trailing = {
                            Text("Dark", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                    )
                }
            }

            // Playback Section
            item {
                SettingsSection(title = stringResource(R.string.playback)) {
                    SettingRow(
                        title = stringResource(R.string.settings_hwdec),
                        subtitle = stringResource(R.string.settings_hwdec_desc),
                        trailing = {
                            Switch(
                                checked = hwdec,
                                onCheckedChange = prefs::setHwdec,
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.default_quality),
                        subtitle = "Auto (Adaptive)",
                        trailing = {
                            Text("Auto", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.auto_play),
                        subtitle = "Automatically play next episode",
                        trailing = {
                            Switch(
                                checked = false, // TODO: Add to prefs
                                onCheckedChange = { },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.auto_next_episode),
                        subtitle = "Continue to next episode automatically",
                        trailing = {
                            Switch(
                                checked = false, // TODO: Add to prefs
                                onCheckedChange = { },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        },
                    )
                }
            }

            // Subtitles Section
            item {
                SettingsSection(title = stringResource(R.string.subtitles)) {
                    SettingRow(
                        title = stringResource(R.string.preferred_language),
                        subtitle = stringResource(R.string.settings_sub_lang_desc),
                        trailing = {
                            OutlinedTextField(
                                value = subLangs,
                                onValueChange = prefs::setSubLangs,
                                readOnly = false,
                                singleLine = true,
                                shape = CircleShape,
                                modifier = Modifier.width(200.dp),
                                colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                ),
                            )
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.subtitle_size),
                        subtitle = "Medium",
                        trailing = {
                            Text("Medium", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.subtitle_style),
                        subtitle = "Default",
                        trailing = {
                            Text("Default", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                    )
                }
            }

            // Network Section
            item {
                SettingsSection(title = stringResource(R.string.network)) {
                    SettingRow(
                        title = stringResource(R.string.server),
                        subtitle = stringResource(R.string.settings_server_desc),
                        trailing = {
                            ExposedDropdownMenuHost(
                                selected = host.removePrefix("https://"),
                                options = HOST_POOL.map { it.removePrefix("https://") },
                                onSelect = prefs::setHost,
                            )
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.tls_verification),
                        subtitle = stringResource(R.string.settings_tls_desc),
                        trailing = {
                            Switch(
                                checked = tls,
                                onCheckedChange = prefs::setStrictTls,
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        },
                    )

                    SettingRow(
                        title = stringResource(R.string.failover),
                        subtitle = "Automatically switch servers on failure",
                        trailing = {
                            Switch(
                                checked = true, // TODO: Add to prefs
                                onCheckedChange = { },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        },
                    )
                }
            }

            // Storage Section
            item {
                SettingsSection(title = stringResource(R.string.storage)) {
                    SettingRow(
                        title = stringResource(R.string.clear_cache),
                        subtitle = "Clear cached images and temporary data",
                        trailing = {
                            Text("0 MB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        onClick = { /* TODO: Clear cache */ },
                    )

                    SettingRow(
                        title = stringResource(R.string.clear_history),
                        subtitle = "Clear watch history",
                        onClick = { /* TODO: Clear history */ },
                    )

                    SettingRow(
                        title = stringResource(R.string.settings_clear_saved),
                        subtitle = stringResource(R.string.settings_clear_saved_confirm, saved.size),
                        onClick = { if (saved.isNotEmpty()) confirmClear = true },
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = stringResource(R.string.about)) {
                    SettingRow(
                        title = stringResource(R.string.app_name),
                        subtitle = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    )

                    SettingRow(
                        title = stringResource(R.string.credits),
                        subtitle = "Built with Kotlin, Compose, MPV",
                        onClick = { /* TODO: Show credits */ },
                    )

                    SettingRow(
                        title = stringResource(R.string.open_source_licenses),
                        subtitle = "View open source licenses",
                        onClick = { /* TODO: Show licenses */ },
                    )

                    SettingRow(
                        title = stringResource(R.string.settings_reset_token),
                        subtitle = stringResource(R.string.settings_reset_token_desc),
                        onClick = {
                            app.client.resetToken()
                            snack(resetMsg, null, null)
                        },
                    )
                }
            }
        }
    }

    if (confirmAdult) {
        AlertDialog(
            onDismissRequest = { confirmAdult = false },
            title = { Text(stringResource(R.string.settings_adult_confirm_title)) },
            text = { Text(stringResource(R.string.settings_adult_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { prefs.setAdult(true); confirmAdult = false }) {
                    Text(stringResource(R.string.settings_enable))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmAdult = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.settings_clear_saved)) },
            text = { Text(stringResource(R.string.settings_clear_saved_confirm, saved.size)) },
            confirmButton = {
                TextButton(onClick = {
                    prefs.clearFavorites()
                    confirmClear = false
                    snack(clearedMsg, null, null)
                }) { Text(stringResource(R.string.clear)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        SectionHeader(title = title)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = DesignTokens.Shape.LG,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val source = if (onClick != null) remember { MutableInteractionSource() } else null
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.pressScale(source!!).clickable { onClick() } else it },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        supportingContent = subtitle?.let {
            { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
    )
}

@Composable
private fun ExposedDropdownMenuHost(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.width(200.dp),
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            shape = CircleShape,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .height(56.dp),
            colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        )
        androidx.compose.material3.ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect("https://$option"); expanded = false },
                    modifier = Modifier.height(48.dp),
                )
            }
        }
    }
}