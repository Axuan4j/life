package com.xuan.life.android.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class FeedCardModel(
    val postId: Long,
    val author: String,
    val authorHandle: String,
    val authorBadge: String,
    val ipRegion: String,
    val tag: String,
    val topic: String,
    val content: String,
    val location: String,
    val publishedAt: String,
    val likeCount: Int,
    val commentCount: Int,
    val repostCount: Int,
    val mediaUrls: List<String>,
)
