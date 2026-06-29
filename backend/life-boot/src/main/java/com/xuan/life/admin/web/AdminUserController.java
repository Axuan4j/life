package com.xuan.life.admin.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.life.admin.web.response.AdminUserListItemResponse;
import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.api.PageResponse;
import com.xuan.life.content.entity.Post;
import com.xuan.life.content.mapper.PostMapper;
import com.xuan.life.social.service.FollowApplicationService;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.entity.UserProfile;
import com.xuan.life.user.mapper.UserAccountMapper;
import com.xuan.life.user.mapper.UserProfileMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    public AdminUserController(
        UserAccountMapper userAccountMapper,
        UserProfileMapper userProfileMapper,
        PostMapper postMapper,
        FollowApplicationService followApplicationService
    ) {
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.postMapper = postMapper;
        this.followApplicationService = followApplicationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminUserListItemResponse>> listUsers(
        @RequestParam(name = "pageNo", defaultValue = "1") @Min(1) int pageNo,
        @RequestParam(name = "pageSize", defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        Page<UserAccount> page = userAccountMapper.selectPage(
            new Page<>(pageNo, pageSize),
            new LambdaQueryWrapper<UserAccount>().orderByDesc(UserAccount::getCreatedAt)
        );

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
}
