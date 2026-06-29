package com.xuan.life.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xuan.life.android.ui.component.ProfileStat
import com.xuan.life.android.ui.model.ProfileUiModel

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    profile: ProfileUiModel?,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
) {
    if (isLoading && profile == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFFF8F2)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Color(0xFFFF5A3C))
        }
        return
    }

    if (!errorMessage.isNullOrBlank() && profile == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFFF8F2)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(errorMessage, color = Color(0xFF7D8795))
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Text("重新加载")
                }
            }
        }
        return
    }

    val currentProfile = profile ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Color(0xFFFFF8F2)),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            // 资料头部直接使用真实用户资料，保持与 Web 端同一套“内容社区”基调。
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFE5D8), Color(0xFFFFFAF7)),
                        ),
                    )
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    HeaderIcon(Icons.Outlined.Search)
                    HeaderIcon(Icons.Outlined.Settings, modifier = Modifier.padding(start = 10.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF7448), Color(0xFFFFB36B)),
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentProfile.nickname,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "@${currentProfile.username}",
                                color = Color(0xFF98A2B3),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        Text(
                            text = currentProfile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF7D8795),
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            text = "最近登录地 ${currentProfile.lastLoginRegion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF98A2B3),
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.78f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        ProfileStat("帖子", currentProfile.postCount)
                        ProfileStat("关注", currentProfile.followingCount)
                        ProfileStat("粉丝", currentProfile.followerCount)
                        ProfileStat("获赞", currentProfile.likedCount)
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    QuickAction("我的收藏", Icons.Outlined.BookmarkBorder)
                    QuickAction("浏览历史", Icons.Outlined.History)
                    QuickAction("我的点赞", Icons.Outlined.FavoriteBorder)
                    QuickAction("草稿箱", Icons.Outlined.Drafts)
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Text(
                        text = "创作与服务",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "管理作品、查看反馈、完善账号设置",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7D8795),
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                    )
                    ProfileMenuLine("创作者中心", Icons.Outlined.Edit)
                    HorizontalDivider(color = Color(0xFFF1F3F7))
                    ProfileMenuLine("帮助与反馈", Icons.AutoMirrored.Outlined.HelpOutline)
                    HorizontalDivider(color = Color(0xFFF1F3F7))
                    ProfileMenuLine("设置", Icons.Outlined.Settings)
                }
            }
        }

        item {
            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFFF5A3C),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text("退出登录")
            }
        }
    }
}

@Composable
private fun HeaderIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.72f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
        )
    }
}

@Composable
private fun QuickAction(
    text: String,
    icon: ImageVector,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFFF3ED))
                .padding(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFFFF5A3C),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ProfileMenuLine(
    title: String,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF4B5563),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
        )
    }
}
