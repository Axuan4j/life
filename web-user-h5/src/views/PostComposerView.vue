<template>
  <div class="page">
    <div class="topbar">
      <button type="button" @click="resetComposer">取消</button>
      <strong>发布动态</strong>
      <button type="button" class="publish" :disabled="submitting || !canSubmit" @click="submit">发布</button>
    </div>

    <div class="panel">
      <div class="editor-card">
        <van-field
          v-model="bodyText"
          rows="7"
          autosize
          type="textarea"
          maxlength="2000"
          placeholder="分享这一刻，记录你想表达的内容..."
          show-word-limit
        />

        <div v-if="topic" class="meta-chip topic-chip">
          <span># {{ topic }}</span>
          <button type="button" aria-label="移除话题" @click="clearTopic">×</button>
        </div>
      </div>

      <div class="action-grid">
        <button type="button" class="action-card topic-card" @click="openTopicEditor">
          <span class="action-icon">#</span>
          <strong>{{ topic ? '修改话题' : '添加话题' }}</strong>
          <small>{{ topic ? `当前：#${topic}` : '让内容更容易被发现' }}</small>
        </button>
        <button type="button" class="action-card emoji-card" @click="emojiPickerVisible = true">
          <span class="action-icon">😊</span>
          <strong>表情</strong>
          <small>常用社交表情，一键插入正文</small>
        </button>
        <button type="button" class="action-card poll-card" @click="openPollEditor">
          <span class="action-icon">📊</span>
          <strong>{{ pollValue ? '编辑投票' : '发起投票' }}</strong>
          <small>{{ pollValue ? `${pollValue.options.length} 个选项` : '适合做轻量互动' }}</small>
        </button>
      </div>

      <div class="settings">
        <button type="button" class="setting-item" @click="visibilityPopupVisible = true">
          <div>
            <strong>谁可以看</strong>
            <p>先支持最稳的两种范围，避免做半成品权限</p>
          </div>
          <span>{{ visibilityLabel }}</span>
        </button>
      </div>

      <div v-if="pollValue" class="poll-preview">
        <div class="poll-preview-head">
          <div>
            <span class="section-badge">投票预览</span>
            <strong>{{ pollValue.question }}</strong>
          </div>
          <button type="button" class="plain-link danger" @click="clearPoll">移除</button>
        </div>
        <div class="poll-preview-options">
          <div v-for="(option, index) in pollValue.options" :key="`preview-poll-${index}`" class="poll-option">
            <span>{{ index + 1 }}</span>
            <strong>{{ option }}</strong>
          </div>
        </div>
      </div>
    </div>

    <van-popup v-model:show="topicEditorVisible" position="bottom" round class="bottom-popup">
      <div class="sheet">
        <div class="sheet-head">
          <strong>设置话题</strong>
          <button type="button" class="plain-link" @click="topicEditorVisible = false">取消</button>
        </div>
        <p class="sheet-tip">发布后会以 `#话题#` 的形式写入正文开头，方便列表和详情页单独展示。</p>
        <van-field
          v-model="topicDraft"
          label="话题"
          maxlength="20"
          clearable
          placeholder="输入话题名称"
        />
        <div class="sheet-actions">
          <van-button v-if="topic" plain round size="small" @click="clearTopic">移除话题</van-button>
          <van-button type="primary" round size="small" @click="applyTopic">保存话题</van-button>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="emojiPickerVisible" position="bottom" round class="bottom-popup">
      <div class="sheet">
        <div class="sheet-head">
          <strong>选择表情</strong>
          <button type="button" class="plain-link" @click="emojiPickerVisible = false">关闭</button>
        </div>
        <p class="sheet-tip">先放常用的一组，点击后会直接追加到正文末尾。</p>
        <van-swipe :show-indicators="emojiPages.length > 1" class="emoji-swipe" :loop="false">
          <van-swipe-item v-for="(page, pageIndex) in emojiPages" :key="`emoji-page-${pageIndex}`">
            <div class="emoji-grid">
              <button v-for="emoji in page" :key="emoji" type="button" class="emoji-btn" @click="appendEmoji(emoji)">
                {{ emoji }}
              </button>
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>
    </van-popup>

    <van-popup v-model:show="pollEditorVisible" position="bottom" round class="bottom-popup">
      <div class="sheet">
        <div class="sheet-head">
          <strong>发起投票</strong>
          <button type="button" class="plain-link" @click="pollEditorVisible = false">取消</button>
        </div>
        <p class="sheet-tip">先支持单题多选项的轻量投票，发布后会在正文里按统一结构渲染。</p>
        <van-field
          v-model="pollDraft.question"
          label="问题"
          maxlength="40"
          clearable
          placeholder="例如：今晚吃什么？"
        />
        <div class="poll-editor">
          <div v-for="(option, index) in pollDraft.options" :key="`poll-option-${index}`" class="poll-editor-row">
            <van-field
              v-model="pollDraft.options[index]"
              :label="`选项 ${index + 1}`"
              maxlength="20"
              clearable
              placeholder="输入选项内容"
            />
            <button
              v-if="pollDraft.options.length > 2"
              type="button"
              class="remove-option"
              @click="removePollOption(index)"
            >
              删除
            </button>
          </div>
        </div>
        <button v-if="pollDraft.options.length < 4" type="button" class="append-option" @click="appendPollOption">
          + 添加选项
        </button>
        <div class="sheet-actions">
          <van-button v-if="pollValue" plain round size="small" @click="clearPoll">移除投票</van-button>
          <van-button type="primary" round size="small" @click="applyPoll">保存投票</van-button>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="visibilityPopupVisible" position="bottom" round class="bottom-popup">
      <div class="sheet">
        <div class="sheet-head">
          <strong>谁可以看</strong>
          <button type="button" class="plain-link" @click="visibilityPopupVisible = false">关闭</button>
        </div>
        <div class="visibility-list">
          <button
            v-for="option in visibilityOptions"
            :key="option.value"
            type="button"
            class="visibility-item"
            :class="{ active: visibility === option.value }"
            @click="selectVisibility(option.value)"
          >
            <div>
              <strong>{{ option.label }}</strong>
              <p>{{ option.description }}</p>
            </div>
            <span>{{ visibility === option.value ? '已选' : '' }}</span>
          </button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { showFailToast, showSuccessToast } from 'vant';
