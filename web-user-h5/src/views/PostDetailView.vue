<template>
  <div class="page">
    <van-nav-bar title="正文" left-arrow fixed placeholder @click-left="router.back()" />

    <section v-if="post" class="post-main">
      <div class="author-row">
        <div
          class="avatar"
          :style="
            post.authorAvatarUrl
              ? { backgroundImage: `url(${post.authorAvatarUrl})` }
              : { background: getFallbackAvatar(post.authorName) }
          "
        ></div>
        <div class="author-main">
          <div class="author-line">
            <strong>{{ post.authorName }}</strong>
            <span class="author-tag">@{{ post.authorId }}</span>
          </div>
          <div class="author-meta">
            <span>{{ post.publishedAt }}</span>
            <span class="meta-dot">·</span>
            <span class="inline-meta">
              <van-icon name="location-o" class="mini-icon" />
              <span>{{ post.ipRegion }}</span>
            </span>
          </div>
        </div>
      </div>

      <p v-if="post.topic" class="topic"># {{ post.topic }}</p>
      <p v-if="post.displayContentText" class="content">{{ post.displayContentText }}</p>

      <div v-if="post.poll" class="poll-card">
        <div class="poll-head">
          <span class="poll-badge">投票</span>
          <strong>{{ post.poll.question }}</strong>
        </div>
        <div class="poll-options">
          <div v-for="(option, index) in post.poll.options" :key="`${post.postId}-poll-${index}`" class="poll-option">
            <span>{{ index + 1 }}</span>
            <strong>{{ option }}</strong>
          </div>
        </div>
      </div>

      <div v-if="post.images.length > 0" class="image-grid">
        <div
          v-for="image in post.images"
          :key="image.id"
          class="image-card"
          :style="image.url ? { backgroundImage: `url(${image.url})` } : { background: image.palette }"
        ></div>
      </div>
    </section>

    <section class="tab-shell">
      <van-tabs v-model:active="activeTabIndex" animated shrink class="detail-tabs">
        <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.label">
          <div v-if="tab.key === 'reposts'" class="tab-panel">
            <article v-for="item in reposts" :key="item.repostId" class="row-item">
              <div
                class="row-avatar"
                :style="
                  item.avatarUrl
                    ? { backgroundImage: `url(${item.avatarUrl})` }
                    : { background: getFallbackAvatar(item.displayName) }
                "
              ></div>
              <div class="row-main">
                <div class="row-line">
                  <strong>{{ item.displayName }}</strong>
                  <span>@{{ item.username }}</span>
                </div>
                <div class="row-meta">
                  <span>{{ item.createdAt }}</span>
                  <span class="meta-dot">·</span>
                  <span class="inline-meta">
                    <van-icon name="location-o" class="mini-icon" />
                    <span>{{ item.ipRegion }}</span>
                  </span>
                </div>
                <div class="quote-box">{{ item.contentText }}</div>
              </div>
            </article>
            <van-empty v-if="reposts.length === 0" description="暂时还没有转发" />
          </div>

          <div v-else-if="tab.key === 'comments'" class="tab-panel">
            <article v-for="comment in comments" :key="comment.commentId" class="row-item comment-item">
              <div
                class="row-avatar"
                :style="
                  comment.avatarUrl
                    ? { backgroundImage: `url(${comment.avatarUrl})` }
                    : { background: getFallbackAvatar(comment.displayName) }
                "
              ></div>
              <div class="row-main">
                <div class="row-line">
                  <strong>{{ comment.displayName }}</strong>
                  <span>@{{ comment.username }}</span>
                </div>
                <p class="comment-text">{{ comment.contentText }}</p>
                <div class="row-meta">
                  <span>{{ comment.createdAt }}</span>
                  <span class="meta-dot">·</span>
                  <span class="inline-meta">
                    <van-icon name="location-o" class="mini-icon" />
                    <span>{{ comment.ipRegion }}</span>
                  </span>
                  <button
                    type="button"
                    class="meta-action"
                    @click="prepareReply(comment.commentId, comment.userId, comment.displayName)"
                  >
                    回复
                  </button>
                  <button
                    v-if="canDeleteComment(comment.userId)"
                    class="meta-action danger"
                    @click="deleteComment(comment.commentId)"
                  >
                    删除
                  </button>
                </div>

                <div v-if="comment.replies.length > 0" class="reply-list">
                  <article v-for="reply in comment.replies" :key="reply.commentId" class="reply-item">
                    <div
                      class="reply-avatar"
                      :style="
                        reply.avatarUrl
                          ? { backgroundImage: `url(${reply.avatarUrl})` }
                          : { background: getFallbackAvatar(reply.displayName) }
                      "
                    ></div>
                    <div class="reply-main">
                      <div class="row-line compact">
                        <strong>{{ reply.displayName }}</strong>
                        <span>@{{ reply.username }}</span>
                      </div>
                      <p class="reply-text">
                        <template v-if="reply.replyToDisplayName">回复 {{ reply.replyToDisplayName }}：</template>
                        {{ reply.contentText }}
                      </p>
                      <div class="row-meta compact">
                        <span>{{ reply.createdAt }}</span>
                        <span class="meta-dot">·</span>
                        <span class="inline-meta">
                          <van-icon name="location-o" class="mini-icon" />
                          <span>{{ reply.ipRegion }}</span>
                        </span>
                        <button
                          type="button"
                          class="meta-action"
                          @click="prepareReply(comment.commentId, reply.userId, reply.displayName)"
                        >
                          回复
                        </button>
                        <button
                          v-if="canDeleteComment(reply.userId)"
                          class="meta-action danger"
                          @click="deleteComment(reply.commentId)"
                        >
                          删除
                        </button>
                      </div>
                    </div>
                  </article>
                </div>
              </div>
            </article>
            <van-empty v-if="comments.length === 0" description="暂时还没有评论" />
          </div>

          <div v-else class="tab-panel">
            <article v-for="user in interaction.likedUsers" :key="user.userId" class="row-item">
              <div
                class="row-avatar"
                :style="
                  user.avatarUrl
                    ? { backgroundImage: `url(${user.avatarUrl})` }
                    : { background: getFallbackAvatar(user.displayName) }
                "
              ></div>
              <div class="row-main">
                <div class="row-line">
                  <strong>{{ user.displayName }}</strong>
                  <span>@{{ user.username }}</span>
                </div>
                <div class="row-subtle">
                  <van-icon name="like" class="liked-icon" />
                  <span>点赞了这条内容</span>
                </div>
              </div>
            </article>
            <van-empty v-if="interaction.likedUsers.length === 0" description="暂时还没有点赞" />
          </div>
        </van-tab>
      </van-tabs>
    </section>

    <van-popup
      v-model:show="composerVisible"
      position="bottom"
      round
      teleport="body"
      class="composer-popup"
      @opened="focusComposerInput"
      @closed="resetReplyWhenEmpty"
    >
      <div class="composer-sheet">
        <div class="composer-head">
          <strong>{{ replyTarget ? `回复 ${replyTarget.displayName}` : '写评论' }}</strong>
          <van-button plain size="small" class="meta-action" @click="composerVisible = false">取消</van-button>
        </div>
        <van-field
          ref="composerFieldRef"
          v-model="commentText"
          type="textarea"
          rows="4"
          autosize
          maxlength="500"
          show-word-limit
          class="composer-field"
          :placeholder="replyTarget ? `回复 ${replyTarget.displayName}...` : '说点什么吧...'"
        />
        <div v-if="emojiPanelVisible" class="emoji-panel">
          <div class="emoji-section">
            <div class="emoji-section-title">常用表情</div>
            <van-swipe :show-indicators="emojiPages.length > 1" class="emoji-swipe" :loop="false">
              <van-swipe-item v-for="(page, pageIndex) in emojiPages" :key="`emoji-page-${pageIndex}`">
                <div class="emoji-grid">
                  <van-button
                    v-for="emoji in page"
                    :key="emoji"
                    plain
                    hairline
                    size="small"
                    class="emoji-btn"
                    @click="appendEmoji(emoji)"
                  >
                    {{ emoji }}
                  </van-button>
                </div>
              </van-swipe-item>
            </van-swipe>
          </div>
        </div>
        <div class="composer-foot">
          <span></span>
          <div class="composer-actions">
            <van-button plain hairline size="small" round icon="smile-o" @click="toggleEmojiPanel">Emoji</van-button>
            <van-button plain hairline size="small" round icon="delete-o" @click="removeLastCharacter">删除</van-button>
            <van-button
              type="primary"
              size="small"
              round
              :loading="submittingComment"
              @click="submitComment"
            >
              发送
            </van-button>
          </div>
        </div>
      </div>
    </van-popup>

    <van-action-bar placeholder safe-area-inset-bottom class="detail-action-bar">
      <van-action-bar-button icon="chat-o" text="评论" @click="openCommentComposer" />
      <van-action-bar-button
        :icon="interaction.likedByCurrentUser ? 'like' : 'like-o'"
        text="点赞"
        :class="{ 'is-active': interaction.likedByCurrentUser }"
        @click="toggleLike"
      />
      <van-action-bar-button
        icon="replay"
        text="转发"
        :class="{ 'is-active': interaction.repostedByCurrentUser }"
        @click="repostPost"
      />
    </van-action-bar>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, useTemplateRef, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { useUserAuthStore } from '../stores/auth';
