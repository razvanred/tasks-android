/*
 * Copyright 2021 Răzvan Roșu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.sedici.tasks.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DonutSmall
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DonutSmall
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import app.sedici.tasks.R
import app.sedici.tasks.ui.AppNavigation
import app.sedici.tasks.ui.Screen

@Composable
internal fun HomeScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val currentSelectedItem by navController.currentScreenAsState()

            HomeBottomNavigation(
                selectedNavigation = currentSelectedItem,
                onNavigationSelected = { selected ->
                    navController.navigate(selected.route) {
                        launchSingleTop = true
                        restoreState = true

                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavigation(
                navController = navController,
            )
        }
    }
}

/**
 * Adds an [NavController.OnDestinationChangedListener] to this [NavController] and updates the
 * returned [State] which is updated as the destination changes.
 */
@Stable
@Composable
private fun NavController.currentScreenAsState(): State<Screen> {
    val selectedItem = remember { mutableStateOf<Screen>(Screen.Tasks) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hierarchy.any { it.route == Screen.Tasks.route } -> {
                    selectedItem.value = Screen.Tasks
                }
                destination.hierarchy.any { it.route == Screen.Stats.route } -> {
                    selectedItem.value = Screen.Stats
                }
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

@Composable
private fun HomeBottomNavigation(
    modifier: Modifier = Modifier,
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
) {
    BottomNavigation(modifier = modifier) {
        HomeBottomNavigationItem(
            label = { Text(text = stringResource(R.string.tasks_title)) },
            selected = selectedNavigation == Screen.Tasks,
            onClick = { onNavigationSelected(Screen.Tasks) },
            contentDescription = stringResource(R.string.cd_tasks_title),
            selectedIcon = Icons.Filled.CheckCircle,
            icon = Icons.Outlined.CheckCircle
        )

        HomeBottomNavigationItem(
            label = { Text(text = stringResource(R.string.stats_title)) },
            selected = selectedNavigation == Screen.Stats,
            onClick = { onNavigationSelected(Screen.Stats) },
            contentDescription = stringResource(R.string.cd_stats_title),
            selectedIcon = Icons.Filled.DonutSmall,
            icon = Icons.Outlined.DonutSmall,
        )
    }
}

@Composable
private fun RowScope.HomeBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedPainter: Painter? = null,
    painter: Painter,
    contentDescription: String,
    label: @Composable () -> Unit,
) {
    BottomNavigationItem(
        selected = selected,
        onClick = onClick,
        icon = {
            if (selectedPainter != null) {
                Crossfade(targetState = selected) { selected ->
                    Icon(
                        painter = if (selected) selectedPainter else painter,
                        contentDescription = contentDescription
                    )
                }
            } else {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription
                )
            }
        },
        label = label,
    )
}

@Composable
private fun RowScope.HomeBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector? = null,
    icon: ImageVector,
    contentDescription: String,
    label: @Composable () -> Unit,
) {
    HomeBottomNavigationItem(
        selected = selected,
        onClick = onClick,
        selectedPainter = selectedIcon?.let { rememberVectorPainter(it) },
        painter = rememberVectorPainter(icon),
        contentDescription = contentDescription,
        label = label
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}