import { useFeedStore } from '../stores/feed';
import {
  buildPostContent,
  normalizeTopic,
  sanitizePoll,
  type StructuredPoll,
} from '../services/post-content';

type PostVisibility = 'PUBLIC' | 'PRIVATE';

const feedStore = useFeedStore();
const bodyText = ref('');
const topic = ref('');
const topicDraft = ref('');
const visibility = ref<PostVisibility>('PUBLIC');
const submitting = ref(false);
const topicEditorVisible = ref(false);
const emojiPickerVisible = ref(false);
const pollEditorVisible = ref(false);
const visibilityPopupVisible = ref(false);
const pollValue = ref<StructuredPoll | null>(null);
const pollDraft = ref<{ question: string; options: string[] }>({
  question: '',
  options: ['', ''],
});

const visibilityOptions = [
  { value: 'PUBLIC' as const, label: '公开', description: '登录用户都可以在主页、详情和内容流里看到' },
  { value: 'PRIVATE' as const, label: '仅自己', description: '只在你自己的个人主页和详情页里可见' },
];

const visibilityLabel = computed(
  () => visibilityOptions.find((item) => item.value === visibility.value)?.label ?? '公开',
);

const composedContent = computed(() =>
  buildPostContent({
    bodyText: bodyText.value,
    topic: topic.value,
    poll: pollValue.value,
  }),
);

const canSubmit = computed(() => composedContent.value.length > 0);

const emojis = [
  '😀', '😁', '😂', '🤣',
  '😊', '😍', '😘', '😎',
  '🥹', '😭', '😡', '🤔',
  '👍', '👏', '❤️', '🎉',
  '🔥', '✨', '🙌', '💯',
  '😄', '😉', '😌', '🥰',
  '😋', '😅', '😴', '🙏',
  '💪', '👌', '👀', '🌹',
  '🥳', '🤗', '😇', '😬',
  '😏', '😤', '🤝', '💖',
  '🌈', '☀️', '🍻', '🍜',
  '☕', '🎧', '📸', '🚴',
];
const emojiPages = computed(() => chunkArray(emojis, 16));

