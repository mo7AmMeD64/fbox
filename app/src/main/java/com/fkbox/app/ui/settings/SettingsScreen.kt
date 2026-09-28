package com.fkbox.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.fkbox.app.ui.common.Heading
import com.fkbox.app.ui.common.LocalSnack
import com.fkbox.app.ui.common.clickableScale
import com.fkbox.app.ui.common.pressScale

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
            item { Heading(stringResource(R.string.nav_settings)) }

            item { SectionLabel(stringResource(R.string.settings_content)) }
            item {
                SettingCard {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_adult)) },
                        supportingContent = { Text(stringResource(R.string.settings_adult_desc)) },
                        trailingContent = {
                            Switch(checked = adult, onCheckedChange = { on ->
                                if (on) confirmAdult = true else prefs.setAdult(false)
                            })
                        },
                        colors = transparentListColors(),
                    )
                }
            }

            item { SectionLabel(stringResource(R.string.settings_server)) }
            item {
                SettingCard {
                    var expanded by remember { mutableStateOf(false) }
                    Text(
                        stringResource(R.string.settings_server_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.padding(16.dp),
                    ) {
                        OutlinedTextField(
                            value = host.removePrefix("https://"),
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            shape = CircleShape,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .height(56.dp),
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            HOST_POOL.forEach { h ->
                                DropdownMenuItem(
                                    text = { Text(h.removePrefix("https://")) },
                                    onClick = { prefs.setHost(h); expanded = false },
                                    modifier = Modifier.height(48.dp),
                                )
                            }
                        }
                    }
                }
            }

            item { SectionLabel(stringResource(R.string.settings_player)) }
            item {
                SettingCard {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_hwdec)) },
                        supportingContent = { Text(stringResource(R.string.settings_hwdec_desc)) },
                        trailingContent = { Switch(checked = hwdec, onCheckedChange = prefs::setHwdec) },
                        colors = transparentListColors(),
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_tls)) },
                        supportingContent = { Text(stringResource(R.string.settings_tls_desc)) },
                        trailingContent = { Switch(checked = tls, onCheckedChange = prefs::setStrictTls) },
                        colors = transparentListColors(),
                    )
                    OutlinedTextField(
                        value = subLangs,
                        onValueChange = prefs::setSubLangs,
                        label = { Text(stringResource(R.string.settings_sub_lang)) },
                        supportingText = { Text(stringResource(R.string.settings_sub_lang_desc)) },
                        singleLine = true,
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    )
                }
            }

            item { SectionLabel(stringResource(R.string.settings_data)) }
            item {
                SettingCard {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_clear_saved)) },
                        supportingContent = { Text(stringResource(R.string.settings_clear_saved_confirm, saved.size)) },
                        modifier = Modifier.clickableScale { if (saved.isNotEmpty()) confirmClear = true },
                        colors = transparentListColors(),
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_reset_token)) },
                        supportingContent = { Text(stringResource(R.string.settings_reset_token_desc)) },
                        modifier = Modifier.clickableScale {
                            app.client.resetToken()
                            snack(resetMsg, null, null)
                        },
                        colors = transparentListColors(),
                    )
                }
            }

            item { SectionLabel(stringResource(R.string.settings_about)) }
            item {
                SettingCard {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.app_name)) },
                        supportingContent = { Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME)) },
                        colors = transparentListColors(),
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
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
    )
}

@Composable
private fun SettingCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        content = content,
    )
}

@Composable
private fun transparentListColors() =
    ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