import { postApi } from '../services/api';
import {
  getFallbackAvatar,
  mapComment,
  mapPostDetail,
  mapRepostItem,
  type CommentViewModel,
  type FeedPostViewModel,
  type PostInteractionViewModel,
  type RepostItemViewModel,
} from '../services/view-models';
import type { EntityId } from '../services/api';

type DetailTabKey = 'reposts' | 'comments' | 'likes';

const route = useRoute();
const router = useRouter();
const authStore = useUserAuthStore();
const activeTab = ref<DetailTabKey>('comments');
const composerVisible = ref(false);
const emojiPanelVisible = ref(false);
const post = ref<FeedPostViewModel | null>(null);
const comments = ref<CommentViewModel[]>([]);
const reposts = ref<RepostItemViewModel[]>([]);
const interaction = ref<PostInteractionViewModel>({
  likeCount: 0,
  commentCount: 0,
  repostCount: 0,
  likedByCurrentUser: false,
  repostedByCurrentUser: false,
  likedUsers: [],
});
const commentText = ref('');
const submittingComment = ref(false);
const replyTarget = ref<{ parentCommentId: EntityId; replyToUserId: EntityId; displayName: string } | null>(null);
const composerFieldRef = useTemplateRef('composerFieldRef');
const postId = computed(() => resolveRouteEntityId(route.params.postId));
const EMOJI_RECENT_KEY = 'life_user_recent_emojis';
const emojis = [
  '😀', '😁', '😂', '🤣',
  '😊', '😍', '😘', '😎',
  '🥹', '😭', '😡', '🤔',
  '👍', '👏', '❤️', '🎉',
  '😄', '😉', '😌', '🥰',
  '😋', '😅', '😴', '🙏',
  '💪', '👌', '👀', '✨',
  '💖', '🔥', '🌹', '🎁',
];
const emojiPages = computed(() => chunkArray(emojis, 16));
const recentEmojis = ref<string[]>(loadRecentEmojis());

