<template>
  <div class="admin-page">
    <n-card class="admin-card compact-card" :bordered="false" title="筛选条件">
      <div class="admin-filter-grid">
        <n-form-item label="关键词">
          <n-input v-model:value="query.keyword" clearable placeholder="用户名 / 昵称" />
        </n-form-item>
      </div>
      <div class="admin-form-actions">
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button secondary @click="resetSearch">重置</n-button>
      </div>
    </n-card>

    <n-card class="admin-card admin-table-card" :bordered="false" title="管理员列表">
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

    <n-modal
      v-model:show="dialogVisible"
      preset="card"
      :title="`分配角色：${currentUserName}`"
      style="width: 560px"
      :bordered="false"
    >
      <n-checkbox-group v-model:value="selectedRoleIds" class="role-check-group">
        <n-space vertical size="small">
          <n-checkbox v-for="role in roleOptions" :key="role.roleId" :value="role.roleId">
            {{ role.roleName }}<span class="role-code-note">（{{ formatAdminRoleCodeLabel(role.roleCode) }}）</span>
          </n-checkbox>
        </n-space>
      </n-checkbox-group>

      <template #footer>
        <div class="admin-form-actions">
          <n-button secondary @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" @click="submitRoles">保存</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  NButton,
  NCard,
  NCheckbox,
  NCheckboxGroup,
  NDataTable,
  NFormItem,
  NInput,
  NModal,
  NPagination,
  NSpace,
  type DataTableColumns,
} from 'naive-ui';
import StatusTag from '../../components/admin/StatusTag.vue';
import { assignAdminUserRoles, fetchAdminUsers, fetchRoleOptions } from '../../services/api';
import { useAdminPermissionStore } from '../../stores/permission';
import type { AdminAdminUserRoleItem, AdminRoleOption } from '../../types/admin';
import { formatAdminRoleCodeLabel, formatPlatformRoleLabel } from '../../utils/adminLabels';
import { message } from '../../utils/feedback';
import { formatDateTime } from '../../utils/format';

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

const columns: DataTableColumns<AdminAdminUserRoleItem> = [
  { key: 'username', title: '用户名' },
  { key: 'nickname', title: '昵称' },
  {
    key: 'platformRoleCode',
    title: '账号类型',
    width: 120,
    render: (row) => h(StatusTag, { label: formatPlatformRoleLabel(row.platformRoleCode), tone: 'info' }),
  },
  {
    key: 'status',
    title: '状态',
    width: 100,
    render: (row) =>
      h(StatusTag, {
        label: row.status === 1 ? '启用' : '禁用',
        tone: row.status === 1 ? 'success' : 'error',
      }),
  },
  {
    key: 'roles',
    title: '后台角色',
    render: (row) =>
      h(
        'div',
        { class: 'admin-action-group' },
        row.roles.map((role) => h(StatusTag, { label: role.roleName, tone: 'info' })),
      ),
  },
  {
    key: 'createdAt',
    title: '创建时间',
    width: 180,
    render: (row) => formatDateTime(row.createdAt),
  },
  {
    key: 'operations',
    title: '操作',
    width: 120,
    render: (row) =>
      h(
        NButton,
        { size: 'small', secondary: true, type: 'primary', onClick: () => openAssignDialog(row) },
        { default: () => '分配角色' },
      ),
  },
];

function rowKey(row: AdminAdminUserRoleItem) {
  return row.userId;
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

function handlePageChange(page: number) {
  query.pageNo = page;
  loadData();
}

function handlePageSizeChange(pageSize: number) {
  query.pageNo = 1;
  query.pageSize = pageSize;
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
  message.success('管理员角色更新成功');
  dialogVisible.value = false;
  await permissionStore.bootstrap(router);
  await loadData();
}

onMounted(loadData);
</script>

<style scoped>
.role-check-group {
  width: 100%;
}

.role-code-note {
  color: var(--life-text-soft);
}
</style>
