package com.xuan.life.admin.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.life.admin.model.AdminPermissionCodes;
import com.xuan.life.admin.service.AdminUserGovernanceApplicationService;
import com.xuan.life.admin.web.request.AdminUpdateUserGovernanceRequest;
import com.xuan.life.admin.web.response.AdminUserDetailResponse;
import com.xuan.life.admin.web.response.AdminUserListItemResponse;
import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.api.PageResponse;
import com.xuan.life.content.entity.Post;
import com.xuan.life.content.mapper.PostMapper;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.social.service.FollowApplicationService;
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
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final PostMapper postMapper;
    private final FollowApplicationService followApplicationService;
    private final AdminUserGovernanceApplicationService adminUserGovernanceApplicationService;

    public AdminUserController(
        UserAccountMapper userAccountMapper,
        UserProfileMapper userProfileMapper,
        PostMapper postMapper,
        FollowApplicationService followApplicationService,
        AdminUserGovernanceApplicationService adminUserGovernanceApplicationService
    ) {
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.postMapper = postMapper;
        this.followApplicationService = followApplicationService;
        this.adminUserGovernanceApplicationService = adminUserGovernanceApplicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.USER + "')")
    public ApiResponse<PageResponse<AdminUserListItemResponse>> listUsers(
        @RequestParam(name = "pageNo", defaultValue = "1") @Min(1) int pageNo,
        @RequestParam(name = "pageSize", defaultValue = "20") @Min(1) @Max(100) int pageSize,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "roleCode", required = false) String roleCode,
        @RequestParam(name = "status", required = false) Integer status
    ) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<UserAccount>()
            .orderByDesc(UserAccount::getCreatedAt);
        if (StringUtils.hasText(roleCode)) {
            wrapper.eq(UserAccount::getRoleCode, roleCode.trim().toUpperCase());
        }
        if (status != null) {
            wrapper.eq(UserAccount::getStatus, status);
        }
        applyKeywordFilter(wrapper, keyword);

        Page<UserAccount> page = userAccountMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<Long> userIds = page.getRecords().stream().map(UserAccount::getId).toList();
        Map<Long, UserProfile> profileMap = userIds.isEmpty() ? Map.of() : userProfileMapper.selectList(
            new LambdaQueryWrapper<UserProfile>().in(UserProfile::getUserId, userIds)
        ).stream().collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<AdminUserListItemResponse> items = page.getRecords().stream().map(account -> {
            UserProfile profile = profileMap.get(account.getId());
            long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getAuthorId, account.getId()));
            return new AdminUserListItemResponse(
                account.getId(),
                account.getUsername(),
                profile != null ? profile.getNickname() : account.getUsername(),
                account.getRoleCode(),
                account.getStatus(),
                account.getCreatedAt(),
                postCount,
                followApplicationService.countFollowing(account.getId()),
                followApplicationService.countFollowers(account.getId())
            );
        }).toList();

        return ApiResponse.success(new PageResponse<>(items, page.getTotal(), pageNo, pageSize));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.USER + "')")
    public ApiResponse<AdminUserDetailResponse> getUserDetail(@PathVariable("userId") Long userId) {
        return ApiResponse.success(adminUserGovernanceApplicationService.getUserDetail(userId));
    }

    @PatchMapping("/{userId}/governance")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.USER + "')")
    public ApiResponse<Void> updateGovernance(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("userId") Long userId,
        @Valid @RequestBody AdminUpdateUserGovernanceRequest request
    ) {
        adminUserGovernanceApplicationService.updateGovernance(userId, request, currentUser.getUserId());
        return ApiResponse.success(null);
    }

    private void applyKeywordFilter(LambdaQueryWrapper<UserAccount> wrapper, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String normalizedKeyword = keyword.trim();
        List<Long> profileUserIds = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .like(UserProfile::getNickname, normalizedKeyword))
            .stream()
            .map(UserProfile::getUserId)
            .toList();

        if (profileUserIds.isEmpty()) {
            wrapper.like(UserAccount::getUsername, normalizedKeyword);
            return;
        }
        wrapper.and(nested -> nested
            .like(UserAccount::getUsername, normalizedKeyword)
            .or()
            .in(UserAccount::getId, profileUserIds));
    }
}
