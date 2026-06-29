package com.xuan.life.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xuan.life.android.ui.component.FeedCard
import com.xuan.life.android.ui.model.FeedCardModel

private enum class FeedTab { FOLLOWING, RECOMMENDED }

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    feedItems: List<FeedCardModel>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onOpenPost: (FeedCardModel) -> Unit,
) {
    var activeTab by rememberSaveable { mutableStateOf(FeedTab.FOLLOWING) }
    val visibleItems = feedItems.filter { item ->
        if (activeTab == FeedTab.FOLLOWING) item.tag == "关注" else item.tag == "推荐"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Color(0xFFFFF8F4)),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TopTab(
                        text = "关注",
                        active = activeTab == FeedTab.FOLLOWING,
                        onClick = { activeTab = FeedTab.FOLLOWING },
                    )
                    TopTab(
                        text = "推荐",
                        active = activeTab == FeedTab.RECOMMENDED,
                        modifier = Modifier.padding(start = 22.dp),
                        onClick = { activeTab = FeedTab.RECOMMENDED },
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x14F8744E))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text("⌕", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        when {
            isLoading && feedItems.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF5A3C))
                    }
                }
            }

            !errorMessage.isNullOrBlank() && feedItems.isEmpty() -> {
                item {
                    ColumnError(
                        message = errorMessage,
                        actionText = "重新加载",
                        onAction = onRetry,
                    )
                }
            }

            visibleItems.isEmpty() -> {
                item {
                    ColumnError(
                        message = "当前分组还没有内容",
                        actionText = if (feedItems.isEmpty()) "刷新" else "",
                        onAction = onRetry,
                    )
                }
            }

            else -> {
                items(visibleItems, key = { it.postId }) { item ->
                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        FeedCard(
                            item = item,
                            onClick = { onOpenPost(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopTab(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.clickable(onClick = onClick),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        color = if (active) Color(0xFF1D2430) else Color(0xFF7D8795),
    )
}

@Composable
private fun ColumnError(
    message: String,
    actionText: String,
    onAction: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Color(0xFF7D8795))
            if (actionText.isNotBlank()) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Text(actionText)
                }
            }
        }
    }
}