const tabs = computed(() => [
  { key: 'reposts' as const, label: `转发 ${interaction.value.repostCount}` },
  { key: 'comments' as const, label: `评论 ${interaction.value.commentCount}` },
  { key: 'likes' as const, label: `点赞 ${interaction.value.likeCount}` },
]);

const activeTabIndex = computed({
  get: () => tabs.value.findIndex((tab) => tab.key === activeTab.value),
  set: (index: number) => {
    activeTab.value = tabs.value[index]?.key ?? 'comments';
  },
});

onMounted(async () => {
  await loadPostDetail();
});

watch(
  () => route.query.focus,
  async (focus) => {
    if (focus === 'comment') {
      openCommentComposer();
      await nextTick();
    }
  },
  { immediate: true },
);

async function loadPostDetail() {
  if (!postId.value) {
    await router.replace('/');
    return;
  }
  // 详情页把正文、评论、转发一次取回，Tab 切换只切视图，不再造成二次闪动。
  const [detail, commentList, repostList] = await Promise.all([
    postApi.getDetail(postId.value),
    postApi.getComments(postId.value),
    postApi.getReposts(postId.value),
  ]);
  const mapped = mapPostDetail(detail);
  post.value = mapped.post;
  interaction.value = mapped.interaction;
  comments.value = commentList.map(mapComment);
  reposts.value = repostList.map(mapRepostItem);
}

async function toggleLike() {
  await postApi.toggleLike(postId.value);
  await loadPostDetail();
}

async function repostPost() {
  if (interaction.value.repostedByCurrentUser) {
    activeTab.value = 'reposts';
    showSuccessToast('你已经转发过这条内容');
    return;
  }
  await postApi.repost(postId.value);
  activeTab.value = 'reposts';
  await loadPostDetail();
  showSuccessToast('转发成功');
}

function openCommentComposer() {
  activeTab.value = 'comments';
  composerVisible.value = true;
}