function resetComposer() {
  bodyText.value = '';
  topic.value = '';
  topicDraft.value = '';
  visibility.value = 'PUBLIC';
  pollValue.value = null;
  pollDraft.value = createEmptyPollDraft();
  topicEditorVisible.value = false;
  emojiPickerVisible.value = false;
  pollEditorVisible.value = false;
  visibilityPopupVisible.value = false;
}

async function submit() {
  if (!canSubmit.value) {
    showFailToast('请输入正文，或先补全投票内容');
    return;
  }
  submitting.value = true;
  try {
    await feedStore.publishPost({
      contentText: composedContent.value,
      visibility: visibility.value,
      medias: [],
    });
    resetComposer();
    showSuccessToast('发布成功');
  } finally {
    submitting.value = false;
  }
}

function openTopicEditor() {
  topicDraft.value = topic.value;
  topicEditorVisible.value = true;
}

function applyTopic() {
  topic.value = normalizeTopic(topicDraft.value);
  topicDraft.value = topic.value;
  topicEditorVisible.value = false;
}

function clearTopic() {
  topic.value = '';
  topicDraft.value = '';
  topicEditorVisible.value = false;
}

function appendEmoji(emoji: string) {
  bodyText.value += emoji;
}

function openPollEditor() {
  pollDraft.value = pollValue.value
    ? {
        question: pollValue.value.question,
        options: [...pollValue.value.options],
      }
    : createEmptyPollDraft();
  pollEditorVisible.value = true;
}

function appendPollOption() {
  if (pollDraft.value.options.length >= 4) {
    return;
  }
  pollDraft.value.options.push('');
}

function removePollOption(index: number) {
  if (pollDraft.value.options.length <= 2) {
    return;
  }
  pollDraft.value.options.splice(index, 1);
}

function applyPoll() {
  const normalized = sanitizePoll(pollDraft.value);
  const hasAnyInput = pollDraft.value.question.trim() || pollDraft.value.options.some((item) => item.trim());
  if (!normalized) {
    if (!hasAnyInput) {
      pollValue.value = null;
      pollEditorVisible.value = false;
      return;
    }
    showFailToast('投票至少需要 1 个问题和 2 个有效选项');
    return;
  }
  pollValue.value = normalized;
  pollDraft.value = {
    question: normalized.question,
    options: [...normalized.options],
  };
  pollEditorVisible.value = false;
}

function clearPoll() {
  pollValue.value = null;
  pollDraft.value = createEmptyPollDraft();
  pollEditorVisible.value = false;
}

function selectVisibility(value: PostVisibility) {
  visibility.value = value;
  visibilityPopupVisible.value = false;
}

function createEmptyPollDraft() {
  return {
    question: '',
    options: ['', ''],
  };
}

