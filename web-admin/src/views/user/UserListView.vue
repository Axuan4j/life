<template>
  <div class="admin-page">
    <n-card class="admin-card compact-card" :bordered="false" title="筛选条件">
      <div class="admin-filter-grid">
        <n-form-item label="关键词">
          <n-input v-model:value="query.keyword" clearable placeholder="用户名 / 昵称" />
        </n-form-item>
        <n-form-item label="平台角色">
          <n-select v-model:value="query.roleCode" clearable :options="roleOptions" placeholder="全部角色" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="query.status" clearable :options="statusOptions" placeholder="全部状态" />
        </n-form-item>
      </div>

      <div class="admin-form-actions">
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button secondary @click="resetSearch">重置</n-button>
      </div>
    </n-card>

    <n-card class="admin-card admin-table-card" :bordered="false" title="用户列表">
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
  NPagination,
  NSelect,
  type DataTableColumns,
} from 'naive-ui';
import StatusTag from '../../components/admin/StatusTag.vue';
import { fetchUsers } from '../../services/api';
import type { AdminUserListItem } from '../../types/admin';
import { formatPlatformRoleLabel } from '../../utils/adminLabels';
import { formatDateTime } from '../../utils/format';

const loading = ref(false);
const total = ref(0);
const rows = ref<AdminUserListItem[]>([]);
const query = reactive({
  pageNo: 1,
  pageSize: 20,
  keyword: '',
  roleCode: null as string | null,
  status: null as number | null,
});

const roleOptions = [{ label: '普通用户', value: 'USER' }];
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const columns: DataTableColumns<AdminUserListItem> = [
  { key: 'username', title: '用户名' },
  { key: 'nickname', title: '昵称' },
  {
    key: 'roleCode',
    title: '平台角色',
    render: (row) => h(StatusTag, { label: formatPlatformRoleLabel(row.roleCode), tone: 'info' }),
  },
  {
    key: 'status',
    title: '状态',
    render: (row) =>
      h(StatusTag, {
        label: row.status === 1 ? '启用' : '禁用',
        tone: row.status === 1 ? 'success' : 'error',
      }),
  },
  { key: 'postCount', title: '帖子数' },
  { key: 'followingCount', title: '关注数' },
  { key: 'followerCount', title: '粉丝数' },
  {
    key: 'createdAt',
    title: '注册时间',
    render: (row) => formatDateTime(row.createdAt),
  },
];

function rowKey(row: AdminUserListItem) {
  return row.userId;
}

async function loadUsers() {
  loading.value = true;
  try {
    const response = await fetchUsers({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      roleCode: query.roleCode || undefined,
      status: query.status ?? '',
    });
    rows.value = response.items;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNo = 1;
  loadUsers();
}

function resetSearch() {
  query.keyword = '';
  query.roleCode = null;
  query.status = null;
  query.pageNo = 1;
  loadUsers();
}

function handlePageChange(page: number) {
  query.pageNo = page;
  loadUsers();
}

function handlePageSizeChange(pageSize: number) {
  query.pageNo = 1;
  query.pageSize = pageSize;
  loadUsers();
}

onMounted(loadUsers);
</script>
