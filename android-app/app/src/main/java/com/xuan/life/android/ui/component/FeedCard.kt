package com.xuan.life.android.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xuan.life.android.ui.model.FeedCardModel
import coil.compose.AsyncImage

@Composable
fun FeedCard(
    item: FeedCardModel,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD3C3), Color(0xFFFF8B68)),
                            ),
                        ),
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.author,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = {
                                Text(
                                    text = item.authorBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = if (item.tag == "推荐") Color(0xFFFFF0E8) else Color(0xFFEFF5FF),
                                disabledLabelColor = if (item.tag == "推荐") Color(0xFFF06D47) else Color(0xFF3D78F2),
                            ),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = item.publishedAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7D8795),
                        )
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "IP 属地",
                            tint = Color(0xFF7D8795),
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = item.ipRegion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7D8795),
                        )
                    }
                }
                Text(text = "···", color = Color(0xFF9CA3AF))
            }

            if (item.topic.isNotBlank()) {
                Text(
                    text = "# ${item.topic}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFF6A45),
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
            if (item.content.isNotBlank()) {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1D2430),
                    modifier = Modifier.padding(top = if (item.topic.isNotBlank()) 8.dp else 14.dp),
                )
            }
            if (item.mediaUrls.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item.mediaUrls.take(3).forEach { mediaUrl ->
                        AsyncImage(
                            model = mediaUrl,
                            contentDescription = "帖子图片",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(92.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF3F4F6)),
                        )
                    }
                }
            }
            Text(
                text = item.location,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7D8795),
                modifier = Modifier.padding(top = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FeedStat(icon = Icons.Outlined.FavoriteBorder, value = item.likeCount)
                FeedStat(icon = Icons.Outlined.ChatBubbleOutline, value = item.commentCount)
                FeedStat(icon = Icons.Outlined.Repeat, value = item.repostCount)
            }
        }
    }
}

@Composable
private fun FeedStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF7D8795),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = value.toString(),
            color = Color(0xFF7D8795),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
