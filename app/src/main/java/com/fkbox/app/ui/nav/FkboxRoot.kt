package com.fkbox.app.ui.nav

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fkbox.app.R
import com.fkbox.app.ui.common.LocalSnack
import com.fkbox.app.ui.common.pressScale
import com.fkbox.app.ui.details.DetailsScreen
import com.fkbox.app.ui.details.PlayRequest
import com.fkbox.app.ui.home.HomeScreen
import com.fkbox.app.ui.library.LibraryScreen
import com.fkbox.app.ui.search.SearchScreen
import com.fkbox.app.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

private enum class Tab(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    Home("home", R.string.nav_home, Icons.Rounded.Home),
    Search("search", R.string.nav_search, Icons.Rounded.Search),
    Library("library", R.string.library, Icons.Rounded.LibraryAdd),
    Settings("settings", R.string.nav_settings, Icons.Rounded.Settings),
}

private const val ROUTE_SEARCH = "search?query={query}"
private const val ROUTE_DETAILS = "details/{id}"

@Composable
fun FkboxRoot(onPlay: (PlayRequest) -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route?.substringBefore('?')?.substringBefore('/')
    val showBar = Tab.entries.any { it.route == current }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snack: (String, String?, (() -> Unit)?) -> Unit = remember {
        { message, action, onAction ->
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                val result = snackbar.showSnackbar(
                    message = message,
                    actionLabel = action,
                    withDismissAction = action == null,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) onAction?.invoke()
            }
        }
    }

    // Light spring bounce on every transition; back plays the same motion reversed.
    val enter: EnterTransition = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
        scaleIn(initialScale = 0.94f, animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow))
    val exit: ExitTransition = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
        scaleOut(targetScale = 0.94f, animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium))

    CompositionLocalProvider(LocalSnack provides snack) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = {
                SnackbarHost(snackbar) { data -> Snackbar(data, shape = RoundedCornerShape(20.dp)) }
            },
            bottomBar = {
                if (showBar) {
                    FkNavigationBar(current = current) { tab ->
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Tab.Home.route,
                enterTransition = { enter },
                exitTransition = { exit },
                popEnterTransition = { enter },
                popExitTransition = { exit },
            ) {
                composable(Tab.Home.route) {
                    HomeScreen(
                        contentPadding = padding,
                        onOpen = { id -> nav.navigate("details/${Uri.encode(id)}") },
                        onSearch = { q ->
                            nav.navigate("search?query=${Uri.encode(q)}") { launchSingleTop = true }
                        },
                    )
                }
                composable(
                    route = ROUTE_SEARCH,
                    arguments = listOf(navArgument("query") { type = NavType.StringType; defaultValue = "" }),
                ) {
                    SearchScreen(contentPadding = padding, onOpen = { id -> nav.navigate("details/${Uri.encode(id)}") })
                }
                composable(Tab.Library.route) {
                    LibraryScreen(contentPadding = padding, onOpen = { id -> nav.navigate("details/${Uri.encode(id)}") })
                }
                composable(Tab.Settings.route) { SettingsScreen(contentPadding = padding) }
                composable(
                    route = ROUTE_DETAILS,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) {
                    DetailsScreen(
                        contentPadding = padding,
                        onBack = { nav.popBackStack() },
                        onOpen = { id -> nav.navigate("details/${Uri.encode(id)}") },
                        onPlay = onPlay,
                    )
                }
            }
        }
    }
}

@Composable
private fun FkNavigationBar(current: String?, onSelect: (Tab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Tab.entries.forEach { tab ->
            val source = remember { MutableInteractionSource() }
            NavigationBarItem(
                selected = tab.route == current,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.label), style = MaterialTheme.typography.labelMedium) },
                interactionSource = source,
                modifier = Modifier.pressScale(source),
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}