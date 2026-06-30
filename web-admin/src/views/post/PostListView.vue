<template>
  <t-card title="帖子列表">
    <div class="query-panel">
      <t-form class="query-form" layout="inline">
        <t-form-item label="关键词">
          <t-input v-model="query.keyword" placeholder="正文 / 作者" clearable />
        </t-form-item>
        <t-form-item label="状态">
          <t-select v-model="query.status" clearable :options="postStatusOptions" />
        </t-form-item>
        <t-form-item label="可见性">
          <t-select v-model="query.visibility" clearable :options="visibilityOptions" />
        </t-form-item>
        <t-space>
          <t-button theme="primary" @click="handleSearch">查询</t-button>
          <t-button variant="outline" @click="resetSearch">重置</t-button>
        </t-space>
      </t-form>
    </div>

    <t-table row-key="postId" :data="rows" :columns="columns" :loading="loading" hover>
      <template #contentText="{ row }">
        <div class="content-cell">{{ row.contentText }}</div>
      </template>
      <template #status="{ row }">
        <t-tag theme="primary" variant="light-outline">{{ row.status }}</t-tag>
      </template>
      <template #visibility="{ row }">
        <t-tag theme="success" variant="light-outline">{{ row.visibility }}</t-tag>
      </template>
      <template #publishedAt="{ row }">{{ formatDateTime(row.publishedAt) }}</template>
      <template #operations="{ row }">
        <t-button size="small" variant="outline" @click="openEditDialog(row)">编辑</t-button>
      </template>
    </t-table>

    <div class="table-footer">
      <t-pagination
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-page-size
        @change="handlePageChange"
      />
    </div>
  </t-card>

  <t-dialog
    v-model:visible="editVisible"
    header="编辑帖子状态"
    width="520px"
    @confirm="submitEdit"
  >
    <t-form label-align="top">
      <t-form-item label="帖子状态">
        <t-select v-model="editForm.status" :options="postStatusOptions" />
      </t-form-item>
      <t-form-item label="可见性">
        <t-select v-model="editForm.visibility" :options="visibilityOptions" />
      </t-form-item>
    </t-form>
  </t-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import { fetchPosts, updatePost } from '../../services/api';
import type { AdminPostListItem } from '../../types/admin';

const loading = ref(false);
const total = ref(0);
const rows = ref<AdminPostListItem[]>([]);
const editVisible = ref(false);
const editingPostId = ref<number | null>(null);
const editForm = reactive({
  status: 'PUBLISHED',
  visibility: 'PUBLIC',
});
const query = reactive({
  pageNo: 1,
  pageSize: 20,
  keyword: '',
  status: '',
  visibility: '',
});

const postStatusOptions = [
  { label: 'PUBLISHED', value: 'PUBLISHED' },
  { label: 'HIDDEN', value: 'HIDDEN' },
];

const visibilityOptions = [
  { label: 'PUBLIC', value: 'PUBLIC' },
  { label: 'PRIVATE', value: 'PRIVATE' },
];

const columns = [
  { colKey: 'postId', title: '帖子 ID', width: 120 },
  { colKey: 'authorNickname', title: '作者' },
  { colKey: 'contentText', title: '正文' },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'visibility', title: '可见性', width: 120 },
  { colKey: 'likeCount', title: '点赞', width: 90 },
  { colKey: 'commentCount', title: '评论', width: 90 },
  { colKey: 'repostCount', title: '转发', width: 90 },
  { colKey: 'publishedAt', title: '发布时间', width: 180 },
  { colKey: 'operations', title: '操作', width: 120 },
];

function formatDateTime(value: string) {
  return value ? value.replace('T', ' ') : '-';
}

async function loadPosts() {
  loading.value = true;
  try {
    const response = await fetchPosts({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status || undefined,
      visibility: query.visibility || undefined,
    });
    rows.value = response.items;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNo = 1;
  loadPosts();
}

function resetSearch() {
  query.keyword = '';
  query.status = '';
  query.visibility = '';
  query.pageNo = 1;
  loadPosts();
}

function handlePageChange(pageInfo: { current: number; pageSize: number }) {
  query.pageNo = pageInfo.current;
  query.pageSize = pageInfo.pageSize;
  loadPosts();
}

function openEditDialog(row: AdminPostListItem) {
  editingPostId.value = row.postId;
  editForm.status = row.status;
  editForm.visibility = row.visibility;
  editVisible.value = true;
}

async function submitEdit() {
  if (!editingPostId.value) {
    return;
  }
  await updatePost(editingPostId.value, {
    status: editForm.status,
    visibility: editForm.visibility,
  });
  MessagePlugin.success('帖子更新成功');
  editVisible.value = false;
  loadPosts();
}

onMounted(loadPosts);
</script>

<style scoped>
.query-panel {
  margin-bottom: 16px;
  padding: 14px 16px 0;
  border-radius: 20px;
  background: rgba(245, 248, 252, 0.88);
}

.query-form {
  gap: 8px 0;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.content-cell {
  max-width: 360px;
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
}
</style>
