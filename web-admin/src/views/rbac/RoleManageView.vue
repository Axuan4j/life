<template>
  <div class="admin-page">
    <div class="admin-form-actions">
      <n-button type="primary" @click="openCreateDialog">新增角色</n-button>
    </div>

    <n-card class="admin-card admin-table-card" :bordered="false" title="角色列表">
      <n-data-table
        striped
        :bordered="false"
        :columns="columns"
        :data="roles"
        :loading="loading"
        :row-key="rowKey"
      />
    </n-card>

    <n-modal
      v-model:show="dialogVisible"
      preset="card"
      :title="editingRoleId ? '编辑角色' : '新增角色'"
      style="width: 560px"
      :bordered="false"
    >
      <div class="admin-filter-grid">
        <n-form-item label="角色编码">
          <n-input v-model:value="form.roleCode" :disabled="editingSystemRole" />
        </n-form-item>
        <n-form-item label="角色名称">
          <n-input v-model:value="form.roleName" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="form.status" :disabled="editingSuperAdmin" :options="statusOptions" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="form.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" />
        </n-form-item>
      </div>

      <template #footer>
        <div class="admin-form-actions">
          <n-button secondary @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" @click="submitRole">保存</n-button>
        </div>
      </template>
    </n-modal>

    <n-modal
      v-model:show="menuDialogVisible"
      preset="card"
      :title="`分配菜单：${currentRoleName}`"
      style="width: 720px"
      :bordered="false"
    >
      <n-tree
        v-model:checked-keys="checkedMenuIds"
        block-line
        cascade
        checkable
        check-on-click
        expand-on-click
        :data="menuTreeOptions"
      />

      <template #footer>
        <div class="admin-form-actions">
          <n-button secondary @click="menuDialogVisible = false">取消</n-button>
          <n-button type="primary" @click="submitMenus">保存授权</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  NButton,
  NCard,
  NDataTable,
  NFormItem,
  NInput,
  NModal,
  NSelect,
  NTree,
  type DataTableColumns,
  type TreeOption,
} from 'naive-ui';
import StatusTag from '../../components/admin/StatusTag.vue';
import { assignRoleMenus, createRole, deleteRole, fetchMenuTree, fetchRoles, updateRole } from '../../services/api';
import { useAdminPermissionStore } from '../../stores/permission';
import type { AdminMenuNode, AdminRoleDetail, RoleFormModel } from '../../types/admin';
import { confirmAction, message } from '../../utils/feedback';
import { formatDateTime } from '../../utils/format';

const loading = ref(false);
const router = useRouter();
const permissionStore = useAdminPermissionStore();
const roles = ref<AdminRoleDetail[]>([]);
const menuTree = ref<AdminMenuNode[]>([]);
const dialogVisible = ref(false);
const menuDialogVisible = ref(false);
const editingRoleId = ref<number | null>(null);
const editingSystemRole = ref(false);
const editingSuperAdmin = ref(false);
const menuRoleId = ref<number | null>(null);
const currentRoleName = ref('');
const checkedMenuIds = ref<number[]>([]);
const form = reactive<RoleFormModel>({
  roleCode: '',
  roleName: '',
  remark: '',
  status: 1,
});

const columns: DataTableColumns<AdminRoleDetail> = [
  { key: 'roleCode', title: '角色编码', width: 160 },
  { key: 'roleName', title: '角色名称', width: 160 },
  {
    key: 'status',
    title: '状态',
    width: 120,
    render: (row) =>
      h(StatusTag, {
        label: row.status === 1 ? '启用' : '停用',
        tone: row.status === 1 ? 'success' : 'error',
      }),
  },
  {
    key: 'isSystem',
    title: '类型',
    width: 140,
    render: (row) =>
      h(StatusTag, {
        label: row.isSystem === 1 ? '系统角色' : '自定义角色',
        tone: row.isSystem === 1 ? 'warning' : 'default',
      }),
  },
  { key: 'remark', title: '备注' },
  {
    key: 'createdAt',
    title: '创建时间',
    width: 180,
    render: (row) => formatDateTime(row.createdAt),
  },
  {
    key: 'operations',
    title: '操作',
    width: 240,
    render: (row) =>
      h('div', { class: 'admin-action-group' }, [
        h(
          NButton,
          { size: 'small', secondary: true, type: 'primary', onClick: () => openEditDialog(row) },
          { default: () => '编辑' },
        ),
        h(
          NButton,
          { size: 'small', secondary: true, onClick: () => openAssignMenus(row) },
          { default: () => '分配菜单' },
        ),
        h(
          NButton,
          { size: 'small', tertiary: true, type: 'error', onClick: () => handleDelete(row.roleId) },
          { default: () => '删除' },
        ),
      ]),
  },
];

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const menuTreeOptions = computed<TreeOption[]>(() => mapMenusToTree(menuTree.value));

function rowKey(row: AdminRoleDetail) {
  return row.roleId;
}

function mapMenusToTree(items: AdminMenuNode[]): TreeOption[] {
  return items.map((item) => ({
    label: item.menuName,
    key: item.id,
    children: mapMenusToTree(item.children),
  }));
}

function resetForm() {
  editingRoleId.value = null;
  editingSystemRole.value = false;
  editingSuperAdmin.value = false;
  Object.assign(form, {
    roleCode: '',
    roleName: '',
    remark: '',
    status: 1,
  });
}

async function loadData() {
  loading.value = true;
  try {
    const [roleResponse, menuResponse] = await Promise.all([fetchRoles(), fetchMenuTree()]);
    roles.value = roleResponse;
    menuTree.value = menuResponse;
  } finally {
    loading.value = false;
  }
}

function openCreateDialog() {
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(role: AdminRoleDetail) {
  editingRoleId.value = role.roleId;
  editingSystemRole.value = role.isSystem === 1;
  editingSuperAdmin.value = role.roleCode === 'SUPER_ADMIN';
  Object.assign(form, {
    roleCode: role.roleCode,
    roleName: role.roleName,
    remark: role.remark,
    status: role.status,
  });
  dialogVisible.value = true;
}

function openAssignMenus(role: AdminRoleDetail) {
  menuRoleId.value = role.roleId;
  currentRoleName.value = role.roleName;
  checkedMenuIds.value = [...role.menuIds];
  menuDialogVisible.value = true;
}

async function submitRole() {
  if (!form.roleCode.trim() || !form.roleName.trim()) {
    message.warning('请填写完整角色信息');
    return;
  }
  if (editingRoleId.value) {
    await updateRole(editingRoleId.value, { ...form });
    message.success('角色更新成功');
  } else {
    await createRole({ ...form });
    message.success('角色创建成功');
  }
  dialogVisible.value = false;
  await permissionStore.bootstrap(router);
  await loadData();
}

async function submitMenus() {
  if (!menuRoleId.value) {
    return;
  }
  await assignRoleMenus(menuRoleId.value, checkedMenuIds.value);
  message.success('菜单分配成功');
  menuDialogVisible.value = false;
  await permissionStore.bootstrap(router);
  await loadData();
}

async function handleDelete(roleId: number) {
  const confirmed = await confirmAction({
    title: '删除角色',
    content: '删除角色后，相关管理员会失去对应后台权限，确认继续吗？',
  });
  if (!confirmed) {
    return;
  }
  await deleteRole(roleId);
  message.success('角色删除成功');
  await permissionStore.bootstrap(router);
  await loadData();
}

onMounted(loadData);
</script>
