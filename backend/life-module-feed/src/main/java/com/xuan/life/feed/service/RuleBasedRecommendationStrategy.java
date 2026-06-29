package com.xuan.life.feed.service;

import com.xuan.life.content.service.PostApplicationService;
import com.xuan.life.content.web.response.PostCardResponse;
import com.xuan.life.feed.model.FeedCursor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class RuleBasedRecommendationStrategy implements RecommendationStrategy {

    private final PostApplicationService postApplicationService;

    public RuleBasedRecommendationStrategy(PostApplicationService postApplicationService) {
        this.postApplicationService = postApplicationService;
    }

    @Override
    public List<PostCardResponse> recommend(
        Long viewerUserId,
        Set<Long> excludedAuthorIds,
        Set<Long> excludedPostIds,
        FeedCursor cursor,
        int limit
    ) {
        List<PostCardResponse> candidates = postApplicationService.findRecommendationCandidates(
            excludedAuthorIds,
            excludedPostIds,
            cursor != null ? cursor.publishedAt() : null,
            cursor != null ? cursor.postId() : null,
            Math.max(limit * 3, 20)
        );

        // 第一版推荐保持可解释：先看新鲜度，再叠加轻量互动热度，后面可以平滑替换为更复杂的策略。
        return candidates.stream()
            .sorted(Comparator.comparingDouble(this::score).reversed())
            .limit(limit)
            .toList();
    }

    private double score(PostCardResponse post) {
        long ageMinutes = Math.max(Duration.between(post.publishedAt(), LocalDateTime.now()).toMinutes(), 0);
        double freshness = Math.max(0, 1440 - ageMinutes) / 1440.0;
        double hotness = post.likeCount() * 1.0 + post.commentCount() * 2.0 + post.repostCount() * 3.0;
        return freshness * 0.7 + hotness * 0.3;
    }
}
