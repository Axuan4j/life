<template>
  <div class="admin-page">
    <div class="admin-form-actions">
      <n-button type="primary" @click="openCreateDialog(0)">新增一级菜单</n-button>
    </div>

    <n-card class="admin-card admin-table-card" :bordered="false" title="菜单结构">
      <n-data-table
        striped
        :bordered="false"
        :columns="columns"
        :data="menuTree"
        :loading="loading"
        :row-key="rowKey"
        children-key="children"
      />
    </n-card>

    <n-modal
      v-model:show="dialogVisible"
      preset="card"
      :title="editingMenuId ? '编辑菜单' : '新增菜单'"
      style="width: 720px"
      :bordered="false"
    >
      <div class="admin-filter-grid">
        <n-form-item label="父级菜单">
          <n-select v-model:value="form.parentId" :options="parentOptions" />
        </n-form-item>
        <n-form-item label="菜单类型">
          <n-select v-model:value="form.menuType" :options="menuTypeOptions" />
        </n-form-item>
        <n-form-item label="菜单名称">
          <n-input v-model:value="form.menuName" />
        </n-form-item>
        <n-form-item label="路由名称">
          <n-input v-model:value="form.routeName" />
        </n-form-item>
        <n-form-item label="路由路径">
          <n-input v-model:value="form.routePath" placeholder="/rbac/menus" />
        </n-form-item>
        <n-form-item label="页面组件">
          <n-select v-model:value="form.viewKey" :disabled="form.menuType !== 'PAGE'" :options="viewKeyOptions" />
        </n-form-item>
        <n-form-item label="图标名称">
          <n-input v-model:value="form.iconName" />
        </n-form-item>
        <n-form-item label="权限编码">
          <n-input v-model:value="form.permissionCode" :disabled="form.menuType !== 'PAGE'" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="显示状态">
          <n-select v-model:value="form.visible" :options="visibleOptions" />
        </n-form-item>
        <n-form-item label="启用状态">
          <n-select v-model:value="form.status" :options="statusOptions" />
        </n-form-item>
      </div>

      <template #footer>
        <div class="admin-form-actions">
          <n-button secondary @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" @click="submitForm">保存</n-button>
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
  NInputNumber,
  NModal,
  NSelect,
  type SelectOption,
  type DataTableColumns,
} from 'naive-ui';
import StatusTag from '../../components/admin/StatusTag.vue';
import { createMenu, deleteMenu, fetchMenuTree, updateMenu } from '../../services/api';
import { useAdminPermissionStore } from '../../stores/permission';
import type { AdminMenuNode, MenuFormModel } from '../../types/admin';
import { formatMenuTypeLabel } from '../../utils/adminLabels';
import { confirmAction, message } from '../../utils/feedback';

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

const columns: DataTableColumns<AdminMenuNode> = [
  {
    key: 'menuName',
    title: '菜单名称',
    render: (row) => h('strong', row.menuName),
  },
  {
    key: 'menuType',
    title: '类型',
    width: 120,
    render: (row) => h(StatusTag, { label: formatMenuTypeLabel(row.menuType), tone: 'info' }),
  },
  { key: 'routeName', title: '路由名称', width: 160 },
  { key: 'routePath', title: '路由路径', width: 180 },
  { key: 'permissionCode', title: '权限标识', width: 180 },
  { key: 'sortOrder', title: '排序', width: 90 },
  {
    key: 'visible',
    title: '显示',
    width: 100,
    render: (row) =>
      h(StatusTag, {
        label: row.visible === 1 ? '显示' : '隐藏',
        tone: row.visible === 1 ? 'success' : 'warning',
      }),
  },
  {
    key: 'status',
    title: '状态',
    width: 100,
    render: (row) =>
      h(StatusTag, {
        label: row.status === 1 ? '启用' : '停用',
        tone: row.status === 1 ? 'success' : 'error',
      }),
  },
  {
    key: 'operations',
    title: '操作',
    width: 240,
    render: (row) =>
      h('div', { class: 'admin-action-group' }, [
        h(
          NButton,
          { size: 'small', secondary: true, type: 'primary', onClick: () => openCreateDialog(row.id) },
          { default: () => '新增子菜单' },
        ),
        h(
          NButton,
          { size: 'small', secondary: true, onClick: () => openEditDialog(row) },
          { default: () => '编辑' },
        ),
        h(
          NButton,
          { size: 'small', tertiary: true, type: 'error', onClick: () => handleDelete(row.id) },
          { default: () => '删除' },
        ),
      ]),
  },
];

const menuTypeOptions = [
  { label: '目录', value: 'DIRECTORY' },
  { label: '页面', value: 'PAGE' },
];

const visibleOptions = [
  { label: '显示', value: 1 },
  { label: '隐藏', value: 0 },
];

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const viewKeyOptions = [
  { label: '首页概览', value: 'OverviewView' },
  { label: '用户列表', value: 'UserListView' },
  { label: '内容治理工作台', value: 'ContentGovernanceView' },
  { label: '运营中心', value: 'OperationCenterView' },
  { label: '数据中心', value: 'AnalyticsView' },
  { label: '系统管理', value: 'SystemManageView' },
  { label: '兼容帖子页', value: 'PostListView' },
  { label: '兼容广播页', value: 'BroadcastManageView' },
  { label: '菜单管理', value: 'MenuManageView' },
  { label: '角色管理', value: 'RoleManageView' },
  { label: '管理员角色分配', value: 'AdminUserRoleView' },
];

const parentOptions = computed<SelectOption[]>(() => [
  { label: '顶级菜单', value: 0 },
  ...flattenMenus(menuTree.value)
    .filter((item) => item.menuType === 'DIRECTORY')
    .map((item) => ({
      label: `${'　'.repeat(item.level)}${item.menuName}`,
      value: item.id,
    })),
]);

function rowKey(row: AdminMenuNode) {
  return row.id;
}

function flattenMenus(items: AdminMenuNode[], level = 0): Array<AdminMenuNode & { level: number }> {
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
    message.warning('请填写完整菜单信息');
    return;
  }
  if (form.menuType === 'PAGE' && (!form.viewKey.trim() || !form.permissionCode.trim())) {
    message.warning('页面菜单必须填写页面组件和权限编码');
    return;
  }
  if (editingMenuId.value) {
    await updateMenu(editingMenuId.value, { ...form });
    message.success('菜单更新成功');
  } else {
    await createMenu({ ...form });
    message.success('菜单创建成功');
  }
  dialogVisible.value = false;
  await permissionStore.bootstrap(router);
  await loadMenus();
}

async function handleDelete(menuId: number) {
  const confirmed = await confirmAction({
    title: '删除菜单',
    content: '删除后不可恢复，且需要提前清理子菜单和角色绑定，确认继续吗？',
  });
  if (!confirmed) {
    return;
  }
  await deleteMenu(menuId);
  message.success('菜单删除成功');
  await permissionStore.bootstrap(router);
  await loadMenus();
}

onMounted(loadMenus);
</script>
