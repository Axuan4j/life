<template>
  <t-card title="角色管理">
    <div class="page-actions">
      <t-button theme="primary" @click="openCreateDialog">新增角色</t-button>
    </div>

    <t-table row-key="roleId" :data="roles" :columns="columns" :loading="loading" bordered hover>
      <template #status="{ row }">
        <t-tag :theme="row.status === 1 ? 'success' : 'danger'" variant="light-outline">
          {{ row.status === 1 ? '启用' : '停用' }}
        </t-tag>
      </template>
      <template #isSystem="{ row }">
        <t-tag :theme="row.isSystem === 1 ? 'warning' : 'default'" variant="light-outline">
          {{ row.isSystem === 1 ? '系统角色' : '自定义角色' }}
        </t-tag>
      </template>
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      <template #operations="{ row }">
        <t-space>
          <t-button size="small" variant="outline" @click="openEditDialog(row)">编辑</t-button>
          <t-button size="small" variant="outline" @click="openAssignMenus(row)">分配菜单</t-button>
          <t-button size="small" theme="danger" variant="outline" @click="handleDelete(row.roleId)">删除</t-button>
        </t-space>
      </template>
    </t-table>
  </t-card>

  <t-dialog
    v-model:visible="dialogVisible"
    :header="editingRoleId ? '编辑角色' : '新增角色'"
    width="560px"
    @confirm="submitRole"
  >
    <t-form label-align="top">
      <t-form-item label="角色编码">
        <t-input v-model="form.roleCode" :disabled="editingSystemRole" />
      </t-form-item>
      <t-form-item label="角色名称">
        <t-input v-model="form.roleName" />
      </t-form-item>
      <t-form-item label="备注">
        <t-textarea v-model="form.remark" :autosize="{ minRows: 3, maxRows: 5 }" />
      </t-form-item>
      <t-form-item label="状态">
        <t-select v-model="form.status" :disabled="editingSuperAdmin" :options="statusOptions" />
      </t-form-item>
    </t-form>
  </t-dialog>

  <t-dialog
    v-model:visible="menuDialogVisible"
    :header="`分配菜单：${currentRoleName}`"
    width="680px"
    @confirm="submitMenus"
  >
    <t-tree
      v-model="checkedMenuIds"
      :data="menuTreeOptions"
      value-mode="all"
      checkable
      expand-all
      hover
    />
  </t-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import { useRouter } from 'vue-router';
import { assignRoleMenus, createRole, deleteRole, fetchMenuTree, fetchRoles, updateRole } from '../../services/api';
import { useAdminPermissionStore } from '../../stores/permission';
import type { AdminMenuNode, AdminRoleDetail, RoleFormModel } from '../../types/admin';
import type { TreeOptionData } from 'tdesign-vue-next';

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

const columns = [
  { colKey: 'roleCode', title: '角色编码', width: 160 },
  { colKey: 'roleName', title: '角色名称', width: 160 },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'isSystem', title: '类型', width: 140 },
  { colKey: 'remark', title: '备注' },
  { colKey: 'createdAt', title: '创建时间', width: 180 },
  { colKey: 'operations', title: '操作', width: 260 },
];

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const menuTreeOptions = computed(() => mapMenusToTree(menuTree.value));

function mapMenusToTree(items: AdminMenuNode[]): TreeOptionData[] {
  return items.map((item) => ({
    label: item.menuName,
    value: item.id,
    children: mapMenusToTree(item.children),
  }));
}

function formatDateTime(value: string) {
  return value ? value.replace('T', ' ') : '-';
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
    MessagePlugin.warning('请填写完整角色信息');
    return;
  }
  if (editingRoleId.value) {
    await updateRole(editingRoleId.value, { ...form });
    MessagePlugin.success('角色更新成功');
  } else {
    await createRole({ ...form });
    MessagePlugin.success('角色创建成功');
  }
  dialogVisible.value = false;
  await permissionStore.bootstrap(router);
  loadData();
}

async function submitMenus() {
  if (!menuRoleId.value) {
    return;
  }
  await assignRoleMenus(menuRoleId.value, checkedMenuIds.value);
  MessagePlugin.success('菜单分配成功');
  menuDialogVisible.value = false;
  await permissionStore.bootstrap(router);
  loadData();
}

async function handleDelete(roleId: number) {
  if (!window.confirm('删除角色后，相关管理员将失去对应后台权限，确认继续吗？')) {
    return;
  }
  await deleteRole(roleId);
  MessagePlugin.success('角色删除成功');
  await permissionStore.bootstrap(router);
  loadData();
}

onMounted(loadData);
</script>

<style scoped>
.page-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}
</style>
