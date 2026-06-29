package com.xuan.life.android.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.xuan.life.android.ui.navigation.MainTab

@Composable
fun BottomTabBar(
    tabs: List<MainTab>,
    currentRoute: String,
    onTabSelected: (String) -> Unit,
) {
    val raisedSpace = 18.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color(0x1F1A2438),
                    spotColor = Color(0x1F1A2438),
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFEFEFE), Color(0xFFF7F8FB)),
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
                // 为激活态图标上浮预留顶部空间，避免视觉上“超出后被截断”。
                .padding(start = 8.dp, end = 8.dp, top = raisedSpace, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            tabs.forEach { tab ->
                BottomTabItem(
                    tab = tab,
                    active = currentRoute == tab.route,
                    onClick = { onTabSelected(tab.route) },
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    tab: MainTab,
    active: Boolean,
    onClick: () -> Unit,
) {
    val translateY = animateDpAsState(targetValue = if (active) (-16).dp else 0.dp, label = "tab-offset")
    val labelOffset = animateDpAsState(targetValue = if (active) (-4).dp else 0.dp, label = "label-offset")
    val elevation = animateDpAsState(targetValue = if (active) 16.dp else 0.dp, label = "tab-elevation")
    val iconScale = animateFloatAsState(targetValue = if (active) 1.02f else 1f, label = "tab-scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .wrapContentHeight()
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                // Compose 不允许负 padding，这里用 offset 做激活态上浮，避免登录后首次渲染底栏时运行时崩溃。
                .offset(y = translateY.value)
                .shadow(
                    elevation = elevation.value,
                    shape = CircleShape,
                    ambientColor = Color(0x33FF5F3D),
                    spotColor = Color(0x33FF5F3D),
                )
                .background(
                    brush = if (active) {
                        Brush.linearGradient(listOf(Color(0xFFFF7A59), Color(0xFFFF4A26)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFF9FAFC), Color(0xFFF9FAFC)))
                    },
                    shape = CircleShape,
                )
                .size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = if (active) Color.White else Color(0xFF94A3B8),
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    },
            )
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) Color(0xFFFF5A3C) else Color(0xFF94A3B8),
            modifier = Modifier
                .padding(top = 6.dp)
                .offset(y = labelOffset.value),
        )
    }
}
