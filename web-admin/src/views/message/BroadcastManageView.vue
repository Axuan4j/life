<template>
  <t-card title="消息广播" subtitle="向全部 C 端用户发送站内广播，在线用户会通过 SSE 立即收到提醒。">
    <t-form ref="formRef" :data="form" :rules="rules" colon @submit="handleSubmit">
      <t-form-item label="广播标题" name="title">
        <t-input v-model="form.title" maxlength="64" placeholder="例如：版本更新通知" />
      </t-form-item>

      <t-form-item label="广播内容" name="contentText">
        <t-textarea
          v-model="form.contentText"
          :maxlength="500"
          :autosize="{ minRows: 6, maxRows: 10 }"
          placeholder="请输入要推送给全部用户的广播内容"
        />
      </t-form-item>

      <t-form-item label="预览">
        <div class="preview-card">
          <div class="preview-head">
            <strong>{{ previewTitle }}</strong>
            <t-tag theme="success" variant="light-outline">广播</t-tag>
          </div>
          <p class="preview-meta">发送方：Life 管理后台</p>
          <p class="preview-content">{{ previewContent }}</p>
        </div>
      </t-form-item>

      <t-form-item>
        <t-space>
          <t-button theme="primary" type="submit" :loading="submitting">发送广播</t-button>
          <t-button variant="outline" @click="resetForm">清空</t-button>
        </t-space>
      </t-form-item>
    </t-form>
  </t-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { MessagePlugin, type FormInstanceFunctions, type SubmitContext } from 'tdesign-vue-next';
import { sendBroadcast } from '../../services/api';

const formRef = ref<FormInstanceFunctions | null>(null);
const submitting = ref(false);
const form = reactive({
  title: '',
  contentText: '',
});

const rules = {
  title: [{ required: true, message: '请输入广播标题', type: 'error' as const }],
  contentText: [{ required: true, message: '请输入广播内容', type: 'error' as const }],
};

const previewTitle = computed(() => form.title.trim() || '版本更新通知');
const previewContent = computed(
  () => form.contentText.trim() || '这里会显示即将发送给全部用户的广播内容预览。',
);

async function handleSubmit(context: SubmitContext) {
  if (context.validateResult !== true || submitting.value) {
    return;
  }
  submitting.value = true;
  try {
    await sendBroadcast({
      title: form.title.trim(),
      contentText: form.contentText.trim(),
    });
    MessagePlugin.success('广播已发送');
    resetForm();
  } finally {
    submitting.value = false;
  }
}

function resetForm() {
  form.title = '';
  form.contentText = '';
  formRef.value?.clearValidate();
}
</script>

<style scoped>
.preview-card {
  width: 100%;
  padding: 16px 18px;
  border: 1px solid var(--td-component-border);
  border-radius: var(--td-radius-large);
  background: var(--td-bg-color-container);
}

.preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.preview-head strong {
  color: var(--td-text-color-primary);
  font-size: 16px;
}

.preview-meta {
  margin: 10px 0 0;
  color: var(--td-text-color-secondary);
  font-size: 13px;
}

.preview-content {
  margin: 12px 0 0;
  color: var(--td-text-color-primary);
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
}
</style>
