<template>
  <t-card title="菜单管理">
    <div class="page-actions">
      <t-button theme="primary" @click="openCreateDialog(0)">新增顶级菜单</t-button>
    </div>

    <t-table row-key="id" :data="flatRows" :columns="columns" :loading="loading" bordered hover>
      <template #menuName="{ row }">
        <div class="menu-name-cell" :style="{ paddingLeft: `${row.level * 20}px` }">
          <strong>{{ row.menuName }}</strong>
        </div>
      </template>
      <template #menuType="{ row }">
        <t-tag theme="primary" variant="light-outline">{{ row.menuType }}</t-tag>
      </template>
      <template #visible="{ row }">
        <t-tag :theme="row.visible === 1 ? 'success' : 'warning'" variant="light-outline">
          {{ row.visible === 1 ? '显示' : '隐藏' }}
        </t-tag>
      </template>
      <template #status="{ row }">
        <t-tag :theme="row.status === 1 ? 'success' : 'danger'" variant="light-outline">
          {{ row.status === 1 ? '启用' : '停用' }}
        </t-tag>
      </template>
      <template #operations="{ row }">
        <t-space>
          <t-button size="small" variant="outline" @click="openCreateDialog(row.id)">新增子菜单</t-button>
          <t-button size="small" variant="outline" @click="openEditDialog(row)">编辑</t-button>
          <t-button size="small" theme="danger" variant="outline" @click="handleDelete(row.id)">删除</t-button>
        </t-space>
      </template>
    </t-table>
  </t-card>

  <t-dialog
    v-model:visible="dialogVisible"
    :header="editingMenuId ? '编辑菜单' : '新增菜单'"
    width="640px"
    @confirm="submitForm"
  >
    <t-form label-align="top">
      <t-form-item label="父级菜单">
        <t-select v-model="form.parentId" :options="parentOptions" />
      </t-form-item>
      <t-form-item label="菜单类型">
        <t-select v-model="form.menuType" :options="menuTypeOptions" />
      </t-form-item>
      <t-form-item label="菜单名称">
        <t-input v-model="form.menuName" />
      </t-form-item>
      <t-form-item label="路由名称">
        <t-input v-model="form.routeName" />
      </t-form-item>
      <t-form-item label="路由路径">
        <t-input v-model="form.routePath" placeholder="/rbac/menus" />
      </t-form-item>
      <t-form-item label="viewKey">
        <t-select v-model="form.viewKey" :disabled="form.menuType !== 'PAGE'" :options="viewKeyOptions" />
      </t-form-item>
      <t-form-item label="iconName">
        <t-input v-model="form.iconName" />
      </t-form-item>
      <t-form-item label="permissionCode">
        <t-input v-model="form.permissionCode" :disabled="form.menuType !== 'PAGE'" />
      </t-form-item>
      <t-form-item label="排序">
        <t-input-number v-model="form.sortOrder" theme="normal" />
      </t-form-item>
      <t-form-item label="显示状态">
        <t-select v-model="form.visible" :options="switchOptions" />
      </t-form-item>
      <t-form-item label="启用状态">
        <t-select v-model="form.status" :options="switchOptions" />
      </t-form-item>
    </t-form>
  </t-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import { useRouter } from 'vue-router';
import { createMenu, deleteMenu, fetchMenuTree, updateMenu } from '../../services/api';
import { useAdminPermissionStore } from '../../stores/permission';
import type { AdminMenuNode, MenuFormModel } from '../../types/admin';

interface FlatMenuRow extends AdminMenuNode {
  level: number;
}

const loading = ref(false);
const router = useRouter();
const permissionStore = useAdminPermissionStore();
const menuTree = ref<AdminMenuNode[]>([]);
const dialogVisible = ref(false);
const editingMenuId = ref<number | null>(null);
const form = reactive<MenuFormModel>({
  parentId: 0,
  menuType: 'PAGE',
  menuName: '',
  routeName: '',
  routePath: '',
  viewKey: 'OverviewView',
  iconName: 'dashboard',
  permissionCode: '',
  sortOrder: 10,
  visible: 1,
  status: 1,
});

