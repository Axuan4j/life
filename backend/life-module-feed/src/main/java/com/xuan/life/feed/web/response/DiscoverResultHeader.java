package com.xuan.life.feed.web.response;

public record DiscoverResultHeader(
    String resultType,
    String queryValue,
    String title,
    String subtitle,
    long totalCount
) {
}