function prepareReply(parentCommentId: EntityId, replyToUserId: EntityId, displayName: string) {
  activeTab.value = 'comments';
  replyTarget.value = {
    parentCommentId,
    replyToUserId,
    displayName,
  };
  composerVisible.value = true;
}

function canDeleteComment(userId: EntityId) {
  return authStore.currentUser?.userId === userId;
}

async function submitComment() {
  const value = commentText.value.trim();
  if (!value) {
    return;
  }
  submittingComment.value = true;
  try {
    await postApi.createComment(postId.value, {
      contentText: value,
      parentCommentId: replyTarget.value?.parentCommentId ?? null,
      replyToUserId: replyTarget.value?.replyToUserId ?? null,
    });
    commentText.value = '';
    replyTarget.value = null;
    emojiPanelVisible.value = false;
    activeTab.value = 'comments';
    composerVisible.value = false;
    await loadPostDetail();
    showSuccessToast('评论成功');
  } finally {
    submittingComment.value = false;
  }
}

async function deleteComment(commentId: EntityId) {
  await postApi.deleteComment(postId.value, commentId);
  await loadPostDetail();
  showSuccessToast('删除成功');
}

function resolveRouteEntityId(value: string | string[] | undefined) {
  return typeof value === 'string' ? value : value?.[0] ?? '';
}

async function focusComposerInput() {
  await nextTick();
  const textarea = composerFieldRef.value?.$el?.querySelector?.('textarea') as HTMLTextAreaElement | undefined;
  textarea?.focus();
}

function resetReplyWhenEmpty() {
  emojiPanelVisible.value = false;
  if (!commentText.value.trim()) {
    replyTarget.value = null;
  }
}

function toggleEmojiPanel() {
  emojiPanelVisible.value = !emojiPanelVisible.value;
}

function appendEmoji(emoji: string) {
  commentText.value += emoji;
  recentEmojis.value = buildRecentEmojis(emoji, recentEmojis.value);
  localStorage.setItem(EMOJI_RECENT_KEY, JSON.stringify(recentEmojis.value));
  void focusComposerInput();
}

function removeLastCharacter() {
  const chars = Array.from(commentText.value);
  chars.pop();
  commentText.value = chars.join('');
  void focusComposerInput();
}

function loadRecentEmojis() {
  try {
    const raw = localStorage.getItem(EMOJI_RECENT_KEY);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === 'string').slice(0, 8) : [];
  } catch {
    return [];
  }
}

function buildRecentEmojis(emoji: string, current: string[]) {
  return [emoji, ...current.filter((item) => item !== emoji)].slice(0, 8);
}

function chunkArray<T>(items: T[], size: number) {
  const pages: T[][] = [];
  for (let index = 0; index < items.length; index += size) {
    pages.push(items.slice(index, index + size));
  }
  return pages;
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f7f8fa;
}

.page :deep(.van-nav-bar) {
  background: rgba(255, 255, 255, 0.96);
}

.page :deep(.van-nav-bar::after) {
  border-bottom: 1px solid rgba(229, 231, 235, 0.88);
}

.page :deep(.van-nav-bar__title) {
  color: var(--lf-color-text-primary);
  font-size: 15px;
  font-weight: 600;
}

.page :deep(.van-icon-arrow-left) {
  color: var(--lf-color-text-primary);
  font-size: 18px;
}

.post-main {
  padding: 14px 14px 16px;
  background: #fff;
}

.author-row,
.row-item,
.reply-item {
  display: flex;
  gap: 10px;
}

