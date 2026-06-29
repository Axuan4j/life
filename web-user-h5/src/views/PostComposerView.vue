<template>
  <div class="page">
    <div class="topbar">
      <button type="button" @click="resetComposer">取消</button>
      <strong>发帖</strong>
      <button type="button" class="publish" :disabled="submitting" @click="submit">发布</button>
    </div>
    <div class="panel">
      <div class="editor-card">
        <van-field
          v-model="contentText"
          rows="7"
          autosize
          type="textarea"
          maxlength="2000"
          placeholder="分享你的生活..."
          show-word-limit
        />
      </div>
      <div class="chips">
        <button type="button" class="chip-button" @click="openTopicEditor">
          {{ currentTopic ? `# ${currentTopic}` : '# 添加话题' }}
        </button>
        <span>@ 提及好友</span>
      </div>
      <div class="media-row">
        <div class="media-card preview">
          <span>封面</span>
        </div>
        <div class="media-card empty">图片 / 短视频</div>
        <div class="media-card empty">继续添加</div>
      </div>
      <div class="settings">
        <div class="setting-item">
          <strong>所在位置</strong>
          <span>去选择</span>
        </div>
        <div class="setting-item">
          <strong>谁可以看</strong>
          <span>公开</span>
        </div>
        <div class="setting-item">
          <strong>更多设置</strong>
          <span>></span>
        </div>
      </div>
      <div class="toolbar">
        <span>☺ 表情</span>
        <button type="button" class="toolbar-button" @click="openTopicEditor"># 话题</button>
        <span>📊 投票</span>
        <span>🗂 活动</span>
      </div>
    </div>

    <van-popup v-model:show="topicEditorVisible" position="bottom" round class="topic-popup">
      <div class="topic-sheet">
        <div class="topic-head">
          <strong>设置话题</strong>
          <button type="button" class="plain-btn" @click="topicEditorVisible = false">取消</button>
        </div>
        <p class="topic-tip">发布后会以 `#话题#` 的形式写入正文，只有你明确设置的话题才会单独展示。</p>
        <van-field
          v-model="topicDraft"
          label="话题"
          maxlength="20"
          clearable
          placeholder="输入话题名称"
        />
        <div class="topic-actions">
          <van-button v-if="currentTopic" plain round size="small" @click="clearTopic">移除话题</van-button>
          <van-button type="primary" round size="small" @click="applyTopic">保存话题</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { showFailToast, showSuccessToast } from 'vant';
import { useFeedStore } from '../stores/feed';

const contentText = ref('');
const feedStore = useFeedStore();
const submitting = ref(false);
const topicEditorVisible = ref(false);
const topicDraft = ref('');
const currentTopic = computed(() => extractLeadingTopic(contentText.value));

function resetComposer() {
  contentText.value = '';
  topicDraft.value = '';
  topicEditorVisible.value = false;
}

async function submit() {
  const value = contentText.value.trim();
  if (!value) {
    showFailToast('请输入帖子内容');
    return;
  }
  submitting.value = true;
  try {
    await feedStore.publishPost(value);
    contentText.value = '';
    topicDraft.value = '';
    topicEditorVisible.value = false;
    showSuccessToast('发布成功');
  } finally {
    submitting.value = false;
  }
}

function openTopicEditor() {
  topicDraft.value = currentTopic.value;
  topicEditorVisible.value = true;
}

function applyTopic() {
  const normalized = normalizeTopic(topicDraft.value);
  contentText.value = rewriteLeadingTopic(contentText.value, normalized);
  topicDraft.value = normalized;
  topicEditorVisible.value = false;
}

function clearTopic() {
  contentText.value = rewriteLeadingTopic(contentText.value, '');
  topicDraft.value = '';
  topicEditorVisible.value = false;
}

function extractLeadingTopic(content: string) {
  const matched = content.match(/^\s*#([^#\r\n]{1,20})#(?:\s+|$)/);
  return matched?.[1]?.trim() ?? '';
}

function normalizeTopic(topic: string) {
  return topic.replace(/#/g, '').trim().slice(0, 20);
}

function rewriteLeadingTopic(content: string, topic: string) {
  const body = content.replace(/^\s*#[^#\r\n]{1,20}#(?:\s+)?/, '').trimStart();
  if (!topic) {
    return body;
  }
  return body ? `#${topic}# ${body}` : `#${topic}#`;
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 110px;
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
  padding: 8px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff7648 0%, #ff4a26 100%);
  color: #fff;
}

.panel {
  margin: 0;
  padding: 16px 16px 0;
}

.editor-card {
  padding: 14px 14px 8px;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.panel :deep(.van-field) {
  padding: 0;
  background: transparent;
}

.panel :deep(.van-field__control) {
  min-height: 168px;
  font-size: 15px;
  line-height: 1.7;
}

.chips {
  display: flex;
  gap: 10px;
  margin: 16px 0 14px;
}

.chips span,
.chip-button {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 145, 112, 0.1);
  border: 1px solid rgba(255, 145, 112, 0.12);
  font-size: 13px;
  color: #e06a46;
}

.chip-button {
  font: inherit;
}

.media-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.media-card {
  height: 104px;
  border-radius: 16px;
}

.preview {
  display: flex;
  align-items: flex-end;
  padding: 12px;
  background: linear-gradient(135deg, #9be4b4 0%, #5ca9f4 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.empty {
  display: grid;
  place-items: center;
  background: #fff;
  border: 1px dashed rgba(203, 213, 225, 0.95);
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.settings {
  overflow: hidden;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.75);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item span {
  color: var(--lf-color-text-secondary);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  padding: 18px 4px 10px;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.toolbar-button {
  border: none;
  background: transparent;
  color: inherit;
  font: inherit;
}

.topic-sheet {
  padding: 16px 16px calc(16px + env(safe-area-inset-bottom));
}

.topic-head,
.topic-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.topic-head strong {
  font-size: 15px;
  color: var(--lf-color-text-primary);
}

.plain-btn {
  border: none;
  background: transparent;
  color: var(--lf-color-text-secondary);
  font-size: 14px;
}

.topic-tip {
  margin: 12px 0;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.topic-actions {
  margin-top: 16px;
}
</style>
