package com.xuan.life.android.ui.screen

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xuan.life.android.ui.model.PostCommentModel
import com.xuan.life.android.ui.model.PostDetailModel
import com.xuan.life.android.ui.model.PostLikedUserModel
import com.xuan.life.android.ui.model.PostReplyModel
import com.xuan.life.android.ui.model.PostRepostModel
import coil.compose.AsyncImage

private const val EMOJI_RECENT_KEY = "life_user_recent_emojis"

private enum class DetailTab(val label: String) {
    REPOSTS("转发"),
    COMMENTS("评论"),
    LIKES("点赞"),
}

private data class ReplyTarget(
    val parentCommentId: Long,
    val replyToUserId: Long,
    val displayName: String,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(
    innerPadding: PaddingValues,
    detail: PostDetailModel,
    onBack: () -> Unit,
    onToggleLike: () -> Unit,
    onRepost: () -> Unit,
    onSubmitComment: (String, Long?, Long?) -> Unit,
    onDeleteComment: (Long) -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("life_android_ui", Context.MODE_PRIVATE)
    }
    val listState = rememberLazyListState()
    var activeTab by rememberSaveable(detail.postId) { mutableStateOf(DetailTab.COMMENTS) }
    var composerVisible by rememberSaveable(detail.postId) { mutableStateOf(false) }
    var emojiPanelVisible by rememberSaveable(detail.postId) { mutableStateOf(false) }
    var commentText by rememberSaveable(detail.postId) { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<ReplyTarget?>(null) }
    var recentEmojis by remember { mutableStateOf(loadRecentEmojis(preferences)) }
    val emojis = remember {
        listOf(
            "😀", "😁", "😂", "🤣", "🥹", "😍", "😘", "😎",
            "🤔", "😭", "😡", "😴", "😇", "🥳", "🙌", "🤝",
            "👍", "👀", "👏", "🎉", "❤️", "💯", "🔥", "✨",
        )
    }
    val emojiPages = remember(emojis) { emojis.chunked(8) }
    val pagerState = rememberPagerState(pageCount = { emojiPages.size })
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun openComposer(nextReplyTarget: ReplyTarget? = null) {
        activeTab = DetailTab.COMMENTS
        replyTarget = nextReplyTarget
        composerVisible = true
    }

    fun appendEmoji(emoji: String) {
        commentText += emoji
        recentEmojis = buildRecentEmojis(emoji, recentEmojis)
        saveRecentEmojis(preferences, recentEmojis)
    }

    fun removeLastCharacter() {
        if (commentText.isNotEmpty()) {
            commentText = commentText.dropLast(1)
        }
    }

    fun submitComment() {
        val content = commentText.trim()
        if (content.isEmpty()) {
            return
        }
        onSubmitComment(content, replyTarget?.parentCommentId, replyTarget?.replyToUserId)
        commentText = ""
        emojiPanelVisible = false
        composerVisible = false
        replyTarget = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("正文") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                DetailActionButton(
                    label = "评论",
                    icon = Icons.Outlined.ChatBubbleOutline,
                    active = false,
                    onClick = { openComposer() },
                    modifier = Modifier.weight(1f),
                )
                DetailActionButton(
                    label = "点赞",
                    icon = if (detail.likedByCurrentUser) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    active = detail.likedByCurrentUser,
                    onClick = onToggleLike,
                    modifier = Modifier.weight(1f),
                )
                DetailActionButton(
                    label = "转发",
                    icon = Icons.Outlined.Repeat,
                    active = detail.repostedByCurrentUser,
                    onClick = {
                        activeTab = DetailTab.REPOSTS
                        onRepost()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) { scaffoldPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(Color(0xFFF7F8FA)),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 18.dp,
            ),
        ) {
            item {
                Surface(color = Color.White) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        PostHeaderSection(detail = detail)
                        Text(
                            text = detail.content,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1D2430),
                        )
                        if (detail.mediaUrls.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                detail.mediaUrls.take(3).forEach { mediaUrl ->
                                    AsyncImage(
                                        model = mediaUrl,
                                        contentDescription = "帖子图片",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(96.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFF3F4F6)),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            stickyHeader {
                Surface(color = Color.White, shadowElevation = 2.dp) {
                    TabRow(selectedTabIndex = activeTab.ordinal) {
                        DetailTab.entries.forEach { tab ->
                            val count = when (tab) {
                                DetailTab.REPOSTS -> detail.repostCount
                                DetailTab.COMMENTS -> detail.commentCount
                                DetailTab.LIKES -> detail.likeCount
                            }
                            Tab(
                                selected = activeTab == tab,
                                onClick = { activeTab = tab },
                                text = { Text("${tab.label} $count") },
                            )
                        }
                    }
                }
            }
            when (activeTab) {
                DetailTab.REPOSTS -> {
                    if (detail.reposts.isEmpty()) {
                        item { DetailEmptyState("暂时还没有转发") }
                    } else {
                        items(detail.reposts, key = { it.repostId }) { repost ->
                            RepostRow(repost = repost)
                            HorizontalDivider(color = Color(0xFFF1F3F7))
                        }
                    }
                }
                DetailTab.COMMENTS -> {
                    if (detail.comments.isEmpty()) {
                        item { DetailEmptyState("暂时还没有评论") }
                    } else {
                        items(detail.comments, key = { it.commentId }) { comment ->
                            CommentRow(
                                comment = comment,
                                onReply = {
                                    openComposer(
                                        ReplyTarget(
                                            parentCommentId = comment.commentId,
                                            replyToUserId = comment.userId,
                                            displayName = comment.author,
                                        ),
                                    )
                                },
                                onDelete = { onDeleteComment(comment.commentId) },
                                onReplyToReply = { reply ->
                                    openComposer(
                                        ReplyTarget(
                                            parentCommentId = comment.commentId,
                                            replyToUserId = reply.userId,
                                            displayName = reply.author,
                                        ),
                                    )
                                },
                                onDeleteReply = { reply -> onDeleteComment(reply.replyId) },
                            )
                            HorizontalDivider(color = Color(0xFFF1F3F7))
                        }
                    }
                }
                DetailTab.LIKES -> {
                    if (detail.likedUsers.isEmpty()) {
                        item { DetailEmptyState("暂时还没有点赞") }
                    } else {
                        items(detail.likedUsers, key = { it.userId }) { user ->
                            LikeRow(user = user)
                            HorizontalDivider(color = Color(0xFFF1F3F7))
                        }
                    }
                }
            }
        }
    }

    if (composerVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                composerVisible = false
                emojiPanelVisible = false
                if (commentText.isBlank()) {
                    replyTarget = null
                }
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = replyTarget?.let { "回复 ${it.displayName}" } ?: "写评论",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = { composerVisible = false }) {
                        Text("取消")
                    }
                }
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    minLines = 4,
                    maxLines = 6,
                    placeholder = {
                        Text(replyTarget?.let { "回复 ${it.displayName}..." } ?: "说点什么吧...")
                    },
                    supportingText = { Text("${commentText.length}/500") },
                )
                if (emojiPanelVisible) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (recentEmojis.isNotEmpty()) {
                                Text(
                                    text = "最近使用",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF7D8795),
                                )
                                EmojiGrid(
                                    emojis = recentEmojis,
                                    modifier = Modifier.padding(top = 8.dp),
                                    onEmojiSelected = ::appendEmoji,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            Text(
                                text = "全部表情",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF7D8795),
                            )
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(132.dp)
                                    .padding(top = 8.dp),
                            ) { pageIndex ->
                                EmojiGrid(
                                    emojis = emojiPages[pageIndex],
                                    onEmojiSelected = ::appendEmoji,
                                )
                            }
                            if (emojiPages.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    repeat(emojiPages.size) { index ->
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (pagerState.currentPage == index) Color(0xFFFF5A3C) else Color(0xFFD1D5DB),
                                                ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = { emojiPanelVisible = !emojiPanelVisible }) {
                        Icon(
                            imageVector = Icons.Outlined.Mood,
                            contentDescription = "Emoji",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Emoji")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = ::removeLastCharacter) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Backspace,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("删除")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = ::submitComment, enabled = commentText.isNotBlank()) {
                        Text("发送")
                    }
                }
            }
        }
    }

    LaunchedEffect(composerVisible) {
        if (!composerVisible) {
            emojiPanelVisible = false
            if (commentText.isBlank()) {
                replyTarget = null
            }
        }
    }
}