const columns = [
  { colKey: 'menuName', title: '菜单名称' },
  { colKey: 'menuType', title: '类型', width: 120 },
  { colKey: 'routeName', title: '路由名称', width: 160 },
  { colKey: 'routePath', title: '路由路径', width: 180 },
  { colKey: 'permissionCode', title: '权限标识', width: 180 },
  { colKey: 'sortOrder', title: '排序', width: 100 },
  { colKey: 'visible', title: '显示', width: 100 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'operations', title: '操作', width: 260 },
];

const menuTypeOptions = [
  { label: 'DIRECTORY', value: 'DIRECTORY' },
  { label: 'PAGE', value: 'PAGE' },
];

const switchOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const viewKeyOptions = [
  { label: 'OverviewView', value: 'OverviewView' },
  { label: 'UserListView', value: 'UserListView' },
  { label: 'PostListView', value: 'PostListView' },
  { label: 'MenuManageView', value: 'MenuManageView' },
  { label: 'RoleManageView', value: 'RoleManageView' },
  { label: 'AdminUserRoleView', value: 'AdminUserRoleView' },
];

const flatRows = computed(() => flattenMenus(menuTree.value));
const parentOptions = computed(() => [
  { label: '顶级菜单', value: 0 },
  ...flatRows.value
    .filter((item) => item.menuType === 'DIRECTORY')
    .map((item) => ({
      label: `${'　'.repeat(item.level)}${item.menuName}`,
      value: item.id,
    })),
]);

function flattenMenus(items: AdminMenuNode[], level = 0): FlatMenuRow[] {
  return items.flatMap((item) => [
    { ...item, level },
    ...flattenMenus(item.children, level + 1),
  ]);
}

function resetForm() {
  editingMenuId.value = null;
  Object.assign(form, {
    parentId: 0,
    menuType: 'PAGE',
    menuName: '',
    routeName: '',
    routePath: '',
    viewKey: 'OverviewView',
    iconName: 'dashboard',
    permissionCode: '',
    sortOrder: 10,
    visible: 1,
    status: 1,
  });
}

async function loadMenus() {
  loading.value = true;
  try {
    menuTree.value = await fetchMenuTree();
  } finally {
    loading.value = false;
  }
}

function openCreateDialog(parentId: number) {
  resetForm();
  form.parentId = parentId;
  dialogVisible.value = true;
}

function openEditDialog(row: AdminMenuNode) {
  editingMenuId.value = row.id;
  Object.assign(form, {
    parentId: row.parentId ?? 0,
    menuType: row.menuType,
    menuName: row.menuName,
    routeName: row.routeName,
    routePath: row.routePath,
    viewKey: row.viewKey || 'OverviewView',
    iconName: row.iconName || '',
    permissionCode: row.permissionCode || '',
    sortOrder: row.sortOrder,
    visible: row.visible,
    status: row.status,
  });
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.menuName.trim() || !form.routeName.trim() || !form.routePath.trim()) {
    MessagePlugin.warning('请填写完整菜单信息');
    return;
  }
  if (form.menuType === 'PAGE' && (!form.viewKey.trim() || !form.permissionCode.trim())) {
    MessagePlugin.warning('页面菜单必须填写 viewKey 和 permissionCode');
    return;
  }
  if (editingMenuId.value) {
    await updateMenu(editingMenuId.value, { ...form });
    MessagePlugin.success('菜单更新成功');
  } else {
    await createMenu({ ...form });
    MessagePlugin.success('菜单创建成功');
  }
  dialogVisible.value = false;
  await permissionStore.bootstrap(router);
  loadMenus();
}

async function handleDelete(menuId: number) {
  if (!window.confirm('删除后不可恢复，且必须先清理子菜单和角色绑定，确认继续吗？')) {
    return;
  }
  await deleteMenu(menuId);
  MessagePlugin.success('菜单删除成功');
  await permissionStore.bootstrap(router);
  loadMenus();
}

onMounted(loadMenus);
</script>

<style scoped>
.page-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.menu-name-cell {
  display: flex;
  align-items: center;
  min-height: 32px;
}
</style>
