package com.xuan.life.android.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xuan.life.android.BuildConfig
import com.xuan.life.android.ui.model.FeedCardModel
import com.xuan.life.android.ui.model.PostCommentModel
import com.xuan.life.android.ui.model.PostDetailModel
import com.xuan.life.android.ui.model.PostLikedUserModel
import com.xuan.life.android.ui.model.PostReplyModel
import com.xuan.life.android.ui.model.PostRepostModel
import com.xuan.life.android.ui.model.ProfileUiModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class LifeRepository(context: Context) {

    private val gson: Gson = GsonBuilder().create()
    private val tokenStore = LifeTokenStore(context.applicationContext)
    private val api: LifeApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(
            OkHttpClient.Builder()
                // 统一在拦截器里注入 access token，避免每个接口手动传 header。
                .addInterceptor(AuthHeaderInterceptor(tokenStore))
                .build(),
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(LifeApiService::class.java)

    fun hasSession(): Boolean = tokenStore.hasSession()

    fun clearSession() {
        tokenStore.clear()
    }

    suspend fun login(username: String, password: String) {
        val tokens = execute { api.login(LoginRequest(username = username, password = password)) }
        tokenStore.save(tokens)
    }

    suspend fun register(username: String, password: String) {
        val tokens = execute {
            api.register(
                RegisterRequest(
                    username = username,
                    password = password,
                    nickname = username,
                ),
            )
        }
        tokenStore.save(tokens)
    }

    suspend fun logout() {
        try {
            executeNullable { api.logout() }
        } finally {
            tokenStore.clear()
        }
    }

    suspend fun refreshIfNeeded() {
        val refreshToken = tokenStore.getRefreshToken()
        if (refreshToken.isBlank()) {
            throw UnauthorizedException("登录已失效，请重新登录")
        }
        val tokens = execute { api.refresh(RefreshTokenRequest(refreshToken = refreshToken)) }
        tokenStore.save(tokens)
    }

    suspend fun getProfile(): ProfileUiModel {
        val profile = execute { api.me() }
        return profile.toUiModel()
    }

    suspend fun getHomeFeed(size: Int = 20): List<FeedCardModel> {
        val page = execute { api.homeFeed(size = size) }
        return page.items.map(::mapFeedItem)
    }

    suspend fun getPostDetail(
        postId: Long,
        authorBadgeHint: String = "",
        currentUserId: Long? = null,
    ): PostDetailModel {
        val detail = execute { api.postDetail(postId) }
        val comments = execute { api.postComments(postId) }
        val reposts = execute { api.postReposts(postId) }
        return mapPostDetail(detail, comments, reposts, authorBadgeHint, currentUserId)
    }

    suspend fun toggleLike(
        postId: Long,
        authorBadgeHint: String = "",
        currentUserId: Long? = null,
    ): PostDetailModel {
        execute { api.toggleLike(postId) }
        return getPostDetail(postId, authorBadgeHint, currentUserId)
    }

    suspend fun repost(
        postId: Long,
        authorBadgeHint: String = "",
        currentUserId: Long? = null,
    ): PostDetailModel {
        execute { api.repost(postId) }
        return getPostDetail(postId, authorBadgeHint, currentUserId)
    }

    suspend fun createComment(
        postId: Long,
        content: String,
        parentCommentId: Long?,
        replyToUserId: Long?,
        authorBadgeHint: String = "",
        currentUserId: Long? = null,
    ): PostDetailModel {
        executeNullable {
            api.createComment(
                postId = postId,
                request = CreatePostCommentRequest(
                    contentText = content,
                    parentCommentId = parentCommentId,
                    replyToUserId = replyToUserId,
                ),
            )
        }
        return getPostDetail(postId, authorBadgeHint, currentUserId)
    }

    suspend fun deleteComment(
        postId: Long,
        commentId: Long,
        authorBadgeHint: String = "",
        currentUserId: Long? = null,
    ): PostDetailModel {
        executeNullable { api.deleteComment(postId = postId, commentId = commentId) }
        return getPostDetail(postId, authorBadgeHint, currentUserId)
    }

    suspend fun createPost(content: String): FeedCardModel {
        val created = execute {
            api.createPost(
                CreatePostRequest(
                    contentText = content,
                    medias = emptyList(),
                ),
            )
        }
        return mapFeedCard(created, sourceType = "FOLLOWING")
    }

    private suspend fun <T> execute(block: suspend () -> ApiResponse<T>): T {
        try {
            val response = block()
            if (response.code != 0) {
                throw ApiException(response.message)
            }
            return response.data ?: throw ApiException("服务返回空数据")
        } catch (exception: HttpException) {
            if (exception.code() == 401) {
                tokenStore.clear()
                throw UnauthorizedException(parseErrorMessage(exception) ?: "登录已失效，请重新登录")
            }
            throw ApiException(parseErrorMessage(exception) ?: "请求失败，请稍后重试", exception)
        } catch (exception: IOException) {
            throw ApiException("网络连接失败，请检查服务是否启动", exception)
        }
    }

    private suspend fun executeNullable(block: suspend () -> ApiResponse<*>): Boolean {
        try {
            val response = block()
            if (response.code != 0) {
                throw ApiException(response.message)
            }
            return true
        } catch (exception: HttpException) {
            if (exception.code() == 401) {
                tokenStore.clear()
                throw UnauthorizedException(parseErrorMessage(exception) ?: "登录已失效，请重新登录")
            }
            throw ApiException(parseErrorMessage(exception) ?: "请求失败，请稍后重试", exception)
        } catch (exception: IOException) {
            throw ApiException("网络连接失败，请检查服务是否启动", exception)
        }
    }

    private fun parseErrorMessage(exception: HttpException): String? {
        val raw = exception.response()?.errorBody()?.string()?.trim().orEmpty()
        if (raw.isBlank()) {
            return null
        }
        return runCatching {
            gson.fromJson(raw, ErrorEnvelope::class.java)?.message
        }.getOrNull() ?: raw
    }

    private fun mapFeedItem(item: FeedItemResponse): FeedCardModel {
        return mapFeedCard(item.post, item.sourceType)
    }

    private fun mapFeedCard(post: PostCardResponse, sourceType: String): FeedCardModel {
        val isFollowing = sourceType == "FOLLOWING"
        val explicitTopic = extractExplicitTopic(post.contentText)
        return FeedCardModel(
            postId = post.postId,
            author = post.authorNickname?.takeIf { it.isNotBlank() } ?: post.authorUsername,
            authorHandle = "@${post.authorUsername}",
            authorBadge = if (isFollowing) "已关注" else "为你推荐",
            ipRegion = post.ipRegion ?: "未知",
            tag = if (isFollowing) "关注" else "推荐",
            topic = explicitTopic,
            content = buildDisplayContent(post.contentText, explicitTopic),
            location = "公开可见",
            publishedAt = formatRelativeTime(post.publishedAt),
            likeCount = post.likeCount.toInt(),
            commentCount = post.commentCount.toInt(),
            repostCount = post.repostCount.toInt(),
            // 媒体区域直接消费后端返回的 URL，避免继续依赖本地占位渐变模拟图片。
            mediaUrls = post.medias
                .sortedBy { it.sortOrder }
                .map { it.mediaUrl }
                .filter { it.isNotBlank() },
        )
    }

    private fun mapPostDetail(
        detail: PostDetailResponse,
        comments: List<PostCommentResponse>,
        reposts: List<PostRepostItemResponse>,
        authorBadgeHint: String,
        currentUserId: Long?,
    ): PostDetailModel {
        val explicitTopic = extractExplicitTopic(detail.post.contentText)
        return PostDetailModel(
            postId = detail.post.postId,
            author = detail.post.authorNickname?.takeIf { it.isNotBlank() } ?: detail.post.authorUsername,
            authorHandle = "@${detail.post.authorUsername}",
            authorBadge = authorBadgeHint,
            ipRegion = detail.post.ipRegion ?: "未知",
            publishedAt = formatRelativeTime(detail.post.publishedAt),
            content = detail.post.contentText,
            topic = explicitTopic,
            mediaUrls = detail.post.medias
                .sortedBy { it.sortOrder }
                .map { it.mediaUrl }
                .filter { it.isNotBlank() },
            likeCount = detail.interaction.likeCount.toInt(),
            commentCount = detail.interaction.commentCount.toInt(),
            repostCount = detail.interaction.repostCount.toInt(),
            likedByCurrentUser = detail.interaction.likedByCurrentUser,
            repostedByCurrentUser = detail.interaction.repostedByCurrentUser,
            comments = comments.map { mapComment(it, currentUserId) },
            likedUsers = detail.interaction.likedUsers.map(::mapLikedUser),
            reposts = reposts.map(::mapRepost),
        )
    }

    private fun mapComment(comment: PostCommentResponse, currentUserId: Long?): PostCommentModel {
        return PostCommentModel(
            commentId = comment.commentId,
            userId = comment.userId,
            author = comment.nickname?.takeIf { it.isNotBlank() } ?: comment.username,
            authorHandle = "@${comment.username}",
            ipRegion = comment.ipRegion ?: "未知",
            createdAt = formatRelativeTime(comment.createdAt),
            content = comment.contentText,
            isOwnedByCurrentUser = currentUserId == comment.userId,
            replies = comment.replies.map { mapReply(it, currentUserId) },
        )
    }

    private fun mapReply(reply: PostCommentReplyResponse, currentUserId: Long?): PostReplyModel {
        return PostReplyModel(
            replyId = reply.commentId,
            userId = reply.userId,
            author = reply.nickname?.takeIf { it.isNotBlank() } ?: reply.username,
            authorHandle = "@${reply.username}",
            replyToAuthor = reply.replyToNickname?.takeIf { it.isNotBlank() } ?: (reply.replyToUsername ?: ""),
            replyToUserId = reply.replyToUserId,
            ipRegion = reply.ipRegion ?: "未知",
            createdAt = formatRelativeTime(reply.createdAt),
            content = reply.contentText,
            isOwnedByCurrentUser = currentUserId == reply.userId,
        )
    }

    private fun mapLikedUser(user: PostLikedUserResponse): PostLikedUserModel {
        return PostLikedUserModel(
            userId = user.userId,
            author = user.nickname?.takeIf { it.isNotBlank() } ?: user.username,
            authorHandle = "@${user.username}",
        )
    }

    private fun mapRepost(repost: PostRepostItemResponse): PostRepostModel {
        return PostRepostModel(
            repostId = repost.repostId,
            author = repost.nickname?.takeIf { it.isNotBlank() } ?: repost.username,
            authorHandle = "@${repost.username}",
            ipRegion = repost.ipRegion ?: "未知",
            createdAt = formatRelativeTime(repost.createdAt),
            content = repost.contentText,
        )
    }

    private fun UserProfileResponse.toUiModel(): ProfileUiModel {
        return ProfileUiModel(
            userId = userId,
            username = username,
            nickname = nickname?.takeIf { it.isNotBlank() } ?: username,
            bio = bio ?: "在这里记录热爱，分享每一个值得被看见的瞬间。",
            lastLoginRegion = lastLoginRegion ?: "未知",
            postCount = postCount.toString(),
            followingCount = followingCount.toString(),
            followerCount = followerCount.toString(),
            likedCount = likedCount.toString(),
        )
    }

    private fun extractExplicitTopic(content: String): String {
        // 只把用户明确写在正文开头的 #话题# 当成话题，避免继续用正文截断伪造话题。
        val match = Regex("^\\s*#([^#\\r\\n]{1,20})#(?:\\s+|$)").find(content) ?: return ""
        return match.groupValues.getOrNull(1)?.trim().orEmpty()
    }

    private fun buildDisplayContent(content: String, topic: String): String {
        if (topic.isBlank()) {
            return content.trim()
        }
        return content.replaceFirst(Regex("^\\s*#[^#\\r\\n]{1,20}#\\s*"), "").trim()
    }

    private fun formatRelativeTime(value: String): String {
        return try {
            val target = LocalDateTime.parse(value)
            val duration = Duration.between(target, LocalDateTime.now())
            when {
                duration.toMinutes() < 1 -> "刚刚"
                duration.toMinutes() < 60 -> "${duration.toMinutes()} 分钟前"
                duration.toHours() < 24 -> "${duration.toHours()} 小时前"
                duration.toDays() < 7 -> "${duration.toDays()} 天前"
                else -> "${target.monthValue}-${target.dayOfMonth}"
            }
        } catch (_: DateTimeParseException) {
            value
        }
    }
}

private class AuthHeaderInterceptor(
    private val tokenStore: LifeTokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.getAccessToken()
        val request = if (token.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

class ApiException(
    override val message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class UnauthorizedException(
    override val message: String,
) : RuntimeException(message)
