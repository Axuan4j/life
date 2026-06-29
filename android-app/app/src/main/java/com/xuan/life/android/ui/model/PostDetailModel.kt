package com.xuan.life.android.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class PostDetailModel(
    val postId: Long,
    val author: String,
    val authorHandle: String,
    val authorBadge: String,
    val ipRegion: String,
    val publishedAt: String,
    val content: String,
    val topic: String,
    val mediaUrls: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val repostCount: Int,
    val likedByCurrentUser: Boolean,
    val repostedByCurrentUser: Boolean,
    val comments: List<PostCommentModel>,
    val likedUsers: List<PostLikedUserModel>,
    val reposts: List<PostRepostModel>,
)

@Immutable
data class PostCommentModel(
    val commentId: Long,
    val userId: Long,
    val author: String,
    val authorHandle: String,
    val ipRegion: String,
    val createdAt: String,
    val content: String,
    val isOwnedByCurrentUser: Boolean,
    val replies: List<PostReplyModel>,
)

@Immutable
data class PostReplyModel(
    val replyId: Long,
    val userId: Long,
    val author: String,
    val authorHandle: String,
    val replyToAuthor: String,
    val replyToUserId: Long?,
    val ipRegion: String,
    val createdAt: String,
    val content: String,
    val isOwnedByCurrentUser: Boolean,
)

@Immutable
data class PostLikedUserModel(
    val userId: Long,
    val author: String,
    val authorHandle: String,
)

@Immutable
data class PostRepostModel(
    val repostId: Long,
    val author: String,
    val authorHandle: String,
    val ipRegion: String,
    val createdAt: String,
    val content: String,
)
