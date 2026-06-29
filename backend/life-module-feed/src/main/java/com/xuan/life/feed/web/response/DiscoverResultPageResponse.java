package com.xuan.life.feed.web.response;

import java.util.List;

public record DiscoverResultPageResponse(
    DiscoverResultHeader header,
    List<FeedItemResponse> items,
    String nextCursor,
    boolean hasMore
) {
}
