package com.xuan.life.android.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LifeApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<TokenPairResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<TokenPairResponse>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): ApiResponse<TokenPairResponse>

    @POST("api/auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @GET("api/users/me")
    suspend fun me(): ApiResponse<UserProfileResponse>

    @GET("api/users/{userId}/profile")
    suspend fun userProfile(@Path("userId") userId: Long): ApiResponse<UserProfileResponse>

    @POST("api/follows/{targetUserId}")
    suspend fun follow(@Path("targetUserId") targetUserId: Long): ApiResponse<Unit>

    @DELETE("api/follows/{targetUserId}")
    suspend fun unfollow(@Path("targetUserId") targetUserId: Long): ApiResponse<Unit>

    @GET("api/follows/status/{targetUserId}")
    suspend fun followStatus(@Path("targetUserId") targetUserId: Long): ApiResponse<FollowStatusResponse>

    @GET("api/feed/home")
    suspend fun homeFeed(
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int = 20,
    ): ApiResponse<CursorPageResponse<FeedItemResponse>>

    @GET("api/discover/home")
    suspend fun discoverHome(): ApiResponse<DiscoverHomeResponse>

    @GET("api/discover/results")
    suspend fun discoverResults(
        @Query("type") type: String,
        @Query("topicKey") topicKey: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("sort") sort: String = "COMPOSITE",
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int = 10,
    ): ApiResponse<DiscoverResultPageResponse>

    @GET("api/posts/{postId}")
    suspend fun postDetail(@Path("postId") postId: Long): ApiResponse<PostDetailResponse>

    @GET("api/posts/{postId}/comments")
    suspend fun postComments(@Path("postId") postId: Long): ApiResponse<List<PostCommentResponse>>

    @GET("api/posts/{postId}/reposts")
    suspend fun postReposts(@Path("postId") postId: Long): ApiResponse<List<PostRepostItemResponse>>

    @POST("api/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Body request: CreatePostCommentRequest,
    ): ApiResponse<Unit>

    @DELETE("api/posts/{postId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long,
    ): ApiResponse<Unit>

    @POST("api/posts/{postId}/likes/toggle")
    suspend fun toggleLike(@Path("postId") postId: Long): ApiResponse<PostInteractionResponse>

    @POST("api/posts/{postId}/reposts")
    suspend fun repost(@Path("postId") postId: Long): ApiResponse<PostInteractionResponse>

    @POST("api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): ApiResponse<PostCardResponse>
}
