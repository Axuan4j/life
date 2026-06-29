package com.xuan.life.android.ui.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.xuan.life.android.data.remote.ApiException
import com.xuan.life.android.data.remote.LifeRepository
import com.xuan.life.android.data.remote.UnauthorizedException
import com.xuan.life.android.ui.component.BottomTabBar
import com.xuan.life.android.ui.model.FeedCardModel
import com.xuan.life.android.ui.model.PostDetailModel
import com.xuan.life.android.ui.model.ProfileUiModel
import com.xuan.life.android.ui.navigation.MainTabRoutes
import com.xuan.life.android.ui.navigation.mainTabs
import com.xuan.life.android.ui.screen.ComposeScreen
import com.xuan.life.android.ui.screen.HomeScreen
import com.xuan.life.android.ui.screen.LoginScreen
import com.xuan.life.android.ui.screen.PlaceholderScreen
import com.xuan.life.android.ui.screen.PostDetailScreen
import com.xuan.life.android.ui.screen.ProfileScreen
import kotlinx.coroutines.launch

@Composable
fun LifeApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { LifeRepository(context) }

    var currentTab by rememberSaveable { mutableStateOf(MainTabRoutes.HOME) }
    var isLoggedIn by rememberSaveable { mutableStateOf(repository.hasSession()) }
    var selectedPostId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedPostBadge by rememberSaveable { mutableStateOf("") }

    var authLoading by rememberSaveable { mutableStateOf(false) }
    var authError by rememberSaveable { mutableStateOf<String?>(null) }
    var feedLoading by rememberSaveable { mutableStateOf(false) }
    var feedError by rememberSaveable { mutableStateOf<String?>(null) }
    var profileLoading by rememberSaveable { mutableStateOf(false) }
    var profileError by rememberSaveable { mutableStateOf<String?>(null) }
    var detailLoading by rememberSaveable { mutableStateOf(false) }
    var detailError by rememberSaveable { mutableStateOf<String?>(null) }
    var composeSubmitting by rememberSaveable { mutableStateOf(false) }

    val feedItems = remember { mutableStateListOf<FeedCardModel>() }
    var profile by remember { mutableStateOf<ProfileUiModel?>(null) }
    var currentDetail by remember { mutableStateOf<PostDetailModel?>(null) }

    suspend fun handleUnauthorized(message: String) {
        repository.clearSession()
        isLoggedIn = false
        selectedPostId = null
        selectedPostBadge = ""
        authLoading = false
        authError = null
        feedLoading = false
        feedError = null
        profileLoading = false
        profileError = null
        detailLoading = false
        detailError = null
        composeSubmitting = false
        currentDetail = null
        profile = null
        feedItems.clear()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun updateFeedCounters(detail: PostDetailModel) {
        val feedIndex = feedItems.indexOfFirst { it.postId == detail.postId }
        if (feedIndex >= 0) {
            feedItems[feedIndex] = feedItems[feedIndex].copy(
                likeCount = detail.likeCount,
                commentCount = detail.commentCount,
                repostCount = detail.repostCount,
            )
        }
    }

    suspend fun loadProfile() {
        profileLoading = true
        profileError = null
        try {
            profile = repository.getProfile()
        } catch (exception: UnauthorizedException) {
            handleUnauthorized(exception.message ?: "登录已失效，请重新登录")
        } catch (exception: ApiException) {
            profileError = exception.message
        } finally {
            profileLoading = false
        }
    }

    suspend fun loadFeed() {
        feedLoading = true
        feedError = null
        try {
            val remoteFeed = repository.getHomeFeed()
            feedItems.clear()
            feedItems.addAll(remoteFeed)
        } catch (exception: UnauthorizedException) {
            handleUnauthorized(exception.message ?: "登录已失效，请重新登录")
        } catch (exception: ApiException) {
            feedError = exception.message
        } finally {
            feedLoading = false
        }
    }

    suspend fun loadCurrentDetail(postId: Long, authorBadge: String) {
        detailLoading = true
        detailError = null
        try {
            currentDetail = repository.getPostDetail(
                postId = postId,
                authorBadgeHint = authorBadge,
                currentUserId = profile?.userId,
            )
            currentDetail?.let(::updateFeedCounters)
        } catch (exception: UnauthorizedException) {
            handleUnauthorized(exception.message ?: "登录已失效，请重新登录")
        } catch (exception: ApiException) {
            detailError = exception.message
        } finally {
            detailLoading = false
        }
    }

    suspend fun runDetailRequest(
        successMessage: String? = null,
        request: suspend () -> PostDetailModel,
    ) {
        try {
            currentDetail = request()
            currentDetail?.let(::updateFeedCounters)
            if (!successMessage.isNullOrBlank()) {
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            }
        } catch (exception: UnauthorizedException) {
            handleUnauthorized(exception.message ?: "登录已失效，请重新登录")
        } catch (exception: ApiException) {
            Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun loadInitialData() {
        // 应用恢复登录态时先拉当前用户，再拉首页 feed，后续详情页才能正确判断“哪些评论是我自己发的”。
        loadProfile()
        if (isLoggedIn) {
            loadFeed()
        }
    }

    fun openPost(item: FeedCardModel) {
        selectedPostId = item.postId
        selectedPostBadge = item.authorBadge
        currentDetail = null
        scope.launch {
            loadCurrentDetail(item.postId, item.authorBadge)
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && profile == null && feedItems.isEmpty()) {
            loadInitialData()
        }
    }

    if (!isLoggedIn) {
        LoginScreen(
            isSubmitting = authLoading,
            errorMessage = authError,
            onLogin = { username, password ->
                scope.launch {
                    authLoading = true
                    authError = null
                    try {
                        repository.login(username, password)
                        isLoggedIn = true
                        currentTab = MainTabRoutes.HOME
                        profile = null
                        currentDetail = null
                        feedItems.clear()
                    } catch (exception: ApiException) {
                        authError = exception.message
                    } catch (exception: UnauthorizedException) {
                        authError = exception.message
                    } finally {
                        authLoading = false
                    }
                }
            },
            onRegister = { username, password ->
                scope.launch {
                    authLoading = true
                    authError = null
                    try {
                        repository.register(username, password)
                        isLoggedIn = true
                        currentTab = MainTabRoutes.HOME
                        profile = null
                        currentDetail = null
                        feedItems.clear()
                    } catch (exception: ApiException) {
                        authError = exception.message
                    } catch (exception: UnauthorizedException) {
                        authError = exception.message
                    } finally {
                        authLoading = false
                    }
                }
            },
        )
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FC),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = if (selectedPostId == null) {
                {
                    BottomTabBar(
                        tabs = mainTabs,
                        currentRoute = currentTab,
                        onTabSelected = { route -> currentTab = route },
                    )
                }
            } else {
                {}
            },
        ) { innerPadding ->
            if (selectedPostId != null) {
                when {
                    detailLoading && currentDetail == null -> {
                        FullScreenState(
                            message = "正在加载详情...",
                            loading = true,
                        )
                    }

                    !detailError.isNullOrBlank() && currentDetail == null -> {
                        FullScreenState(
                            message = detailError ?: "详情加载失败",
                            actionText = "重新加载",
                            onAction = {
                                scope.launch {
                                    loadCurrentDetail(selectedPostId!!, selectedPostBadge)
                                }
                            },
                        )
                    }

                    currentDetail != null -> {
                        PostDetailScreen(
                            innerPadding = innerPadding,
                            detail = currentDetail!!,
                            onBack = {
                                selectedPostId = null
                                currentDetail = null
                                detailError = null
                            },
                            onToggleLike = {
                                scope.launch {
                                    runDetailRequest {
                                        repository.toggleLike(
                                            postId = currentDetail!!.postId,
                                            authorBadgeHint = selectedPostBadge,
                                            currentUserId = profile?.userId,
                                        )
                                    }
                                }
                            },
                            onRepost = {
                                scope.launch {
                                    if (currentDetail?.repostedByCurrentUser == true) {
                                        Toast.makeText(context, "你已经转发过这条内容", Toast.LENGTH_SHORT).show()
                                    } else {
                                        runDetailRequest(successMessage = "转发成功") {
                                            repository.repost(
                                                postId = currentDetail!!.postId,
                                                authorBadgeHint = selectedPostBadge,
                                                currentUserId = profile?.userId,
                                            )
                                        }
                                    }
                                }
                            },
                            onSubmitComment = { content, parentCommentId, replyToUserId ->
                                scope.launch {
                                    runDetailRequest(successMessage = "评论成功") {
                                        repository.createComment(
                                            postId = currentDetail!!.postId,
                                            content = content,
                                            parentCommentId = parentCommentId,
                                            replyToUserId = replyToUserId,
                                            authorBadgeHint = selectedPostBadge,
                                            currentUserId = profile?.userId,
                                        )
                                    }
                                }
                            },
                            onDeleteComment = { commentId ->
                                scope.launch {
                                    runDetailRequest(successMessage = "删除成功") {
                                        repository.deleteComment(
                                            postId = currentDetail!!.postId,
                                            commentId = commentId,
                                            authorBadgeHint = selectedPostBadge,
                                            currentUserId = profile?.userId,
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            } else {
                when (currentTab) {
                    MainTabRoutes.HOME -> HomeScreen(
                        innerPadding = innerPadding,
                        feedItems = feedItems,
                        isLoading = feedLoading,
                        errorMessage = feedError,
                        onRetry = { scope.launch { loadFeed() } },
                        onOpenPost = ::openPost,
                    )

                    MainTabRoutes.DISCOVER -> PlaceholderScreen(
                        innerPadding = innerPadding,
                        title = "发现",
                        eyebrow = "Discover",
                        description = "发现优质作者、热门话题与更多值得关注的内容。",
                    )

                    MainTabRoutes.COMPOSE -> ComposeScreen(
                        innerPadding = innerPadding,
                        isSubmitting = composeSubmitting,
                        onCancel = { currentTab = MainTabRoutes.HOME },
                        onSubmit = { content ->
                            if (content.isBlank()) {
                                Toast.makeText(context, "内容不能为空", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    composeSubmitting = true
                                    try {
                                        repository.createPost(content)
                                        loadFeed()
                                        currentTab = MainTabRoutes.HOME
                                        Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
                                    } catch (exception: UnauthorizedException) {
                                        handleUnauthorized(exception.message ?: "登录已失效，请重新登录")
                                    } catch (exception: ApiException) {
                                        Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                                    } finally {
                                        composeSubmitting = false
                                    }
                                }
                            }
                        },
                    )

                    MainTabRoutes.MESSAGE -> PlaceholderScreen(
                        innerPadding = innerPadding,
                        title = "消息",
                        eyebrow = "Inbox",
                        description = "集中查看评论互动、点赞提醒、关注动态与系统通知。",
                    )

                    MainTabRoutes.PROFILE -> ProfileScreen(
                        innerPadding = innerPadding,
                        profile = profile,
                        isLoading = profileLoading,
                        errorMessage = profileError,
                        onRetry = { scope.launch { loadProfile() } },
                        onLogout = {
                            scope.launch {
                                try {
                                    repository.logout()
                                } catch (_: Exception) {
                                    repository.clearSession()
                                }
                                repository.clearSession()
                                isLoggedIn = false
                                currentTab = MainTabRoutes.HOME
                                selectedPostId = null
                                selectedPostBadge = ""
                                currentDetail = null
                                profile = null
                                feedItems.clear()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenState(
    message: String,
    loading: Boolean = false,
    actionText: String = "",
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color(0xFFFF5A3C))
            }
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF7D8795))
            if (actionText.isNotBlank() && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
    }
}