.avatar,
.row-avatar {
  flex: 0 0 auto;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.reply-avatar {
  flex: 0 0 auto;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.author-main,
.row-main,
.reply-main {
  flex: 1;
  min-width: 0;
}

.author-line,
.row-line {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.author-line strong,
.row-line strong {
  font-size: 14px;
  font-weight: 600;
  color: var(--lf-color-text-primary);
}

.author-tag,
.row-line span,
.author-meta,
.row-meta {
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.author-meta,
.row-meta,
.inline-meta {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
}

.meta-dot {
  color: #c4c7ce;
}

.mini-icon {
  font-size: 12px;
}

.topic {
  margin: 12px 0 0;
  color: var(--lf-color-primary);
  font-size: 14px;
  font-weight: 600;
}

.content {
  margin: 10px 0 0;
  color: var(--lf-color-text-primary);
  font-size: 16px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.poll-card {
  margin-top: 14px;
  padding: 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff7f1 0%, #fff 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 166, 130, 0.22);
}

.poll-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.poll-head strong {
  font-size: 15px;
  line-height: 1.55;
  color: var(--lf-color-text-primary);
}

.poll-badge {
  width: fit-content;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 145, 112, 0.16);
  color: #eb6b47;
  font-size: 12px;
  font-weight: 600;
}

.poll-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.poll-option {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 0 12px;
  border-radius: 14px;
  background: rgba(247, 248, 252, 0.95);
  box-shadow: inset 0 0 0 1px rgba(229, 231, 235, 0.85);
}

.poll-option span {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(255, 145, 112, 0.12);
  color: #eb6b47;
  font-size: 12px;
  font-weight: 700;
}

.poll-option strong {
  font-size: 14px;
  font-weight: 500;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-top: 12px;
}

.image-card {
  aspect-ratio: 1;
  border-radius: 10px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.tab-shell {
  margin-top: 8px;
  background: #fff;
}

.detail-tabs :deep(.van-tabs__wrap) {
  position: sticky;
  top: 46px;
  z-index: 15;
  background: rgba(255, 255, 255, 0.98);
}

.detail-tabs :deep(.van-tabs__line) {
  background: var(--lf-color-primary);
}

.detail-tabs :deep(.van-tab) {
  color: var(--lf-color-text-secondary);
  font-size: 14px;
}

.detail-tabs :deep(.van-tab--active) {
  color: var(--lf-color-text-primary);
  font-weight: 600;
}

.detail-tabs :deep(.van-tabs__content) {
  background: #fff;
}

.tab-panel {
  background: #fff;
}

.row-item {
  padding: 14px;
  border-bottom: 1px solid rgba(241, 243, 247, 0.95);
}

.row-item:last-child {
  border-bottom: none;
}

.comment-item {
  align-items: flex-start;
}

.comment-text,
.reply-text {
  margin: 4px 0 0;
  color: var(--lf-color-text-primary);
  font-size: 13px;
  line-height: 1.52;
}

.row-subtle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.liked-icon {
  color: var(--lf-color-primary);
}

.quote-box {
  margin-top: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7f8fa;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.meta-action {
  appearance: none;
  border: none;
  background: transparent;
  color: var(--lf-color-primary);
  font-size: 12px;
  padding: 0;
  line-height: 1;
  cursor: pointer;
}

.meta-action.danger {
  color: #e05a37;
}

.reply-list {
  margin-top: 8px;
  padding-left: 4px;
  border-left: 2px solid #f2f3f5;
}

.reply-item {
  gap: 8px;
  padding: 8px 0 0 8px;
}

.row-line.compact strong {
  font-size: 12px;
}

.row-line.compact span,
.row-meta.compact {
  font-size: 11px;
}

.empty-state {
  padding: 28px 16px;
  text-align: center;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.composer-popup {
  overflow: hidden;
}

.composer-sheet {
  padding: 14px 14px calc(14px + env(safe-area-inset-bottom));
  background: #fff;
}

.composer-head,
.composer-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.composer-head strong {
  font-size: 14px;
  color: var(--lf-color-text-primary);
}

.composer-field {
  margin-top: 10px;
  border-radius: 12px;
  background: #f7f8fa;
}

.composer-field :deep(.van-field__control) {
  color: var(--lf-color-text-primary);
  font-size: 14px;
  line-height: 1.6;
}

.composer-foot {
  margin-top: 12px;
}

.composer-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.emoji-panel {
  margin-top: 10px;
  padding: 10px;
  border-radius: 12px;
  background: #f7f8fa;
}

.emoji-section + .emoji-section {
  margin-top: 10px;
}

.emoji-section-title {
  margin-bottom: 8px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.emoji-swipe {
  --van-swipe-indicator-size: 6px;
  --van-swipe-indicator-margin: 8px;
  min-height: 112px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.emoji-btn {
  min-width: 0;
  font-size: 5vw;
  min-height: 36px;
}

.detail-action-bar :deep(.van-action-bar) {
  box-shadow: none;
  border-top: 1px solid rgba(229, 231, 235, 0.9);
}

.detail-action-bar :deep(.van-action-bar-button) {
  height: 48px;
  color: var(--lf-color-text-secondary);
  background: #fff;
}

.detail-action-bar :deep(.van-action-bar-button__icon) {
  font-size: 18px;
}

.detail-action-bar :deep(.van-action-bar-button__text) {
  font-size: 13px;
}

.detail-action-bar :deep(.is-active .van-action-bar-button__icon),
.detail-action-bar :deep(.is-active .van-action-bar-button__text) {
  color: var(--lf-color-primary);
}
</style>
