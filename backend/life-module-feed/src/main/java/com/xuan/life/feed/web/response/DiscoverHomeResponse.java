package com.xuan.life.feed.web.response;

import java.util.List;

public record DiscoverHomeResponse(
    List<DiscoverHotKeywordItem> hotKeywords,
    List<DiscoverTopicSquareItem> topicSquare,
    List<DiscoverRecommendedAuthorItem> recommendedAuthors
) {
}
