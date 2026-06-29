package com.xuan.life.feed.service;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.content.service.PostApplicationService;
import com.xuan.life.content.web.response.PostCardResponse;
import com.xuan.life.feed.config.DiscoverCatalogProperties;
import com.xuan.life.feed.model.DiscoverCursor;
import com.xuan.life.feed.model.DiscoverResultSort;
import com.xuan.life.feed.model.DiscoverResultType;
import com.xuan.life.feed.model.FeedSourceType;
import com.xuan.life.feed.web.response.DiscoverHomeResponse;
import com.xuan.life.feed.web.response.DiscoverHotKeywordItem;
import com.xuan.life.feed.web.response.DiscoverRecommendedAuthorItem;
import com.xuan.life.feed.web.response.DiscoverResultHeader;
import com.xuan.life.feed.web.response.DiscoverResultPageResponse;
import com.xuan.life.feed.web.response.DiscoverTopicSquareItem;
import com.xuan.life.feed.web.response.FeedItemResponse;
import com.xuan.life.social.service.FollowApplicationService;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.service.UserProfileApplicationService;
import com.xuan.life.user.web.response.UserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DiscoverApplicationService {

    private static final int COMPOSITE_SCAN_LIMIT = 200;

    private final DiscoverCatalogProperties discoverCatalogProperties;
    private final PostApplicationService postApplicationService;
    private final UserProfileApplicationService userProfileApplicationService;
    private final FollowApplicationService followApplicationService;

    public DiscoverApplicationService(
        DiscoverCatalogProperties discoverCatalogProperties,
        PostApplicationService postApplicationService,
        UserProfileApplicationService userProfileApplicationService,
        FollowApplicationService followApplicationService
    ) {
        this.discoverCatalogProperties = discoverCatalogProperties;
        this.postApplicationService = postApplicationService;
        this.userProfileApplicationService = userProfileApplicationService;
        this.followApplicationService = followApplicationService;
    }

    public DiscoverHomeResponse home(Long viewerUserId) {
        return new DiscoverHomeResponse(
            buildHotKeywords(),
            buildTopicSquare(),
            buildRecommendedAuthors(viewerUserId)
        );
    }

    public DiscoverResultPageResponse result(
        Long viewerUserId,
        String typeValue,
        String topicKey,
        String keyword,
        String sortValue,
        String cursorToken,
        int size
    ) {
        DiscoverResultType resultType = DiscoverResultType.from(typeValue);
        DiscoverResultSort resultSort = DiscoverResultSort.from(sortValue);
        DiscoverQueryDescriptor queryDescriptor = resolveQueryDescriptor(resultType, topicKey, keyword);
        long totalCount = postApplicationService.countVisiblePostsByKeywords(queryDescriptor.matchKeywords());
        DiscoverResultHeader header = new DiscoverResultHeader(
            resultType.name(),
            queryDescriptor.queryValue(),
            queryDescriptor.title(),
            queryDescriptor.buildSubtitle(totalCount),
            totalCount
        );
        return resultSort == DiscoverResultSort.LATEST
            ? latestResultPage(header, queryDescriptor.matchKeywords(), cursorToken, size)
            : compositeResultPage(header, queryDescriptor.matchKeywords(), cursorToken, size);
    }

    private List<DiscoverHotKeywordItem> buildHotKeywords() {
        List<DiscoverCatalogProperties.HotKeyword> configuredHotKeywords = discoverCatalogProperties.getHotKeywords();
        for (int index = 0; index < configuredHotKeywords.size(); index++) {
            DiscoverCatalogProperties.HotKeyword keyword = configuredHotKeywords.get(index);
            configuredHotKeywords.set(index, keyword);
        }
        return java.util.stream.IntStream.range(0, configuredHotKeywords.size())
            .mapToObj(index -> {
                DiscoverCatalogProperties.HotKeyword hotKeyword = configuredHotKeywords.get(index);
                long matchedCount = postApplicationService.countVisiblePostsByKeywords(resolveMatchKeywords(hotKeyword.getMatchKeywords(), hotKeyword.getKeyword()));
                return new DiscoverHotKeywordItem(
                    index + 1,
                    hotKeyword.getKeyword(),
                    defaultIfBlank(hotKeyword.getTitle(), hotKeyword.getKeyword()),
                    defaultIfBlank(hotKeyword.getTrendLabel(), "热"),
                    defaultIfBlank(hotKeyword.getHeatLabel(), formatCount(matchedCount) + " 热度")
                );
            })
            .toList();
    }

    private List<DiscoverTopicSquareItem> buildTopicSquare() {
        return discoverCatalogProperties.getTopics().stream()
            .map(topic -> new DiscoverTopicSquareItem(
                topic.getTopicKey(),
                topic.getTitle(),
                defaultIfBlank(topic.getSummary(), "发现更多你可能感兴趣的内容"),
                postApplicationService.countVisiblePostsByKeywords(resolveMatchKeywords(topic.getMatchKeywords(), topic.getTitle())),
                defaultIfBlank(topic.getCoverStyle(), "warm")
            ))
            .toList();
    }

    private List<DiscoverRecommendedAuthorItem> buildRecommendedAuthors(Long viewerUserId) {
        List<DiscoverCatalogProperties.RecommendedAuthor> configuredAuthors = discoverCatalogProperties.getRecommendedAuthors();
        List<String> configuredUsernames = configuredAuthors.stream()
            .map(DiscoverCatalogProperties.RecommendedAuthor::getUsername)
            .filter(StringUtils::hasText)
            .toList();
        Map<String, UserAccount> accountMap = userProfileApplicationService.listAccountsByUsernames(configuredUsernames).stream()
            .collect(Collectors.toMap(UserAccount::getUsername, Function.identity()));
        Map<Long, UserProfileResponse> profileMap = userProfileApplicationService.getProfiles(accountMap.values().stream()
            .map(UserAccount::getId)
            .toList());

        return configuredAuthors.stream()
            .map(configuredAuthor -> {
                UserAccount account = accountMap.get(configuredAuthor.getUsername());
                if (account == null || Objects.equals(account.getId(), viewerUserId)) {
                    return null;
                }
                UserProfileResponse profile = profileMap.get(account.getId());
                if (profile == null) {
                    return null;
                }
                return new DiscoverRecommendedAuthorItem(
                    profile.userId(),
                    profile.username(),
                    profile.nickname(),
                    profile.avatarUrl(),
                    profile.bio(),
                    profile.followerCount(),
                    followApplicationService.isFollowing(viewerUserId, profile.userId()),
                    defaultIfBlank(configuredAuthor.getReason(), "推荐关注")
                );
            })
            .filter(Objects::nonNull)
            .toList();
    }

    private DiscoverResultPageResponse latestResultPage(
        DiscoverResultHeader header,
        List<String> matchKeywords,
        String cursorToken,
        int size
    ) {
        DiscoverCursor cursor = DiscoverCursor.decode(cursorToken);
        List<PostCardResponse> posts = postApplicationService.findLatestVisiblePostsByKeywords(
            matchKeywords,
            cursor != null ? cursor.publishedAt() : null,
            cursor != null ? cursor.postId() : null,
            size + 1
        );
        boolean hasMore = posts.size() > size;
        List<PostCardResponse> currentPagePosts = hasMore ? posts.subList(0, size) : posts;
        String nextCursor = null;
        if (hasMore && !currentPagePosts.isEmpty()) {
            PostCardResponse lastPost = currentPagePosts.get(currentPagePosts.size() - 1);
            nextCursor = new DiscoverCursor(0L, lastPost.publishedAt(), lastPost.postId()).encode();
        }
        return new DiscoverResultPageResponse(header, toFeedItems(currentPagePosts), nextCursor, hasMore);
    }

    private DiscoverResultPageResponse compositeResultPage(
        DiscoverResultHeader header,
        List<String> matchKeywords,
        String cursorToken,
        int size
    ) {
        DiscoverCursor cursor = DiscoverCursor.decode(cursorToken);
        // V1 没有结构化 hashtag 和检索引擎，这里先在“最近一批命中的内容”上做规则打分，
        // 保证 discover 综合排序可解释、可联调，后续再替换成更精确的搜索/召回实现。
        List<ScoredPost> rankedPosts = postApplicationService.findLatestVisiblePostsByKeywords(
                matchKeywords,
                null,
                null,
                Math.max(COMPOSITE_SCAN_LIMIT, size * 8)
            )
            .stream()
            .map(post -> new ScoredPost(scoreComposite(post), post))
            .sorted(Comparator
                .comparingLong(ScoredPost::score).reversed()
                .thenComparing(scoredPost -> scoredPost.post().publishedAt(), Comparator.reverseOrder())
                .thenComparing(scoredPost -> scoredPost.post().postId(), Comparator.reverseOrder()))
            .toList();

        List<ScoredPost> filteredPosts = rankedPosts.stream()
            .filter(scoredPost -> comesAfterCompositeCursor(scoredPost, cursor))
            .limit(size + 1L)
            .toList();
        boolean hasMore = filteredPosts.size() > size;
        List<ScoredPost> currentPagePosts = hasMore ? filteredPosts.subList(0, size) : filteredPosts;
        String nextCursor = null;
        if (hasMore && !currentPagePosts.isEmpty()) {
            ScoredPost lastPost = currentPagePosts.get(currentPagePosts.size() - 1);
            nextCursor = new DiscoverCursor(lastPost.score(), lastPost.post().publishedAt(), lastPost.post().postId()).encode();
        }
        return new DiscoverResultPageResponse(
            header,
            toFeedItems(currentPagePosts.stream().map(ScoredPost::post).toList()),
            nextCursor,
            hasMore
        );
    }

    private boolean comesAfterCompositeCursor(ScoredPost scoredPost, DiscoverCursor cursor) {
        if (cursor == null) {
            return true;
        }
        if (scoredPost.score() < cursor.sortScore()) {
            return true;
        }
        if (scoredPost.score() > cursor.sortScore()) {
            return false;
        }
        if (scoredPost.post().publishedAt().isBefore(cursor.publishedAt())) {
            return true;
        }
        if (scoredPost.post().publishedAt().isAfter(cursor.publishedAt())) {
            return false;
        }
        return scoredPost.post().postId() < cursor.postId();
    }

    private long scoreComposite(PostCardResponse post) {
        long ageMinutes = Math.max(Duration.between(post.publishedAt(), LocalDateTime.now()).toMinutes(), 0L);
        long freshnessScore = Math.max(0L, 2_880L - ageMinutes);
        long interactionScore = post.likeCount() * 120L + post.commentCount() * 200L + post.repostCount() * 260L;
        // 综合榜先用“互动权重 + 时间衰减”组合，既能把热点顶上来，也避免旧内容长期霸榜。
        return interactionScore + freshnessScore;
    }

    private DiscoverQueryDescriptor resolveQueryDescriptor(
        DiscoverResultType resultType,
        String topicKey,
        String keyword
    ) {
        if (resultType == DiscoverResultType.TOPIC) {
            DiscoverCatalogProperties.Topic topic = discoverCatalogProperties.getTopics().stream()
                .filter(item -> item.getTopicKey() != null && item.getTopicKey().equals(topicKey))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "话题不存在"));
            return new DiscoverQueryDescriptor(
                topic.getTopicKey(),
                "#" + topic.getTitle(),
                defaultIfBlank(topic.getSummary(), "发现更多相关内容"),
                resolveMatchKeywords(topic.getMatchKeywords(), topic.getTitle())
            );
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        DiscoverCatalogProperties.HotKeyword configuredKeyword = discoverCatalogProperties.getHotKeywords().stream()
            .filter(item -> normalizedKeyword.equalsIgnoreCase(item.getKeyword()))
            .findFirst()
            .orElse(null);
        return new DiscoverQueryDescriptor(
            normalizedKeyword,
            normalizedKeyword,
            "正在为你整理相关讨论",
            configuredKeyword != null
                ? resolveMatchKeywords(configuredKeyword.getMatchKeywords(), configuredKeyword.getKeyword())
                : List.of(normalizedKeyword)
        );
    }

    private List<String> resolveMatchKeywords(List<String> configuredKeywords, String fallbackKeyword) {
        Set<String> keywords = new LinkedHashSet<>();
        if (configuredKeywords != null) {
            configuredKeywords.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .forEach(keywords::add);
        }
        if (StringUtils.hasText(fallbackKeyword)) {
            keywords.add(fallbackKeyword.trim());
        }
        return List.copyOf(keywords);
    }

    private List<FeedItemResponse> toFeedItems(List<PostCardResponse> posts) {
        return posts.stream()
            .map(post -> new FeedItemResponse(FeedSourceType.RECOMMENDED.name(), post))
            .toList();
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "keyword 不能为空");
        }
        return keyword.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String formatCount(long count) {
        if (count >= 10_000) {
            return String.format(Locale.ROOT, "%.1fw", count / 10_000.0d);
        }
        return Long.toString(count);
    }

    private record DiscoverQueryDescriptor(
        String queryValue,
        String title,
        String description,
        List<String> matchKeywords
    ) {
        private String buildSubtitle(long totalCount) {
            if (totalCount <= 0) {
                return description + " · 暂无相关内容";
            }
            return description + " · " + totalCount + " 条内容";
        }
    }

    private record ScoredPost(
        long score,
        PostCardResponse post
    ) {
    }
}
