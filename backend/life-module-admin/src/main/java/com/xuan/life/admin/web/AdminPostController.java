package com.xuan.life.admin.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.life.admin.model.AdminPermissionCodes;
import com.xuan.life.admin.service.AdminContentGovernanceApplicationService;
import com.xuan.life.admin.web.request.AdminUpdatePostRequest;
import com.xuan.life.admin.web.response.AdminPostListItemResponse;
import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.api.PageResponse;
import com.xuan.life.content.entity.Post;
import com.xuan.life.content.entity.PostStat;
import com.xuan.life.content.mapper.PostMapper;
import com.xuan.life.content.mapper.PostStatMapper;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.entity.UserProfile;
import com.xuan.life.user.mapper.UserAccountMapper;
import com.xuan.life.user.mapper.UserProfileMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final PostMapper postMapper;
    private final PostStatMapper postStatMapper;
    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final AdminContentGovernanceApplicationService adminContentGovernanceApplicationService;

    public AdminPostController(
        PostMapper postMapper,
        PostStatMapper postStatMapper,
        UserAccountMapper userAccountMapper,
        UserProfileMapper userProfileMapper,
        AdminContentGovernanceApplicationService adminContentGovernanceApplicationService
    ) {
        this.postMapper = postMapper;
        this.postStatMapper = postStatMapper;
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.adminContentGovernanceApplicationService = adminContentGovernanceApplicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.CONTENT + "')")
    public ApiResponse<PageResponse<AdminPostListItemResponse>> listPosts(
        @RequestParam(name = "pageNo", defaultValue = "1") @Min(1) int pageNo,
        @RequestParam(name = "pageSize", defaultValue = "20") @Min(1) @Max(100) int pageSize,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "status", required = false) Integer status,
        @RequestParam(name = "reviewStatus", required = false) Integer reviewStatus,
        @RequestParam(name = "visibility", required = false) String visibility
    ) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
            .orderByDesc(Post::getCreatedAt);
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        if (reviewStatus != null) {
            wrapper.eq(Post::getReviewStatus, reviewStatus);
        }
        if (StringUtils.hasText(visibility)) {
            wrapper.eq(Post::getVisibility, visibility.trim().toUpperCase());
        }
        applyKeywordFilter(wrapper, keyword);

        Page<Post> page = postMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

        List<Long> authorIds = page.getRecords().stream().map(Post::getAuthorId).distinct().toList();
        List<Long> postIds = page.getRecords().stream().map(Post::getId).toList();
        Map<Long, UserAccount> accountMap = authorIds.isEmpty() ? Map.of() : userAccountMapper.selectBatchIds(authorIds)
            .stream().collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = authorIds.isEmpty() ? Map.of() : userProfileMapper.selectList(
            new LambdaQueryWrapper<UserProfile>().in(UserProfile::getUserId, authorIds)
        ).stream().collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        Map<Long, PostStat> statMap = postIds.isEmpty() ? Map.of() : postStatMapper.selectBatchIds(postIds)
            .stream().collect(Collectors.toMap(PostStat::getPostId, Function.identity()));

        List<AdminPostListItemResponse> items = page.getRecords().stream().map(post -> {
            UserAccount account = accountMap.get(post.getAuthorId());
            UserProfile profile = profileMap.get(post.getAuthorId());
            PostStat stat = statMap.get(post.getId());
            return new AdminPostListItemResponse(
                post.getId(),
                post.getAuthorId(),
                account != null ? account.getUsername() : "",
                profile != null ? profile.getNickname() : (account != null ? account.getUsername() : ""),
                post.getContentText(),
                post.getStatus(),
                post.getReviewStatus(),
                post.getReviewReason(),
                post.getVisibility(),
                post.getPublishedAt(),
                stat != null && stat.getLikeCount() != null ? stat.getLikeCount() : 0L,
                stat != null && stat.getCommentCount() != null ? stat.getCommentCount() : 0L,
                stat != null && stat.getRepostCount() != null ? stat.getRepostCount() : 0L
            );
        }).toList();

        return ApiResponse.success(new PageResponse<>(items, page.getTotal(), pageNo, pageSize));
    }

    @PatchMapping("/{postId}")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.CONTENT + "')")
    public ApiResponse<Void> updatePost(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId,
        @Valid @RequestBody AdminUpdatePostRequest request
    ) {
        adminContentGovernanceApplicationService.updatePost(postId, request, currentUser.getUserId());
        return ApiResponse.success(null);
    }

    private void applyKeywordFilter(LambdaQueryWrapper<Post> wrapper, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String normalizedKeyword = keyword.trim();
        List<Long> usernameMatchedIds = userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
                .like(UserAccount::getUsername, normalizedKeyword))
            .stream()
            .map(UserAccount::getId)
            .toList();
        List<Long> nicknameMatchedIds = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .like(UserProfile::getNickname, normalizedKeyword))
            .stream()
            .map(UserProfile::getUserId)
            .toList();
        List<Long> authorIds = java.util.stream.Stream.concat(usernameMatchedIds.stream(), nicknameMatchedIds.stream())
            .distinct()
            .toList();

        if (authorIds.isEmpty()) {
            wrapper.like(Post::getContentText, normalizedKeyword);
            return;
        }
        wrapper.and(nested -> nested
            .like(Post::getContentText, normalizedKeyword)
            .or()
            .in(Post::getAuthorId, authorIds));
    }
}
