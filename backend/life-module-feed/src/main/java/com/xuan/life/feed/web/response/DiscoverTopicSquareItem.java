package com.xuan.life.feed.web.response;

public record DiscoverTopicSquareItem(
    String topicKey,
    String title,
    String summary,
    long discussionCount,
    String coverStyle
) {
}
