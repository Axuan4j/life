package com.xuan.life.admin.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.life.admin.model.AdminPermissionCodes;
import com.xuan.life.admin.web.response.AdminOverviewResponse;
import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.content.entity.Post;
import com.xuan.life.content.mapper.PostMapper;
import com.xuan.life.social.service.FollowApplicationService;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.mapper.UserAccountMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/overview")
public class AdminOverviewController {

    private final UserAccountMapper userAccountMapper;
    private final PostMapper postMapper;
    private final FollowApplicationService followApplicationService;

    public AdminOverviewController(
        UserAccountMapper userAccountMapper,
        PostMapper postMapper,
        FollowApplicationService followApplicationService
    ) {
        this.userAccountMapper = userAccountMapper;
        this.postMapper = postMapper;
        this.followApplicationService = followApplicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.OVERVIEW + "')")
    public ApiResponse<AdminOverviewResponse> overview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return ApiResponse.success(new AdminOverviewResponse(
            userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()),
            postMapper.selectCount(new LambdaQueryWrapper<Post>()),
            postMapper.selectCount(new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, todayStart)),
            followApplicationService.countAllFollows()
        ));
    }
}
