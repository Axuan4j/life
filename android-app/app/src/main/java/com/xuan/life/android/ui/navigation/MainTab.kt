package com.xuan.life.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

object MainTabRoutes {
    const val HOME = "home"
    const val DISCOVER = "discover"
    const val COMPOSE = "compose"
    const val MESSAGE = "message"
    const val PROFILE = "profile"
}

val mainTabs = listOf(
    MainTab(MainTabRoutes.HOME, "首页", Icons.Outlined.Home),
    MainTab(MainTabRoutes.DISCOVER, "发现", Icons.Outlined.Explore),
    MainTab(MainTabRoutes.COMPOSE, "发帖", Icons.Outlined.Add),
    MainTab(MainTabRoutes.MESSAGE, "消息", Icons.Outlined.ChatBubbleOutline),
    MainTab(MainTabRoutes.PROFILE, "我的", Icons.Outlined.Person),
)
