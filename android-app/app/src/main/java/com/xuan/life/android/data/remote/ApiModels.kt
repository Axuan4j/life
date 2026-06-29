package com.xuan.life.android.data.remote

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val requestId: String?,
)

data class CursorPageResponse<T>(
    val items: List<T>,
    val nextCursor: String,
    val hasMore: Boolean,
)

data class TokenPairResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
)

data class UserProfileResponse(
    val userId: Long,
    val username: String,
    val nickname: String?,
    val avatarUrl: String?,
    val bio: String?,
    val lastLoginRegion: String?,
    val postCount: Long,
    val followingCount: Long,
    val followerCount: Long,
    val likedCount: Long,
)

data class FollowStatusResponse(
    val targetUserId: Long,
    val following: Boolean,
)

data class FeedItemResponse(
    val sourceType: String,
    val post: PostCardResponse,
)

data class DiscoverHotKeywordItemResponse(
    val rank: Int,
    val keyword: String,
    val title: String,
    val trendLabel: String,
    val heatLabel: String,
)

data class DiscoverTopicSquareItemResponse(
    val topicKey: String,
    val title: String,
    val summary: String,
    val discussionCount: Long,
    val coverStyle: String,
)

data class DiscoverRecommendedAuthorItemResponse(
    val userId: Long,
    val username: String,
    val nickname: String,
    val avatarUrl: String?,
    val bio: String?,
    val followerCount: Long,
    val following: Boolean,
    val reason: String,
)

data class DiscoverHomeResponse(
    val hotKeywords: List<DiscoverHotKeywordItemResponse>,
    val topicSquare: List<DiscoverTopicSquareItemResponse>,
    val recommendedAuthors: List<DiscoverRecommendedAuthorItemResponse>,
)

data class DiscoverResultHeaderResponse(
    val resultType: String,
    val queryValue: String,
    val title: String,
    val subtitle: String,
    val totalCount: Long,
)

data class DiscoverResultPageResponse(
    val header: DiscoverResultHeaderResponse,
    val items: List<FeedItemResponse>,
    val nextCursor: String,
    val hasMore: Boolean,
)

data class PostMediaResponse(
    val mediaType: String,
    val mediaUrl: String,
    val sortOrder: Int,
)

data class PostCardResponse(
    val postId: Long,
    val authorId: Long,
    val authorUsername: String,
    val authorNickname: String?,
    val authorAvatarUrl: String?,
    val ipRegion: String?,
    val contentText: String,
    val publishedAt: String,
    val likeCount: Long,
    val commentCount: Long,
    val repostCount: Long,
    val medias: List<PostMediaResponse> = emptyList(),
)

data class PostLikedUserResponse(
    val userId: Long,
    val username: String,
    val nickname: String?,
    val avatarUrl: String?,
)

data class PostInteractionResponse(
    val likeCount: Long,
    val commentCount: Long,
    val repostCount: Long,
    val likedByCurrentUser: Boolean,
    val repostedByCurrentUser: Boolean,
    val likedUsers: List<PostLikedUserResponse> = emptyList(),
)

data class PostDetailResponse(
    val post: PostCardResponse,
    val interaction: PostInteractionResponse,
)

data class PostCommentReplyResponse(
    val commentId: Long,
    val userId: Long,
    val username: String,
    val nickname: String?,
    val avatarUrl: String?,
    val ipRegion: String?,
    val replyToUserId: Long?,
    val replyToUsername: String?,
    val replyToNickname: String?,
    val contentText: String,
    val createdAt: String,
)

data class PostCommentResponse(
    val commentId: Long,
    val userId: Long,
    val username: String,
    val nickname: String?,
    val avatarUrl: String?,
    val ipRegion: String?,
    val contentText: String,
    val createdAt: String,
    val replies: List<PostCommentReplyResponse> = emptyList(),
)

data class PostRepostItemResponse(
    val repostId: Long,
    val userId: Long,
    val username: String,
    val nickname: String?,
    val avatarUrl: String?,
    val ipRegion: String?,
    val contentText: String,
    val createdAt: String,
)

data class LoginRequest(
    val username: String,
    val password: String,
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String?,
)

data class RefreshTokenRequest(
    val refreshToken: String,
)

data class CreatePostRequest(
    val contentText: String,
    val medias: List<PostMediaRequest>,
)

data class PostMediaRequest(
    val mediaType: String,
    val mediaUrl: String,
    val sortOrder: Int,
)

data class CreatePostCommentRequest(
    val contentText: String,
    val parentCommentId: Long?,
    val replyToUserId: Long?,
)

data class ErrorEnvelope(
    val code: Int?,
    val message: String?,
)
