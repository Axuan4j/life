<template>
  <t-card title="用户列表">
    <div class="query-panel">
      <t-form class="query-form" layout="inline">
        <t-form-item label="关键词">
          <t-input v-model="query.keyword" placeholder="用户名 / 昵称" clearable />
        </t-form-item>
        <t-form-item label="平台角色">
          <t-select v-model="query.roleCode" clearable :options="roleOptions" />
        </t-form-item>
        <t-form-item label="状态">
          <t-select v-model="query.status" clearable :options="statusOptions" />
        </t-form-item>
        <t-space>
          <t-button theme="primary" @click="handleSearch">查询</t-button>
          <t-button variant="outline" @click="resetSearch">重置</t-button>
        </t-space>
      </t-form>
    </div>

    <t-table row-key="userId" :data="rows" :columns="columns" :loading="loading" hover>
      <template #roleCode="{ row }">
        <t-tag theme="primary" variant="light-outline">{{ row.roleCode }}</t-tag>
      </template>
      <template #status="{ row }">
        <t-tag :theme="row.status === 1 ? 'success' : 'danger'" variant="light-outline">
          {{ row.status === 1 ? '启用' : '禁用' }}
        </t-tag>
      </template>
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { fetchUsers } from '../../services/api';
import type { AdminUserListItem } from '../../types/admin';

const loading = ref(false);
const total = ref(0);
const rows = ref<AdminUserListItem[]>([]);
const query = reactive({
  pageNo: 1,
  pageSize: 20,
  keyword: '',
  roleCode: '',
  status: '' as number | '',
});

const roleOptions = [
  { label: '普通用户', value: 'USER' },
];

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const columns = [
  { colKey: 'username', title: '用户名' },
  { colKey: 'nickname', title: '昵称' },
  { colKey: 'roleCode', title: '平台角色' },
  { colKey: 'status', title: '状态' },
  { colKey: 'postCount', title: '帖子数' },
  { colKey: 'followingCount', title: '关注数' },
  { colKey: 'followerCount', title: '粉丝数' },
  { colKey: 'createdAt', title: '注册时间' },
];

function formatDateTime(value: string) {
  return value ? value.replace('T', ' ') : '-';
}

async function loadUsers() {
  loading.value = true;
  try {
    const response = await fetchUsers({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      roleCode: query.roleCode || undefined,
      status: query.status,
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
  query.roleCode = '';
  query.status = '';
  query.pageNo = 1;
  loadUsers();
}

function handlePageChange(pageInfo: { current: number; pageSize: number }) {
  query.pageNo = pageInfo.current;
  query.pageSize = pageInfo.pageSize;
  loadUsers();
}

onMounted(loadUsers);
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
</style>
