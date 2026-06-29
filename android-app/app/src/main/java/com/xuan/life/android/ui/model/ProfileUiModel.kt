package com.xuan.life.android.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileUiModel(
    val userId: Long,
    val username: String,
    val nickname: String,
    val bio: String,
    val lastLoginRegion: String,
    val postCount: String,
    val followingCount: String,
    val followerCount: String,
    val likedCount: String,
)