@Composable
private fun PostHeaderSection(detail: PostDetailModel) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { AvatarBubble(size = 42.dp) },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(detail.author, fontWeight = FontWeight.SemiBold)
                if (detail.authorBadge.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = {
                            Text(
                                text = detail.authorBadge,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = Color(0xFFFFF0E8),
                            disabledLabelColor = Color(0xFFF06D47),
                        ),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(detail.authorHandle)
                Text("·")
                Text(detail.publishedAt)
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "IP 属地",
                    modifier = Modifier.size(14.dp),
                )
                Text(detail.ipRegion)
            }
        },
    )
}

@Composable
private fun RepostRow(repost: PostRepostModel) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.White),
        leadingContent = { AvatarBubble(size = 40.dp) },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(repost.author, fontWeight = FontWeight.SemiBold)
                Text(
                    text = repost.authorHandle,
                    modifier = Modifier.padding(start = 6.dp),
                    color = Color(0xFF7D8795),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        supportingContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(repost.createdAt)
                    Text("·")
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "IP 属地",
                        modifier = Modifier.size(14.dp),
                    )
                    Text(repost.ipRegion)
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
                ) {
                    Text(
                        text = repost.content,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4B5563),
                    )
                }
            }
        },
    )
}

@Composable
private fun CommentRow(
    comment: PostCommentModel,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    onReplyToReply: (PostReplyModel) -> Unit,
    onDeleteReply: (PostReplyModel) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.White),
            leadingContent = { AvatarBubble(size = 40.dp) },
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.author, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = comment.authorHandle,
                        modifier = Modifier.padding(start = 6.dp),
                        color = Color(0xFF7D8795),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            supportingContent = {
                Column {
                    Text(
                        text = comment.content,
                        modifier = Modifier.padding(top = 2.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1D2430),
                    )
                    MetaActionRow(
                        createdAt = comment.createdAt,
                        ipRegion = comment.ipRegion,
                        canDelete = comment.isOwnedByCurrentUser,
                        onReply = onReply,
                        onDelete = onDelete,
                    )
                }
            },
        )
        if (comment.replies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(start = 70.dp, end = 16.dp, bottom = 8.dp)
                    .background(Color.Transparent),
            ) {
                comment.replies.forEach { reply ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(42.dp)
                                .background(Color(0xFFF1F3F7)),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = reply.author,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = reply.authorHandle,
                                    modifier = Modifier.padding(start = 6.dp),
                                    color = Color(0xFF7D8795),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Text(
                                text = "回复 ${reply.replyToAuthor}：${reply.content}",
                                modifier = Modifier.padding(top = 2.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1D2430),
                            )
                            MetaActionRow(
                                createdAt = reply.createdAt,
                                ipRegion = reply.ipRegion,
                                canDelete = reply.isOwnedByCurrentUser,
                                onReply = { onReplyToReply(reply) },
                                onDelete = { onDeleteReply(reply) },
                                compact = true,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LikeRow(user: PostLikedUserModel) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.White),
        leadingContent = { AvatarBubble(size = 40.dp) },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.author, fontWeight = FontWeight.SemiBold)
                Text(
                    text = user.authorHandle,
                    modifier = Modifier.padding(start = 6.dp),
                    color = Color(0xFF7D8795),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = "已点赞",
                    tint = Color(0xFFFF5A3C),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = " 点赞了这条内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7D8795),
                )
            }
        },
    )
}

