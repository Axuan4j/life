package com.xuan.life.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.content.entity.Post;
import com.xuan.life.content.entity.PostComment;
import com.xuan.life.content.entity.PostLike;
import com.xuan.life.content.entity.PostMedia;
import com.xuan.life.content.entity.PostPollVote;
import com.xuan.life.content.entity.PostRepost;
import com.xuan.life.content.entity.PostStat;
import com.xuan.life.content.model.CommentStatusCodes;
import com.xuan.life.content.model.CreatePostModerationResultCodes;
import com.xuan.life.content.model.PostReviewStatusCodes;
import com.xuan.life.content.model.PostStatusCodes;
import com.xuan.life.content.model.SensitiveActionCodes;
import com.xuan.life.content.model.SensitiveHitActionResultCodes;
import com.xuan.life.content.model.SensitiveTargetTypes;
import com.xuan.life.content.mapper.PostCommentMapper;
import com.xuan.life.content.mapper.PostLikeMapper;
import com.xuan.life.content.mapper.PostMapper;
import com.xuan.life.content.mapper.PostMediaMapper;
import com.xuan.life.content.mapper.PostPollVoteMapper;
import com.xuan.life.content.mapper.PostRepostMapper;
import com.xuan.life.content.mapper.PostStatMapper;
import com.xuan.life.content.web.request.CreatePostCommentRequest;
import com.xuan.life.content.web.request.CreatePostRequest;
import com.xuan.life.content.web.request.PostMediaRequest;
import com.xuan.life.content.web.request.VotePostPollRequest;
import com.xuan.life.content.web.response.CreatePostResponse;
import com.xuan.life.content.web.response.PostCardResponse;
import com.xuan.life.content.web.response.PostCommentReplyResponse;
import com.xuan.life.content.web.response.PostCommentResponse;
import com.xuan.life.content.web.response.PostDetailResponse;
import com.xuan.life.content.web.response.PostInteractionResponse;
import com.xuan.life.content.web.response.PostLikedUserResponse;
import com.xuan.life.content.web.response.PostMediaResponse;
import com.xuan.life.content.web.response.PostPollOptionResponse;
import com.xuan.life.content.web.response.PostPollStateResponse;
import com.xuan.life.content.web.response.PostRepostItemResponse;
import com.xuan.life.infra.ip.IpRegionService;
import com.xuan.life.message.service.NotificationApplicationService;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.entity.UserGovernanceState;
import com.xuan.life.user.entity.UserProfile;
import com.xuan.life.user.mapper.UserAccountMapper;
import com.xuan.life.user.mapper.UserGovernanceStateMapper;
import com.xuan.life.user.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostApplicationService {

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostMediaMapper postMediaMapper;
    private final PostPollVoteMapper postPollVoteMapper;
    private final PostRepostMapper postRepostMapper;
    private final PostStatMapper postStatMapper;
    private final UserAccountMapper userAccountMapper;
    private final UserGovernanceStateMapper userGovernanceStateMapper;
    private final UserProfileMapper userProfileMapper;
    private final IpRegionService ipRegionService;
    private final NotificationApplicationService notificationApplicationService;
    private final SensitiveWordRuntimeService sensitiveWordRuntimeService;
    private final PostReviewAuditService postReviewAuditService;

    public PostApplicationService(
        PostMapper postMapper,
        PostCommentMapper postCommentMapper,
        PostLikeMapper postLikeMapper,
        PostMediaMapper postMediaMapper,
        PostPollVoteMapper postPollVoteMapper,
        PostRepostMapper postRepostMapper,
        PostStatMapper postStatMapper,
        UserAccountMapper userAccountMapper,
        UserGovernanceStateMapper userGovernanceStateMapper,
        UserProfileMapper userProfileMapper,
        IpRegionService ipRegionService,
        NotificationApplicationService notificationApplicationService,
        SensitiveWordRuntimeService sensitiveWordRuntimeService,
        PostReviewAuditService postReviewAuditService
    ) {
        this.postMapper = postMapper;
        this.postCommentMapper = postCommentMapper;
        this.postLikeMapper = postLikeMapper;
        this.postMediaMapper = postMediaMapper;
        this.postPollVoteMapper = postPollVoteMapper;
        this.postRepostMapper = postRepostMapper;
        this.postStatMapper = postStatMapper;
        this.userAccountMapper = userAccountMapper;
        this.userGovernanceStateMapper = userGovernanceStateMapper;
        this.userProfileMapper = userProfileMapper;
        this.ipRegionService = ipRegionService;
        this.notificationApplicationService = notificationApplicationService;
        this.sensitiveWordRuntimeService = sensitiveWordRuntimeService;
        this.postReviewAuditService = postReviewAuditService;
    }

    @Transactional
    public CreatePostResponse createPost(Long authorId, CreatePostRequest request, String clientIp) {
        validateMediaRequests(request.medias());
        ensureCanCreatePost(authorId);
        SensitiveWordRuntimeService.SensitiveScanResult scanResult = sensitiveWordRuntimeService.scanText(request.contentText());
        PostCreateDecision createDecision = resolvePostCreateDecision(scanResult);
        if (createDecision.reject()) {
            sensitiveWordRuntimeService.recordHitInNewTransaction(
                SensitiveTargetTypes.POST,
                null,
                authorId,
                scanResult,
                SensitiveHitActionResultCodes.POST_REJECTED,
                request.contentText()
            );
            throw new BusinessException(ErrorCode.BAD_REQUEST, createDecision.message());
        }

        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContentText(request.contentText());
        post.setVisibility(normalizeVisibility(request.visibility()));
        post.setStatus(PostStatusCodes.PUBLISHED);
        post.setReviewStatus(createDecision.reviewStatus());
        post.setReviewReason(createDecision.reviewReason());
        post.setClientIp(clientIp);
        post.setIpRegion(ipRegionService.resolveRegion(clientIp));
        post.setPublishedAt(LocalDateTime.now());
        postMapper.insert(post);

        if (!CollectionUtils.isEmpty(request.medias())) {
            // 这里保留显式排序字段，后续支持图片墙、视频封面时不会被客户端提交顺序绑死。
            for (PostMediaRequest mediaRequest : request.medias()) {
                PostMedia media = new PostMedia();
                media.setPostId(post.getId());
                media.setMediaType(mediaRequest.mediaType());
                media.setMediaUrl(mediaRequest.mediaUrl());
                media.setSortOrder(mediaRequest.sortOrder());
                postMediaMapper.insert(media);
            }
        }

        if (createDecision.reviewPending()) {
            postReviewAuditService.recordSystemReview(post.getId(), post.getReviewStatus(), post.getReviewReason());
        }

        PostStat stat = new PostStat();
        stat.setPostId(post.getId());
        stat.setLikeCount(0L);
        stat.setCommentCount(0L);
        stat.setRepostCount(0L);
        postStatMapper.insert(stat);
        UserAccount account = userAccountMapper.selectById(authorId);
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
            .eq(UserProfile::getUserId, authorId));
        List<PostMediaResponse> mediaResponses = CollectionUtils.isEmpty(request.medias()) ? List.of() : request.medias().stream()
            .sorted(Comparator.comparing(PostMediaRequest::sortOrder))
            .map(media -> new PostMediaResponse(media.mediaType(), media.mediaUrl(), media.sortOrder()))
            .toList();
        PostCardResponse postCard = mapToCard(post, mediaResponses, stat, account, profile);
        if (scanResult.hasMatches()) {
            sensitiveWordRuntimeService.recordHitAfterCommit(
                SensitiveTargetTypes.POST,
                post.getId(),
                authorId,
                scanResult,
                createDecision.hitActionResult(),
                request.contentText()
            );
        }
        return new CreatePostResponse(createDecision.moderationResult(), createDecision.message(), postCard);
    }

    public List<PostCardResponse> listUserPosts(Long viewerUserId, Long userId, int pageNo, int pageSize) {
        Page<Post> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
            .eq(Post::getStatus, PostStatusCodes.PUBLISHED)
            .eq(Post::getAuthorId, userId)
            .orderByDesc(Post::getPublishedAt)
            .orderByDesc(Post::getId);
        if (!userId.equals(viewerUserId)) {
            wrapper.eq(Post::getReviewStatus, PostReviewStatusCodes.APPROVED);
            wrapper.eq(Post::getVisibility, "PUBLIC");
        }
        List<Post> posts = postMapper.selectPage(page, wrapper).getRecords();
        return mapToCards(posts);
    }

    public PostDetailResponse getPostDetail(Long currentUserId, Long postId) {
        Post post = requireAccessiblePost(currentUserId, postId);
        PostCardResponse card = mapToCards(List.of(post)).stream().findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "帖子不存在"));
        return new PostDetailResponse(
            card,
            buildInteractionResponse(currentUserId, postId),
            buildPollState(currentUserId, post)
        );
    }

    public PostPollStateResponse getPostPollState(Long currentUserId, Long postId) {
        Post post = requireAccessiblePost(currentUserId, postId);
        return requirePollState(currentUserId, post);
    }

    public List<PostCommentResponse> listPostComments(Long currentUserId, Long postId) {
        requireAccessiblePost(currentUserId, postId);

        List<PostComment> comments = postCommentMapper.selectVisibleCommentsByPostId(postId);
        if (CollectionUtils.isEmpty(comments)) {
            return List.of();
        }

        List<Long> userIds = comments.stream()
            .flatMap(comment -> java.util.stream.Stream.of(comment.getUserId(), comment.getReplyToUserId()))
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, UserAccount> accountMap = userAccountMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .in(UserProfile::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        Map<Long, List<PostCommentReplyResponse>> replyMap = new HashMap<>();
        List<PostCommentResponse> topLevelComments = new ArrayList<>();

        // 评论区当前只做两级结构：楼主层 + 回复层。
        // 这里先把回复挂到 parentCommentId 上，再按创建时间稳定输出，前端拿到即可直接渲染。
        for (PostComment comment : comments) {
            if (comment.getParentCommentId() != null) {
                replyMap.computeIfAbsent(comment.getParentCommentId(), ignored -> new ArrayList<>())
                    .add(mapReply(comment, accountMap, profileMap));
                continue;
            }
            topLevelComments.add(new PostCommentResponse(
                comment.getId(),
                comment.getUserId(),
                getUsername(comment.getUserId(), accountMap),
                getNickname(comment.getUserId(), accountMap, profileMap),
                getAvatarUrl(comment.getUserId(), profileMap),
                comment.getIpRegion(),
                comment.getContentText(),
                comment.getCreatedAt(),
                List.of()
            ));
        }

        return topLevelComments.stream()
            .map(comment -> new PostCommentResponse(
                comment.commentId(),
                comment.userId(),
                comment.username(),
                comment.nickname(),
                comment.avatarUrl(),
                comment.ipRegion(),
                comment.contentText(),
                comment.createdAt(),
                replyMap.getOrDefault(comment.commentId(), List.of())
            ))
            .toList();
    }

    public List<PostRepostItemResponse> listPostReposts(Long currentUserId, Long postId) {
        requireAccessiblePost(currentUserId, postId);

        List<PostRepost> reposts = postRepostMapper.selectList(new LambdaQueryWrapper<PostRepost>()
            .eq(PostRepost::getPostId, postId)
            .orderByDesc(PostRepost::getCreatedAt)
            .last("LIMIT 30"));
        if (CollectionUtils.isEmpty(reposts)) {
            return List.of();
        }

        List<Long> userIds = reposts.stream()
            .map(PostRepost::getUserId)
            .distinct()
            .toList();
        List<Long> repostPostIds = reposts.stream()
            .map(PostRepost::getRepostPostId)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();

        Map<Long, UserAccount> accountMap = userIds.isEmpty() ? Map.of() : userAccountMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = userIds.isEmpty() ? Map.of() : userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .in(UserProfile::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        Map<Long, Post> repostPostMap = repostPostIds.isEmpty() ? Map.of() : postMapper.selectBatchIds(repostPostIds).stream()
            .collect(Collectors.toMap(Post::getId, Function.identity()));

        // 转发列表直接返回“谁在什么时候转发了什么内容”，前端就不需要再拼装二次查询。
        return reposts.stream()
            .map(repost -> {
                Post repostPost = repostPostMap.get(repost.getRepostPostId());
                return new PostRepostItemResponse(
                    repost.getId(),
                    repost.getUserId(),
                    getUsername(repost.getUserId(), accountMap),
                    getNickname(repost.getUserId(), accountMap, profileMap),
                    getAvatarUrl(repost.getUserId(), profileMap),
                    repostPost != null ? repostPost.getIpRegion() : "未知",
                    repostPost != null ? repostPost.getContentText() : "转发了这条内容",
                    repost.getCreatedAt()
                );
            })
            .toList();
    }

    @Transactional
    public PostInteractionResponse toggleLike(Long currentUserId, Long postId) {
        Post sourcePost = requireAccessiblePost(currentUserId, postId);
        PostLike existingLike = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
            .eq(PostLike::getPostId, postId)
            .eq(PostLike::getUserId, currentUserId)
            .last("LIMIT 1"));
        PostStat stat = requirePostStat(postId);
        long currentLikeCount = stat.getLikeCount() != null ? stat.getLikeCount() : 0L;

        if (existingLike != null) {
            postLikeMapper.deleteById(existingLike.getId());
            stat.setLikeCount(Math.max(0L, currentLikeCount - 1));
        } else {
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(currentUserId);
            postLikeMapper.insert(postLike);
            stat.setLikeCount(currentLikeCount + 1);
            notificationApplicationService.createLikeNotification(
                currentUserId,
                sourcePost.getAuthorId(),
                sourcePost.getId(),
                sourcePost.getContentText()
            );
        }
        postStatMapper.updateById(stat);
        return buildInteractionResponse(currentUserId, postId);
    }

    @Transactional
    public PostInteractionResponse repost(Long currentUserId, Long postId) {
        Post sourcePost = requireAccessiblePost(currentUserId, postId);
        if (!"PUBLIC".equals(sourcePost.getVisibility())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅公开内容支持转发");
        }
        PostRepost existingRepost = postRepostMapper.selectOne(new LambdaQueryWrapper<PostRepost>()
            .eq(PostRepost::getPostId, postId)
            .eq(PostRepost::getUserId, currentUserId)
            .last("LIMIT 1"));
        if (existingRepost == null) {
            UserAccount sourceAuthorAccount = userAccountMapper.selectById(sourcePost.getAuthorId());
            UserProfile sourceAuthorProfile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, sourcePost.getAuthorId())
                .last("LIMIT 1"));
            String repostContent = buildRepostContent(sourcePost, sourceAuthorAccount, sourceAuthorProfile);

            Post repostedPost = new Post();
            repostedPost.setAuthorId(currentUserId);
            repostedPost.setContentText(repostContent);
            repostedPost.setVisibility("PUBLIC");
            repostedPost.setStatus(PostStatusCodes.PUBLISHED);
            repostedPost.setReviewStatus(PostReviewStatusCodes.APPROVED);
            repostedPost.setReviewReason("");
            repostedPost.setClientIp(sourcePost.getClientIp());
            repostedPost.setIpRegion(sourcePost.getIpRegion());
            repostedPost.setPublishedAt(LocalDateTime.now());
            postMapper.insert(repostedPost);

            PostStat repostStat = new PostStat();
            repostStat.setPostId(repostedPost.getId());
            repostStat.setLikeCount(0L);
            repostStat.setCommentCount(0L);
            repostStat.setRepostCount(0L);
            postStatMapper.insert(repostStat);

            PostRepost repost = new PostRepost();
            repost.setPostId(postId);
            repost.setUserId(currentUserId);
            repost.setRepostPostId(repostedPost.getId());
            postRepostMapper.insert(repost);

            PostStat stat = requirePostStat(postId);
            long currentRepostCount = stat.getRepostCount() != null ? stat.getRepostCount() : 0L;
            stat.setRepostCount(currentRepostCount + 1);
            postStatMapper.updateById(stat);
            notificationApplicationService.createRepostNotification(
                currentUserId,
                sourcePost.getAuthorId(),
                sourcePost.getId(),
                repostContent
            );
        }
        return buildInteractionResponse(currentUserId, postId);
    }

    @Transactional
    public PostPollStateResponse votePoll(Long currentUserId, Long postId, VotePostPollRequest request) {
        Post post = requireAccessiblePost(currentUserId, postId);
        PostPollParser.ParsedPostPoll poll = requireParsedPoll(post);
        int optionIndex = request.optionIndex();
        if (optionIndex < 0 || optionIndex >= poll.options().size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "投票选项不存在");
        }

        PostPollVote existingVote = postPollVoteMapper.selectOne(new LambdaQueryWrapper<PostPollVote>()
            .eq(PostPollVote::getPostId, postId)
            .eq(PostPollVote::getUserId, currentUserId)
            .last("LIMIT 1"));
        if (existingVote == null) {
            PostPollVote vote = new PostPollVote();
            vote.setPostId(postId);
            vote.setUserId(currentUserId);
            vote.setOptionIndex(optionIndex);
            postPollVoteMapper.insert(vote);
        } else if (!Integer.valueOf(optionIndex).equals(existingVote.getOptionIndex())) {
            existingVote.setOptionIndex(optionIndex);
            postPollVoteMapper.updateById(existingVote);
        }

        return buildPollState(currentUserId, post, poll);
    }

    @Transactional
    public void createComment(Long currentUserId, Long postId, CreatePostCommentRequest request, String clientIp) {
        Post sourcePost = requireAccessiblePost(currentUserId, postId);
        ensureCanCreateComment(currentUserId);
        SensitiveWordRuntimeService.SensitiveScanResult scanResult = sensitiveWordRuntimeService.scanText(request.contentText());
        if (scanResult.hasMatches()) {
            sensitiveWordRuntimeService.recordHitInNewTransaction(
                SensitiveTargetTypes.COMMENT,
                null,
                currentUserId,
                scanResult,
                SensitiveHitActionResultCodes.COMMENT_REJECTED,
                request.contentText()
            );
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评论包含不合规表达，请调整后再发送");
        }

        Long parentCommentId = request.parentCommentId();
        Long replyToUserId = request.replyToUserId();
        if (parentCommentId != null) {
            PostComment parentComment = postCommentMapper.selectById(parentCommentId);
            if (parentComment == null || !postId.equals(parentComment.getPostId()) || !Integer.valueOf(CommentStatusCodes.VISIBLE).equals(parentComment.getStatus())) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "回复目标不存在");
            }
            // 评论区只允许两级结构：如果回复的是子评论，也统一挂在其顶级父评论下，避免无限楼中楼。
            if (parentComment.getParentCommentId() != null) {
                parentCommentId = parentComment.getParentCommentId();
            }
            if (replyToUserId == null) {
                replyToUserId = parentComment.getUserId();
            }
        } else {
            replyToUserId = null;
        }

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(currentUserId);
        comment.setParentCommentId(parentCommentId);
        comment.setReplyToUserId(replyToUserId);
        comment.setContentText(request.contentText().trim());
        comment.setStatus(CommentStatusCodes.VISIBLE);
        comment.setClientIp(clientIp);
        comment.setIpRegion(ipRegionService.resolveRegion(clientIp));
        postCommentMapper.insert(comment);

        PostStat stat = requirePostStat(postId);
        long currentCommentCount = stat.getCommentCount() != null ? stat.getCommentCount() : 0L;
        stat.setCommentCount(currentCommentCount + 1);
        postStatMapper.updateById(stat);
        notificationApplicationService.createCommentNotification(
            currentUserId,
            sourcePost.getAuthorId(),
            comment.getReplyToUserId(),
            sourcePost.getId(),
            comment.getId(),
            comment.getContentText()
        );
    }

    private void ensureCanCreatePost(Long userId) {
        UserGovernanceState governanceState = userGovernanceStateMapper.selectById(userId);
        if (governanceState == null) {
            return;
        }
        if (governanceState.getPostDisabled() != null && governanceState.getPostDisabled() == 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前账号已被限制发帖");
        }
        if (governanceState.getMuteUntil() != null && governanceState.getMuteUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前账号禁言中，暂时不能发帖");
        }
    }

    private void ensureCanCreateComment(Long userId) {
        UserGovernanceState governanceState = userGovernanceStateMapper.selectById(userId);
        if (governanceState == null) {
            return;
        }
        if (governanceState.getCommentDisabled() != null && governanceState.getCommentDisabled() == 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前账号已被限制评论");
        }
        if (governanceState.getMuteUntil() != null && governanceState.getMuteUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前账号禁言中，解除时间 " + governanceState.getMuteUntil());
        }
    }

    @Transactional
    public void deleteComment(Long currentUserId, Long postId, Long commentId) {
        requireAccessiblePost(currentUserId, postId);
        PostComment targetComment = postCommentMapper.selectById(commentId);
        if (targetComment == null || !postId.equals(targetComment.getPostId()) || !Integer.valueOf(CommentStatusCodes.VISIBLE).equals(targetComment.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        if (!currentUserId.equals(targetComment.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己的评论");
        }

        long affectedCount = postCommentMapper.selectCount(new LambdaQueryWrapper<PostComment>()
            .eq(PostComment::getPostId, postId)
            .and(wrapper -> wrapper
                .eq(PostComment::getId, commentId)
                .or()
                .eq(PostComment::getParentCommentId, commentId)));

        postCommentMapper.deleteById(commentId);

        PostStat stat = requirePostStat(postId);
        long currentCommentCount = stat.getCommentCount() != null ? stat.getCommentCount() : 0L;
        stat.setCommentCount(Math.max(0L, currentCommentCount - affectedCount));
        postStatMapper.updateById(stat);
    }

    public List<PostCardResponse> findRecentVisiblePostsByAuthors(
        List<Long> authorIds,
        LocalDateTime cursorPublishedAt,
        Long cursorPostId,
        int limit
    ) {
        if (CollectionUtils.isEmpty(authorIds)) {
            return List.of();
        }
        return mapToCards(postMapper.selectRecentVisiblePostsByAuthors(authorIds, cursorPublishedAt, cursorPostId, limit));
    }

    public List<PostCardResponse> findRecommendationCandidates(
        Set<Long> excludedAuthorIds,
        Set<Long> excludedPostIds,
        LocalDateTime cursorPublishedAt,
        Long cursorPostId,
        int limit
    ) {
        return mapToCards(postMapper.selectRecommendationCandidates(
            excludedAuthorIds,
            excludedPostIds,
            cursorPublishedAt,
            cursorPostId,
            limit
        ));
    }

    public List<PostCardResponse> findLatestVisiblePostsByKeywords(
        List<String> keywords,
        LocalDateTime cursorPublishedAt,
        Long cursorPostId,
        int limit
    ) {
        if (CollectionUtils.isEmpty(keywords)) {
            return List.of();
        }
        return mapToCards(postMapper.selectLatestVisiblePostsByKeywords(keywords, cursorPublishedAt, cursorPostId, limit));
    }

    public long countVisiblePostsByKeywords(List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return 0L;
        }
        Long count = postMapper.countVisiblePostsByKeywords(keywords);
        return count != null ? count : 0L;
    }

    private List<PostCardResponse> mapToCards(List<Post> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();
        Map<Long, List<PostMediaResponse>> mediaMap = postMediaMapper.selectList(
            new LambdaQueryWrapper<PostMedia>().in(PostMedia::getPostId, postIds)
        ).stream().collect(Collectors.groupingBy(
            PostMedia::getPostId,
            Collectors.collectingAndThen(Collectors.toList(), medias -> medias.stream()
                .sorted(Comparator.comparing(PostMedia::getSortOrder))
                .map(media -> new PostMediaResponse(media.getMediaType(), media.getMediaUrl(), media.getSortOrder()))
                .toList())
        ));
        Map<Long, PostStat> statMap = postStatMapper.selectBatchIds(postIds).stream()
            .collect(Collectors.toMap(PostStat::getPostId, Function.identity()));
        Map<Long, UserAccount> accountMap = userAccountMapper.selectBatchIds(authorIds).stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .in(UserProfile::getUserId, authorIds))
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<PostCardResponse> responses = new ArrayList<>(posts.size());
        for (Post post : posts) {
            responses.add(mapToCard(
                post,
                mediaMap.getOrDefault(post.getId(), List.of()),
                statMap.get(post.getId()),
                accountMap.get(post.getAuthorId()),
                profileMap.get(post.getAuthorId())
            ));
        }
        return responses;
    }

    private PostCardResponse mapToCard(
        Post post,
        List<PostMediaResponse> medias,
        PostStat stat,
        UserAccount account,
        UserProfile profile
    ) {
        long likeCount = stat != null && stat.getLikeCount() != null ? stat.getLikeCount() : 0L;
        long commentCount = stat != null && stat.getCommentCount() != null ? stat.getCommentCount() : 0L;
        long repostCount = stat != null && stat.getRepostCount() != null ? stat.getRepostCount() : 0L;
        return new PostCardResponse(
            post.getId(),
            post.getAuthorId(),
            account != null ? account.getUsername() : "",
            profile != null ? profile.getNickname() : (account != null ? account.getUsername() : ""),
            profile != null ? profile.getAvatarUrl() : "",
            post.getIpRegion(),
            post.getVisibility(),
            post.getContentText(),
            post.getPublishedAt(),
            likeCount,
            commentCount,
            repostCount,
            medias
        );
    }

    private void validateMediaRequests(List<PostMediaRequest> medias) {
        if (CollectionUtils.isEmpty(medias)) {
            return;
        }

        Set<Integer> sortOrders = medias.stream()
            .map(PostMediaRequest::sortOrder)
            .collect(Collectors.toSet());
        if (sortOrders.size() != medias.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "媒体顺序不能重复");
        }
    }

    private PostCommentReplyResponse mapReply(
        PostComment comment,
        Map<Long, UserAccount> accountMap,
        Map<Long, UserProfile> profileMap
    ) {
        return new PostCommentReplyResponse(
            comment.getId(),
            comment.getUserId(),
            getUsername(comment.getUserId(), accountMap),
            getNickname(comment.getUserId(), accountMap, profileMap),
            getAvatarUrl(comment.getUserId(), profileMap),
            comment.getIpRegion(),
            comment.getReplyToUserId(),
            comment.getReplyToUserId() == null ? "" : getUsername(comment.getReplyToUserId(), accountMap),
            comment.getReplyToUserId() == null ? "" : getNickname(comment.getReplyToUserId(), accountMap, profileMap),
            comment.getContentText(),
            comment.getCreatedAt()
        );
    }

    private String getUsername(Long userId, Map<Long, UserAccount> accountMap) {
        UserAccount account = accountMap.get(userId);
        return account != null ? account.getUsername() : "";
    }

    private String getNickname(Long userId, Map<Long, UserAccount> accountMap, Map<Long, UserProfile> profileMap) {
        UserProfile profile = profileMap.get(userId);
        if (profile != null) {
            return profile.getNickname();
        }
        return getUsername(userId, accountMap);
    }

    private String getAvatarUrl(Long userId, Map<Long, UserProfile> profileMap) {
        UserProfile profile = profileMap.get(userId);
        return profile != null ? profile.getAvatarUrl() : "";
    }

    private Post requireAccessiblePost(Long currentUserId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !Integer.valueOf(PostStatusCodes.PUBLISHED).equals(post.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "帖子不存在");
        }
        if (!Integer.valueOf(PostReviewStatusCodes.APPROVED).equals(post.getReviewStatus())) {
            if (currentUserId != null && currentUserId.equals(post.getAuthorId())) {
                return post;
            }
            throw new BusinessException(ErrorCode.NOT_FOUND, "帖子不存在");
        }
        if ("PUBLIC".equals(post.getVisibility())) {
            return post;
        }
        if ("PRIVATE".equals(post.getVisibility()) && currentUserId != null && currentUserId.equals(post.getAuthorId())) {
            return post;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND, "帖子不存在");
    }

    private String normalizeVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return "PUBLIC";
        }
        String normalized = visibility.trim().toUpperCase();
        if ("PUBLIC".equals(normalized) || "PRIVATE".equals(normalized)) {
            return normalized;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的可见范围");
    }

    private PostStat requirePostStat(Long postId) {
        PostStat stat = postStatMapper.selectById(postId);
        if (stat == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "帖子统计不存在");
        }
        return stat;
    }

    private PostPollStateResponse requirePollState(Long currentUserId, Post post) {
        PostPollStateResponse pollState = buildPollState(currentUserId, post);
        if (pollState == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "这条内容没有投票");
        }
        return pollState;
    }

    private PostPollStateResponse buildPollState(Long currentUserId, Post post) {
        PostPollParser.ParsedPostPoll poll = PostPollParser.parse(post.getContentText());
        if (poll == null) {
            return null;
        }
        return buildPollState(currentUserId, post, poll);
    }

    private PostPollStateResponse buildPollState(Long currentUserId, Post post, PostPollParser.ParsedPostPoll poll) {
        Map<Integer, Long> voteCountMap = postPollVoteMapper.countVotesByPostId(post.getId()).stream()
            .collect(Collectors.toMap(
                PostPollVoteMapper.PostPollOptionCountRow::getOptionIndex,
                row -> row.getVoteCount() != null ? row.getVoteCount() : 0L
            ));
        long totalVotes = voteCountMap.values().stream().mapToLong(Long::longValue).sum();

        PostPollVote currentVote = currentUserId == null ? null : postPollVoteMapper.selectOne(new LambdaQueryWrapper<PostPollVote>()
            .eq(PostPollVote::getPostId, post.getId())
            .eq(PostPollVote::getUserId, currentUserId)
            .last("LIMIT 1"));
        Integer currentOptionIndex = currentVote != null ? currentVote.getOptionIndex() : null;

        List<PostPollOptionResponse> options = new ArrayList<>(poll.options().size());
        for (int index = 0; index < poll.options().size(); index++) {
            long voteCount = voteCountMap.getOrDefault(index, 0L);
            int votePercent = totalVotes == 0 ? 0 : (int) Math.round(voteCount * 100.0 / totalVotes);
            options.add(new PostPollOptionResponse(
                index,
                poll.options().get(index),
                voteCount,
                votePercent,
                currentOptionIndex != null && currentOptionIndex == index
            ));
        }

        return new PostPollStateResponse(
            poll.question(),
            options,
            totalVotes,
            currentOptionIndex != null,
            currentOptionIndex
        );
    }

    private PostPollParser.ParsedPostPoll requireParsedPoll(Post post) {
        PostPollParser.ParsedPostPoll poll = PostPollParser.parse(post.getContentText());
        if (poll == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "这条内容没有投票");
        }
        return poll;
    }

    private PostInteractionResponse buildInteractionResponse(Long currentUserId, Long postId) {
        PostStat stat = requirePostStat(postId);
        boolean likedByCurrentUser = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
            .eq(PostLike::getPostId, postId)
            .eq(PostLike::getUserId, currentUserId)) > 0;
        boolean repostedByCurrentUser = postRepostMapper.selectCount(new LambdaQueryWrapper<PostRepost>()
            .eq(PostRepost::getPostId, postId)
            .eq(PostRepost::getUserId, currentUserId)) > 0;

        List<PostLike> recentLikers = postLikeMapper.selectRecentLikers(postId, 9);
        List<Long> likerUserIds = recentLikers.stream().map(PostLike::getUserId).distinct().toList();
        Map<Long, UserAccount> accountMap = likerUserIds.isEmpty() ? Map.of() : userAccountMapper.selectBatchIds(likerUserIds).stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = likerUserIds.isEmpty() ? Map.of() : userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .in(UserProfile::getUserId, likerUserIds))
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<PostLikedUserResponse> likedUsers = recentLikers.stream()
            .map(like -> new PostLikedUserResponse(
                like.getUserId(),
                getUsername(like.getUserId(), accountMap),
                getNickname(like.getUserId(), accountMap, profileMap),
                getAvatarUrl(like.getUserId(), profileMap)
            ))
            .toList();

        return new PostInteractionResponse(
            stat.getLikeCount() != null ? stat.getLikeCount() : 0L,
            stat.getCommentCount() != null ? stat.getCommentCount() : 0L,
            stat.getRepostCount() != null ? stat.getRepostCount() : 0L,
            likedByCurrentUser,
            repostedByCurrentUser,
            likedUsers
        );
    }

    private String buildRepostContent(Post sourcePost, UserAccount sourceAuthorAccount, UserProfile sourceAuthorProfile) {
        String sourceAuthorName = sourceAuthorProfile != null && sourceAuthorProfile.getNickname() != null && !sourceAuthorProfile.getNickname().isBlank()
            ? sourceAuthorProfile.getNickname()
            : (sourceAuthorAccount != null ? sourceAuthorAccount.getUsername() : "原作者");
        return "转发 @" + sourceAuthorName + "：\n" + sourcePost.getContentText();
    }

    private PostCreateDecision resolvePostCreateDecision(SensitiveWordRuntimeService.SensitiveScanResult scanResult) {
        if (scanResult == null || !scanResult.hasMatches()) {
            return new PostCreateDecision(
                CreatePostModerationResultCodes.PUBLISHED,
                "发布成功",
                PostReviewStatusCodes.APPROVED,
                "",
                null,
                false
            );
        }
        if (Objects.equals(scanResult.highestActionType(), SensitiveActionCodes.REJECT)) {
            return new PostCreateDecision(
                CreatePostModerationResultCodes.PUBLISHED,
                "内容包含不合规表达，请调整后再发布",
                PostReviewStatusCodes.REJECTED,
                "",
                SensitiveHitActionResultCodes.POST_REJECTED,
                true
            );
        }
        if (Objects.equals(scanResult.highestActionType(), SensitiveActionCodes.REVIEW)) {
            return new PostCreateDecision(
                CreatePostModerationResultCodes.REVIEW_PENDING,
                "内容已提交，审核通过后展示",
                PostReviewStatusCodes.PENDING,
                "敏感词命中，待人工审核",
                SensitiveHitActionResultCodes.REVIEW_PENDING,
                false
            );
        }
        return new PostCreateDecision(
            CreatePostModerationResultCodes.WARN_PASSED,
            "内容已发布，请注意社区规范",
            PostReviewStatusCodes.APPROVED,
            "",
            SensitiveHitActionResultCodes.WARN_PASSED,
            false
        );
    }

    private record PostCreateDecision(
        String moderationResult,
        String message,
        Integer reviewStatus,
        String reviewReason,
        String hitActionResult,
        boolean reject
    ) {
        private boolean reviewPending() {
            return Objects.equals(reviewStatus, PostReviewStatusCodes.PENDING);
        }
    }
}
