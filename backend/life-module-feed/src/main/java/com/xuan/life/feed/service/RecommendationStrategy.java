package com.xuan.life.feed.service;

import com.xuan.life.content.web.response.PostCardResponse;
import com.xuan.life.feed.model.FeedCursor;

import java.util.Set;
import java.util.List;

public interface RecommendationStrategy {

    List<PostCardResponse> recommend(
        Long viewerUserId,
        Set<Long> excludedAuthorIds,
        Set<Long> excludedPostIds,
        FeedCursor cursor,
        int limit
    );
}