@Composable
private fun MetaActionRow(
    createdAt: String,
    ipRegion: String,
    canDelete: Boolean,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    compact: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = createdAt,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
            color = Color(0xFF7D8795),
        )
        Text(" · ", color = Color(0xFFC4C7CE))
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = "IP 属地",
            tint = Color(0xFF7D8795),
            modifier = Modifier.size(if (compact) 12.dp else 14.dp),
        )
        Text(
            text = ipRegion,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
            color = Color(0xFF7D8795),
            modifier = Modifier.padding(start = 3.dp),
        )
        TextButton(onClick = onReply, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
            Text("回复")
        }
        if (canDelete) {
            TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                Text("删除", color = Color(0xFFE05A37))
            }
        }
    }
}

@Composable
private fun EmojiGrid(
    emojis: List<String>,
    modifier: Modifier = Modifier,
    onEmojiSelected: (String) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        emojis.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { emoji ->
                    OutlinedButton(
                        onClick = { onEmojiSelected(emoji) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(emoji)
                    }
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, modifier = modifier.height(52.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) Color(0xFFFF5A3C) else Color(0xFF7D8795),
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (active) Color(0xFFFF5A3C) else Color(0xFF7D8795),
        )
    }
}

@Composable
private fun DetailEmptyState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = text,
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFF7D8795),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AvatarBubble(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD3C3), Color(0xFFFF8B68)),
                ),
            ),
    )
}

private fun loadRecentEmojis(preferences: android.content.SharedPreferences): List<String> {
    val value = preferences.getString(EMOJI_RECENT_KEY, "") ?: ""
    if (value.isBlank()) {
        return emptyList()
    }
    return value.split("|").filter { it.isNotBlank() }.take(8)
}

private fun saveRecentEmojis(
    preferences: android.content.SharedPreferences,
    emojis: List<String>,
) {
    preferences.edit().putString(EMOJI_RECENT_KEY, emojis.joinToString("|")).apply()
}

private fun buildRecentEmojis(emoji: String, current: List<String>): List<String> {
    return listOf(emoji) + current.filterNot { it == emoji }.take(7)
}