function chunkArray<T>(source: T[], size: number) {
  const pages: T[][] = [];
  for (let index = 0; index < source.length; index += size) {
    pages.push(source.slice(index, index + size));
  }
  return pages;
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 32px;
  background: linear-gradient(180deg, #fff8f4 0%, #f7faff 100%);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid rgba(229, 231, 235, 0.7);
}

.topbar button {
  border: none;
  background: transparent;
  color: var(--lf-color-text-secondary);
  font-size: 15px;
}

.topbar .publish {
  padding: 8px 16px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff7648 0%, #ff4a26 100%);
  color: #fff;
  font-weight: 600;
}

.topbar .publish:disabled {
  opacity: 0.45;
}

.panel {
  padding: 16px;
}

.editor-card,
.settings,
.poll-preview {
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.editor-card {
  padding: 14px 14px 12px;
}

.panel :deep(.van-field) {
  padding: 0;
  background: transparent;
}

.panel :deep(.van-field__control) {
  min-height: 172px;
  font-size: 15px;
  line-height: 1.7;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
}

.topic-chip {
  background: rgba(255, 145, 112, 0.1);
  border: 1px solid rgba(255, 145, 112, 0.12);
  color: #e06a46;
}

.meta-chip button {
  border: none;
  background: transparent;
  color: inherit;
  font-size: 16px;
  line-height: 1;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  min-height: 132px;
  padding: 16px 14px;
  border: none;
  border-radius: 22px;
  color: #1f2937;
  text-align: left;
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.topic-card {
  background: linear-gradient(180deg, #fff5ef 0%, #fff 100%);
}

.emoji-card {
  background: linear-gradient(180deg, #f7f5ff 0%, #fff 100%);
}

.poll-card {
  background: linear-gradient(180deg, #eef9ff 0%, #fff 100%);
}

.action-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8);
  font-size: 22px;
  box-shadow: inset 0 0 0 1px rgba(229, 231, 235, 0.7);
}

.action-card strong {
  font-size: 15px;
}

.action-card small {
  color: var(--lf-color-text-secondary);
  line-height: 1.5;
}

.settings {
  margin-top: 16px;
  overflow: hidden;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 16px;
  border: none;
  background: transparent;
  text-align: left;
}

.setting-item strong {
  display: block;
  font-size: 15px;
  color: var(--lf-color-text-primary);
}

.setting-item p {
  margin: 6px 0 0;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.setting-item span {
  color: #f06d47;
  font-size: 14px;
  font-weight: 600;
}

.poll-preview {
  margin-top: 16px;
  padding: 16px;
}

.poll-preview-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.poll-preview-head strong {
  display: block;
  margin-top: 8px;
  font-size: 16px;
  line-height: 1.55;
  color: var(--lf-color-text-primary);
}

.section-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 145, 112, 0.16);
  color: #eb6b47;
  font-size: 12px;
  font-weight: 600;
}

.plain-link {
  border: none;
  background: transparent;
  color: var(--lf-color-text-secondary);
  font-size: 14px;
}

.plain-link.danger {
  color: #ef4444;
}

.poll-preview-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 14px;
}

.poll-option {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
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
  color: var(--lf-color-text-primary);
}

.bottom-popup {
  overflow: hidden;
}

.sheet {
  padding: 16px 16px calc(16px + env(safe-area-inset-bottom));
}

.sheet-head,
.sheet-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sheet-head strong {
  font-size: 15px;
  color: var(--lf-color-text-primary);
}

.sheet-tip {
  margin: 12px 0;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.sheet-actions {
  margin-top: 16px;
}

.emoji-swipe {
  margin-top: 8px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  padding-bottom: 6px;
}

.emoji-btn {
  display: grid;
  place-items: center;
  aspect-ratio: 1;
  border: none;
  border-radius: 18px;
  background: rgba(247, 248, 252, 0.95);
  box-shadow: inset 0 0 0 1px rgba(229, 231, 235, 0.8);
  font-size: 26px;
}

.poll-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.poll-editor-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.poll-editor-row :deep(.van-field) {
  flex: 1;
}

.remove-option {
  flex-shrink: 0;
  border: none;
  background: transparent;
  color: #ef4444;
  font-size: 13px;
}

.append-option {
  width: 100%;
  margin-top: 12px;
  padding: 12px 14px;
  border: 1px dashed rgba(203, 213, 225, 0.95);
  border-radius: 16px;
  background: #fff;
  color: var(--lf-color-text-secondary);
  font-size: 14px;
}

.visibility-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.visibility-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border: none;
  border-radius: 18px;
  background: rgba(247, 248, 252, 0.95);
  box-shadow: inset 0 0 0 1px rgba(229, 231, 235, 0.8);
  text-align: left;
}

.visibility-item.active {
  background: linear-gradient(180deg, #fff5ef 0%, #fff 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 145, 112, 0.22);
}

.visibility-item strong {
  display: block;
  font-size: 15px;
  color: var(--lf-color-text-primary);
}

.visibility-item p {
  margin: 6px 0 0;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.visibility-item span {
  color: #f06d47;
  font-size: 13px;
  font-weight: 600;
}

@media (max-width: 380px) {
  .action-grid {
    grid-template-columns: 1fr;
  }

  .action-card {
    min-height: 108px;
  }
}
</style>
