package com.xuan.life.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.social.service.FollowApplicationService;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.entity.UserProfile;
import com.xuan.life.user.mapper.UserAccountMapper;
import com.xuan.life.user.mapper.UserProfileMapper;
import com.xuan.life.user.web.response.UserProfileResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserProfileApplicationService {

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final FollowApplicationService followApplicationService;
    private final JdbcTemplate jdbcTemplate;

    public UserProfileApplicationService(
        UserAccountMapper userAccountMapper,
        UserProfileMapper userProfileMapper,
        FollowApplicationService followApplicationService,
        JdbcTemplate jdbcTemplate
    ) {
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.followApplicationService = followApplicationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserProfileResponse getProfile(Long userId) {
        UserAccount account = userAccountMapper.selectById(userId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        UserProfile profile = userProfileMapper.selectOne(
            new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
        return new UserProfileResponse(
            account.getId(),
            account.getUsername(),
            profile != null ? profile.getNickname() : account.getUsername(),
            profile != null ? profile.getAvatarUrl() : "",
            profile != null ? profile.getBio() : "",
            account.getLastLoginRegion(),
            countPublishedPosts(userId),
            followApplicationService.countFollowing(userId),
            followApplicationService.countFollowers(userId),
            0L
        );
    }

    public Map<Long, UserProfileResponse> getProfiles(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, UserAccount> accountMap = userAccountMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .in(UserProfile::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        return accountMap.values().stream().map(account -> {
            UserProfile profile = profileMap.get(account.getId());
            return new UserProfileResponse(
                account.getId(),
                account.getUsername(),
                profile != null ? profile.getNickname() : account.getUsername(),
                profile != null ? profile.getAvatarUrl() : "",
                profile != null ? profile.getBio() : "",
                account.getLastLoginRegion(),
                countPublishedPosts(account.getId()),
                followApplicationService.countFollowing(account.getId()),
                followApplicationService.countFollowers(account.getId()),
                0L
            );
        }).collect(Collectors.toMap(UserProfileResponse::userId, Function.identity()));
    }

    public List<UserAccount> listAccounts(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userAccountMapper.selectBatchIds(userIds);
    }

    public List<UserAccount> listAccountsByUsernames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }
        return userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
            .in(UserAccount::getUsername, usernames));
    }

    private long countPublishedPosts(Long userId) {
        // 这里用 JdbcTemplate 直接做聚合统计，避免 user/content 模块互相依赖造成循环编译。
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM post WHERE author_id = ? AND status = 'PUBLISHED' AND visibility = 'PUBLIC'",
            Long.class,
            userId
        );
        return count != null ? count : 0L;
    }
}
