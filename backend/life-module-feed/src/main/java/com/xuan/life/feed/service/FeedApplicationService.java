package com.xuan.life.feed.service;

import com.xuan.life.common.api.CursorPageResponse;
import com.xuan.life.content.service.PostApplicationService;
import com.xuan.life.content.web.response.PostCardResponse;
import com.xuan.life.feed.entity.FeedExposure;
import com.xuan.life.feed.mapper.FeedExposureMapper;
import com.xuan.life.feed.model.FeedCursor;
import com.xuan.life.feed.model.FeedSourceType;
import com.xuan.life.feed.web.response.FeedItemResponse;
import com.xuan.life.social.service.FollowApplicationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class FeedApplicationService {

    private final FollowApplicationService followApplicationService;
    private final PostApplicationService postApplicationService;
    private final RecommendationStrategy recommendationStrategy;
    private final FeedExposureMapper feedExposureMapper;

    public FeedApplicationService(
        FollowApplicationService followApplicationService,
        PostApplicationService postApplicationService,
        RecommendationStrategy recommendationStrategy,
        FeedExposureMapper feedExposureMapper
    ) {
        this.followApplicationService = followApplicationService;
        this.postApplicationService = postApplicationService;
        this.recommendationStrategy = recommendationStrategy;
        this.feedExposureMapper = feedExposureMapper;
    }

    public CursorPageResponse<FeedItemResponse> homeFeed(Long viewerUserId, String cursorToken, int size) {
        FeedCursor cursor = FeedCursor.decode(cursorToken);
        List<Long> followedAuthorIds = followApplicationService.listFollowedUserIds(viewerUserId);
        List<PostCardResponse> followingPosts = postApplicationService.findRecentVisiblePostsByAuthors(
            followedAuthorIds,
            cursor != null ? cursor.publishedAt() : null,
            cursor != null ? cursor.postId() : null,
            size * 2 + 1
        );

        Set<Long> excludedPostIds = new LinkedHashSet<>();
        for (PostCardResponse followingPost : followingPosts) {
            excludedPostIds.add(followingPost.postId());
        }

        List<PostCardResponse> recommendedPosts = recommendationStrategy.recommend(
            viewerUserId,
            new HashSet<>(followedAuthorIds),
            excludedPostIds,
            cursor,
            size + 1
        );

        FeedMixResult mixResult = mixFollowingAndRecommended(followingPosts, recommendedPosts, size);
        List<FeedItemResponse> mixedItems = mixResult.items();
        String nextCursor = null;
        boolean hasMore = mixResult.hasMore();
        if (!mixedItems.isEmpty()) {
            FeedItemResponse lastItem = mixedItems.get(mixedItems.size() - 1);
            nextCursor = new FeedCursor(lastItem.post().publishedAt(), lastItem.post().postId()).encode();
        }

        recordExposure(viewerUserId, mixedItems, nextCursor);
        return new CursorPageResponse<>(mixedItems, nextCursor, hasMore);
    }

    private FeedMixResult mixFollowingAndRecommended(
        List<PostCardResponse> followingPosts,
        List<PostCardResponse> recommendedPosts,
        int size
    ) {
        List<FeedItemResponse> results = new ArrayList<>(size);
        int followingIndex = 0;
        int recommendedIndex = 0;

        // 这里显式控制“3 条关注 + 1 条推荐”的节奏，让首页始终以关注流为主，同时给冷启动和探索留入口。
        while (results.size() < size && (followingIndex < followingPosts.size() || recommendedIndex < recommendedPosts.size())) {
            for (int i = 0; i < 3 && results.size() < size && followingIndex < followingPosts.size(); i++) {
                results.add(new FeedItemResponse(FeedSourceType.FOLLOWING.name(), followingPosts.get(followingIndex++)));
            }
            if (results.size() < size && recommendedIndex < recommendedPosts.size()) {
                results.add(new FeedItemResponse(FeedSourceType.RECOMMENDED.name(), recommendedPosts.get(recommendedIndex++)));
            }
        }
        boolean hasMore = followingIndex < followingPosts.size() || recommendedIndex < recommendedPosts.size();
        return new FeedMixResult(results, hasMore);
    }

    private void recordExposure(Long viewerUserId, List<FeedItemResponse> items, String nextCursor) {
        for (FeedItemResponse item : items) {
            FeedExposure exposure = new FeedExposure();
            exposure.setViewerUserId(viewerUserId);
            exposure.setPostId(item.post().postId());
            exposure.setSourceType(item.sourceType());
            exposure.setCursorToken(nextCursor);
            exposure.setShownAt(LocalDateTime.now());
            feedExposureMapper.insert(exposure);
        }
    }

    private record FeedMixResult(
        List<FeedItemResponse> items,
        boolean hasMore
    ) {
    }
}
