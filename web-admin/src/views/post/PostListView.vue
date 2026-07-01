<template>
  <div class="admin-page">
    <n-card class="admin-card compact-card" :bordered="false" title="筛选条件">
      <div class="admin-filter-grid">
        <n-form-item label="关键词">
          <n-input v-model:value="query.keyword" clearable placeholder="正文 / 作者" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="query.status" clearable :options="postStatusOptions" placeholder="全部状态" />
        </n-form-item>
        <n-form-item label="可见范围">
          <n-select
            v-model:value="query.visibility"
            clearable
            :options="visibilityOptions"
            placeholder="全部可见范围"
          />
        </n-form-item>
      </div>

      <div class="admin-form-actions">
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button secondary @click="resetSearch">重置</n-button>
      </div>
    </n-card>

    <n-card class="admin-card admin-table-card" :bordered="false" title="帖子列表">
      <n-data-table
        remote
        striped
        :bordered="false"
        :columns="columns"
        :data="rows"
        :loading="loading"
        :row-key="rowKey"
      />

      <div class="admin-table-footer">
        <n-pagination
          :page="query.pageNo"
          :page-size="query.pageSize"
          :item-count="total"
          show-size-picker
          :page-sizes="[10, 20, 50]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </n-card>

    <n-modal v-model:show="editVisible" preset="card" title="编辑帖子状态" style="width: 520px" :bordered="false">
      <div class="admin-filter-grid">
        <n-form-item label="帖子状态">
          <n-select v-model:value="editForm.status" :options="postStatusOptions" />
        </n-form-item>
        <n-form-item label="可见范围">
          <n-select v-model:value="editForm.visibility" :options="visibilityOptions" />
        </n-form-item>
      </div>

      <template #footer>
        <div class="admin-form-actions">
          <n-button secondary @click="editVisible = false">取消</n-button>
          <n-button type="primary" @click="submitEdit">保存</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NFormItem,
  NInput,
  NModal,
  NPagination,
  NSelect,
  type DataTableColumns,
} from 'naive-ui';
import StatusTag from '../../components/admin/StatusTag.vue';
import { fetchPosts, updatePost } from '../../services/api';
import type { AdminPostListItem } from '../../types/admin';
import { formatPostStatusLabel, formatVisibilityLabel } from '../../utils/adminLabels';
import { message } from '../../utils/feedback';
import { formatDateTime } from '../../utils/format';

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
  status: null as string | null,
  visibility: null as string | null,
});

const postStatusOptions = [
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已隐藏', value: 'HIDDEN' },
];

const visibilityOptions = [
  { label: '公开可见', value: 'PUBLIC' },
  { label: '仅自己可见', value: 'PRIVATE' },
];

const columns: DataTableColumns<AdminPostListItem> = [
  { key: 'postId', title: '帖子 ID', width: 110 },
  { key: 'authorNickname', title: '作者', width: 140 },
  {
    key: 'contentText',
    title: '正文',
    render: (row) => h('div', { class: 'admin-content-cell' }, row.contentText),
  },
  {
    key: 'status',
    title: '状态',
    width: 120,
    render: (row) =>
      h(StatusTag, {
        label: formatPostStatusLabel(row.status),
        tone: row.status === 'PUBLISHED' ? 'success' : 'warning',
      }),
  },
  {
    key: 'visibility',
    title: '可见范围',
    width: 120,
    render: (row) =>
      h(StatusTag, {
        label: formatVisibilityLabel(row.visibility),
        tone: row.visibility === 'PUBLIC' ? 'info' : 'default',
      }),
  },
  { key: 'likeCount', title: '点赞', width: 80 },
  { key: 'commentCount', title: '评论', width: 80 },
  { key: 'repostCount', title: '转发', width: 80 },
  {
    key: 'publishedAt',
    title: '发布时间',
    width: 180,
    render: (row) => formatDateTime(row.publishedAt),
  },
  {
    key: 'operations',
    title: '操作',
    width: 100,
    render: (row) =>
      h(
        NButton,
        {
          size: 'small',
          secondary: true,
          type: 'primary',
          onClick: () => openEditDialog(row),
        },
        { default: () => '编辑' },
      ),
  },
];

function rowKey(row: AdminPostListItem) {
  return row.postId;
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
  query.status = null;
  query.visibility = null;
  query.pageNo = 1;
  loadPosts();
}

function handlePageChange(page: number) {
  query.pageNo = page;
  loadPosts();
}

function handlePageSizeChange(pageSize: number) {
  query.pageNo = 1;
  query.pageSize = pageSize;
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
  message.success('帖子更新成功');
  editVisible.value = false;
  loadPosts();
}

onMounted(loadPosts);
</script>
