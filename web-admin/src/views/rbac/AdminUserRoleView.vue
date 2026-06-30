<template>
  <t-card title="管理员角色绑定">
    <t-form class="query-form" layout="inline">
      <t-form-item label="关键词">
        <t-input v-model="query.keyword" placeholder="用户名 / 昵称" clearable />
      </t-form-item>
      <t-space>
        <t-button theme="primary" @click="handleSearch">查询</t-button>
        <t-button variant="outline" @click="resetSearch">重置</t-button>
      </t-space>
    </t-form>

    <t-table row-key="userId" :data="rows" :columns="columns" :loading="loading" bordered hover>
      <template #status="{ row }">
        <t-tag :theme="row.status === 1 ? 'success' : 'danger'" variant="light-outline">
          {{ row.status === 1 ? '启用' : '禁用' }}
        </t-tag>
      </template>
      <template #roles="{ row }">
        <t-space break-line>
          <t-tag v-for="role in row.roles" :key="role.roleId" theme="primary" variant="light-outline">
            {{ role.roleName }}
          </t-tag>
        </t-space>
      </template>
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      <template #operations="{ row }">
        <t-button size="small" variant="outline" @click="openAssignDialog(row)">分配角色</t-button>
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
    v-model:visible="dialogVisible"
    :header="`分配角色：${currentUserName}`"
    width="560px"
    @confirm="submitRoles"
  >
    <t-checkbox-group v-model="selectedRoleIds" class="role-check-group">
      <t-checkbox v-for="role in roleOptions" :key="role.roleId" :value="role.roleId">
        {{ role.roleName }}（{{ role.roleCode }}）
      </t-checkbox>
    </t-checkbox-group>
  </t-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import { useRouter } from 'vue-router';
import { assignAdminUserRoles, fetchAdminUsers, fetchRoleOptions } from '../../services/api';
import { useAdminPermissionStore } from '../../stores/permission';
import type { AdminAdminUserRoleItem, AdminRoleOption } from '../../types/admin';

const loading = ref(false);
const router = useRouter();
const permissionStore = useAdminPermissionStore();
const total = ref(0);
const rows = ref<AdminAdminUserRoleItem[]>([]);
const roleOptions = ref<AdminRoleOption[]>([]);
const dialogVisible = ref(false);
const currentUserId = ref<number | null>(null);
const currentUserName = ref('');
const selectedRoleIds = ref<number[]>([]);
const query = reactive({
  pageNo: 1,
  pageSize: 20,
  keyword: '',
});

const columns = [
  { colKey: 'username', title: '用户名' },
  { colKey: 'nickname', title: '昵称' },
  { colKey: 'platformRoleCode', title: '账号类型', width: 120 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'roles', title: '后台角色' },
  { colKey: 'createdAt', title: '创建时间', width: 180 },
  { colKey: 'operations', title: '操作', width: 140 },
];

function formatDateTime(value: string) {
  return value ? value.replace('T', ' ') : '-';
}

async function loadData() {
  loading.value = true;
  try {
    const [userResponse, roleResponse] = await Promise.all([
      fetchAdminUsers({
        pageNo: query.pageNo,
        pageSize: query.pageSize,
        keyword: query.keyword || undefined,
      }),
      fetchRoleOptions(),
    ]);
    rows.value = userResponse.items;
    total.value = userResponse.total;
    roleOptions.value = roleResponse;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNo = 1;
  loadData();
}

function resetSearch() {
  query.keyword = '';
  query.pageNo = 1;
  loadData();
}

function handlePageChange(pageInfo: { current: number; pageSize: number }) {
  query.pageNo = pageInfo.current;
  query.pageSize = pageInfo.pageSize;
  loadData();
}

function openAssignDialog(row: AdminAdminUserRoleItem) {
  currentUserId.value = row.userId;
  currentUserName.value = row.nickname || row.username;
  selectedRoleIds.value = [...row.roleIds];
  dialogVisible.value = true;
}

async function submitRoles() {
  if (!currentUserId.value) {
    return;
  }
  await assignAdminUserRoles(currentUserId.value, selectedRoleIds.value);
  MessagePlugin.success('管理员角色更新成功');
  dialogVisible.value = false;
  await permissionStore.bootstrap(router);
  loadData();
}

onMounted(loadData);
</script>

<style scoped>
.query-form {
  margin-bottom: 16px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.role-check-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
