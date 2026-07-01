<template>
  <div class="admin-page">
    <section class="admin-two-column">
      <n-card class="admin-card" :bordered="false" title="广播内容">
        <div class="admin-filter-grid">
          <n-form-item label="广播标题">
            <n-input v-model:value="form.title" maxlength="64" placeholder="例如：版本更新通知" />
          </n-form-item>

          <n-form-item label="广播内容" class="broadcast-field">
            <n-input
              v-model:value="form.contentText"
              type="textarea"
              maxlength="500"
              :autosize="{ minRows: 8, maxRows: 12 }"
              placeholder="请输入要推送给全部用户的广播内容"
            />
          </n-form-item>
        </div>

        <div class="admin-form-actions">
          <n-button type="primary" :loading="submitting" @click="handleSubmit">发送广播</n-button>
          <n-button secondary @click="resetForm">清空</n-button>
        </div>
      </n-card>

      <n-card class="admin-card" :bordered="false" title="消息预览">
        <div class="preview-card">
          <div class="preview-card__head">
            <div>
              <strong>{{ previewTitle }}</strong>
              <p>来源：Life 管理后台</p>
            </div>
            <status-tag label="广播" tone="success" />
          </div>

          <p class="preview-card__content">{{ previewContent }}</p>

          <div class="preview-card__footer">
            <span>发送后会按现有广播流程下发</span>
          </div>
        </div>
      </n-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { NButton, NCard, NFormItem, NInput } from 'naive-ui';
import StatusTag from '../../components/admin/StatusTag.vue';
import { sendBroadcast } from '../../services/api';
import { message } from '../../utils/feedback';

const submitting = ref(false);
const form = reactive({
  title: '',
  contentText: '',
});

const previewTitle = computed(() => form.title.trim() || '版本更新通知');
const previewContent = computed(
  () => form.contentText.trim() || '这里会显示即将发送给全部用户的广播内容预览。',
);

async function handleSubmit() {
  if (!form.title.trim()) {
    message.warning('请输入广播标题');
    return;
  }
  if (!form.contentText.trim()) {
    message.warning('请输入广播内容');
    return;
  }

  submitting.value = true;
  try {
    await sendBroadcast({
      title: form.title.trim(),
      contentText: form.contentText.trim(),
    });
    message.success('广播已发送');
    resetForm();
  } finally {
    submitting.value = false;
  }
}

function resetForm() {
  form.title = '';
  form.contentText = '';
}
</script>

<style scoped>
.broadcast-field {
  grid-column: 1 / -1;
}

.preview-card {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  padding: 20px;
  border: 1px solid var(--life-border);
  border-radius: 18px;
  background: var(--life-bg-soft);
}

.preview-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.preview-card__head strong {
  color: var(--life-text);
  font-size: 18px;
}

.preview-card__head p {
  margin: 8px 0 0;
  color: var(--life-text-soft);
  font-size: 12px;
}

.preview-card__content {
  flex: 1;
  margin: 22px 0 0;
  color: var(--life-text-muted);
  font-size: 14px;
  line-height: 1.9;
  white-space: pre-wrap;
}

.preview-card__footer {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed var(--life-border-strong);
  color: var(--life-text-soft);
  font-size: 12px;
}
</style>
