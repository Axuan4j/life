package com.xuan.life.feed.web.response;

public record DiscoverHotKeywordItem(
    int rank,
    String keyword,
    String title,
    String trendLabel,
    String heatLabel
) {
}
