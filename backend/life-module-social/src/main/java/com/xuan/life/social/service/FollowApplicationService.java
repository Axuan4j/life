package com.xuan.life.social.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.social.entity.UserFollow;
import com.xuan.life.social.mapper.UserFollowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowApplicationService {

    private final UserFollowMapper userFollowMapper;

    public FollowApplicationService(UserFollowMapper userFollowMapper) {
        this.userFollowMapper = userFollowMapper;
    }

    public void follow(Long followerUserId, Long followedUserId) {
        if (followerUserId.equals(followedUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能关注自己");
        }
        UserFollow existing = userFollowMapper.selectOne(new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowerUserId, followerUserId)
            .eq(UserFollow::getFollowedUserId, followedUserId));
        if (existing == null) {
            UserFollow follow = new UserFollow();
            follow.setFollowerUserId(followerUserId);
            follow.setFollowedUserId(followedUserId);
            userFollowMapper.insert(follow);
        }
    }

    public void unfollow(Long followerUserId, Long followedUserId) {
        userFollowMapper.delete(new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowerUserId, followerUserId)
            .eq(UserFollow::getFollowedUserId, followedUserId));
    }

    public List<Long> listFollowedUserIds(Long followerUserId) {
        return userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerUserId, followerUserId))
            .stream()
            .map(UserFollow::getFollowedUserId)
            .toList();
    }

    public boolean isFollowing(Long followerUserId, Long followedUserId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowerUserId, followerUserId)
            .eq(UserFollow::getFollowedUserId, followedUserId)) > 0;
    }

    public long countFollowing(Long followerUserId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowerUserId, followerUserId));
    }

    public long countFollowers(Long followedUserId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowedUserId, followedUserId));
    }

    public long countAllFollows() {
        return userFollowMapper.selectCount(null);
    }
}
