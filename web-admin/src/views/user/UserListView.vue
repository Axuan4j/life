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
        :row-key="(row: AdminUserListItem) => row.userId"
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

    <n-drawer v-model:show="detailVisible" :width="720" placement="right">
      <n-drawer-content title="用户详情与治理" closable>
        <div v-if="detailLoading" class="detail-loading">正在加载用户详情...</div>
        <div v-else-if="detail" class="detail-wrap">
          <div class="admin-two-column">
            <n-card class="admin-card compact-card" :bordered="false" title="基础资料">
              <div class="detail-block">
                <div class="detail-row"><span>用户名</span><strong>{{ detail.username }}</strong></div>
                <div class="detail-row"><span>昵称</span><strong>{{ detail.nickname }}</strong></div>
                <div class="detail-row"><span>注册时间</span><strong>{{ formatDateTime(detail.createdAt) }}</strong></div>
                <div class="detail-row"><span>最近登录</span><strong>{{ formatDateTime(detail.lastLoginAt) }}</strong></div>
                <div class="detail-row"><span>登录地区</span><strong>{{ detail.lastLoginRegion || '-' }}</strong></div>
                <div class="detail-row"><span>登录 IP</span><strong>{{ detail.lastLoginIp || '-' }}</strong></div>
              </div>
            </n-card>

            <n-card class="admin-card compact-card" :bordered="false" title="内容与互动">
              <div class="detail-block">
                <div class="detail-row"><span>累计帖子</span><strong>{{ detail.postCount }}</strong></div>
                <div class="detail-row"><span>公开帖子</span><strong>{{ detail.publishedPostCount }}</strong></div>
                <div class="detail-row"><span>累计评论</span><strong>{{ detail.commentCount }}</strong></div>
                <div class="detail-row"><span>关注数</span><strong>{{ detail.followingCount }}</strong></div>
                <div class="detail-row"><span>粉丝数</span><strong>{{ detail.followerCount }}</strong></div>
                <div class="detail-row"><span>被举报次数</span><strong>{{ detail.reportedCount }}</strong></div>
              </div>
            </n-card>
          </div>

          <n-card class="admin-card compact-card" :bordered="false" title="治理操作">
            <div class="admin-filter-grid">
              <n-form-item label="账号状态">
                <n-select v-model:value="governanceForm.accountStatus" :options="statusOptions" />
              </n-form-item>
              <n-form-item label="白名单">
                <n-select v-model:value="governanceForm.whitelistFlag" :options="switchOptions" />
              </n-form-item>
              <n-form-item label="限制发帖">
                <n-select v-model:value="governanceForm.postDisabled" :options="switchOptions" />
              </n-form-item>
              <n-form-item label="限制评论">
                <n-select v-model:value="governanceForm.commentDisabled" :options="switchOptions" />
              </n-form-item>
              <n-form-item label="禁言至">
                <n-input v-model:value="governanceForm.muteUntil" placeholder="例如 2026-07-10T12:00:00" />
              </n-form-item>
              <n-form-item label="积分调整">
                <n-input-number v-model:value="governanceForm.pointDelta" placeholder="可正可负" />
              </n-form-item>
              <n-form-item label="操作备注">
                <n-input v-model:value="governanceForm.reasonText" type="textarea" placeholder="填写处置原因或备注" />
              </n-form-item>
            </div>

            <div class="admin-form-actions">
              <n-button type="primary" @click="() => submitGovernance()">保存治理动作</n-button>
              <n-button secondary @click="clearMute">清除禁言</n-button>
              <n-button tertiary type="warning" @click="forceLogout">强制下线</n-button>
            </div>
          </n-card>

          <div class="admin-two-column">
            <n-card class="admin-card compact-card" :bordered="false" title="签到记录">
              <n-data-table
                striped
                :bordered="false"
                :columns="checkInColumns"
                :data="detail.recentCheckIns"
                :pagination="false"
                :row-key="(row: AdminUserCheckInLogItem) => `${row.checkInDate}-${row.createdAt}`"
              />
            </n-card>

            <n-card class="admin-card compact-card" :bordered="false" title="处置记录">
              <n-data-table
                striped
                :bordered="false"
                :columns="moderationColumns"
                :data="detail.moderationRecords"
                :pagination="false"
                :row-key="(row: AdminUserModerationRecordItem) => `${row.actionType}-${row.createdAt}`"
              />
            </n-card>
          </div>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NFormItem,
  NInput,
  NInputNumber,
  NPagination,
  NSelect,
  type DataTableColumns,
} from 'naive-ui';
import StatusTag from '../../components/admin/StatusTag.vue';
import { fetchUserDetail, fetchUsers, updateUserGovernance } from '../../services/api';
import type {
  AdminUserCheckInLogItem,
  AdminUserDetail,
  AdminUserListItem,
  AdminUserModerationRecordItem,
} from '../../types/admin';
import { formatPlatformRoleLabel } from '../../utils/adminLabels';
import { message } from '../../utils/feedback';
import { formatDateTime } from '../../utils/format';

const loading = ref(false);
const total = ref(0);
const rows = ref<AdminUserListItem[]>([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<AdminUserDetail | null>(null);
const currentUserId = ref<number | null>(null);

const query = reactive({
  pageNo: 1,
  pageSize: 20,
  keyword: '',
  roleCode: null as string | null,
  status: null as number | null,
});

const governanceForm = reactive({
  accountStatus: 1,
  whitelistFlag: 0,
  postDisabled: 0,
  commentDisabled: 0,
  muteUntil: '',
  pointDelta: 0 as number | null,
  reasonText: '',
});

const roleOptions = [{ label: '普通用户', value: 'USER' }];
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];
const switchOptions = [
  { label: '关闭', value: 0 },
  { label: '开启', value: 1 },
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
    render: (row) => h(StatusTag, {
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
  {
    key: 'operations',
    title: '操作',
    width: 120,
    render: (row) => h(
      NButton,
      {
        size: 'small',
        secondary: true,
        type: 'primary',
        onClick: () => openDetail(row.userId),
      },
      { default: () => '详情 / 处置' },
    ),
  },
];

const checkInColumns: DataTableColumns<AdminUserCheckInLogItem> = [
  { key: 'checkInDate', title: '签到日期' },
  { key: 'rewardPoints', title: '奖励积分', width: 90 },
  {
    key: 'createdAt',
    title: '记录时间',
    render: (row) => formatDateTime(row.createdAt),
  },
];

const moderationColumns: DataTableColumns<AdminUserModerationRecordItem> = [
  { key: 'actionType', title: '动作', width: 120 },
  {
    key: 'reasonText',
    title: '说明',
    render: (row) => h('div', { class: 'admin-content-cell' }, row.reasonText || '-'),
  },
  { key: 'operatorName', title: '操作人', width: 120 },
  {
    key: 'createdAt',
    title: '时间',
    width: 180,
    render: (row) => formatDateTime(row.createdAt),
  },
];

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

async function openDetail(userId: number) {
  currentUserId.value = userId;
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const response = await fetchUserDetail(userId);
    detail.value = response;
    governanceForm.accountStatus = response.accountStatus;
    governanceForm.whitelistFlag = response.whitelistFlag;
    governanceForm.postDisabled = response.postDisabled;
    governanceForm.commentDisabled = response.commentDisabled;
    governanceForm.muteUntil = response.muteUntil || '';
    governanceForm.pointDelta = 0;
    governanceForm.reasonText = response.moderationNote || '';
  } finally {
    detailLoading.value = false;
  }
}

async function submitGovernance(extra: { clearMute?: number; forceLogout?: number } = {}) {
  if (!currentUserId.value) {
    return;
  }
  await updateUserGovernance(currentUserId.value, {
    accountStatus: governanceForm.accountStatus,
    whitelistFlag: governanceForm.whitelistFlag,
    postDisabled: governanceForm.postDisabled,
    commentDisabled: governanceForm.commentDisabled,
    muteUntil: governanceForm.muteUntil || null,
    clearMute: extra.clearMute,
    forceLogout: extra.forceLogout,
    pointDelta: governanceForm.pointDelta ?? 0,
    reasonText: governanceForm.reasonText,
  });
  message.success('用户治理已更新');
  await Promise.all([loadUsers(), openDetail(currentUserId.value)]);
}

async function clearMute() {
  governanceForm.muteUntil = '';
  await submitGovernance({ clearMute: 1 });
}

async function forceLogout() {
  await submitGovernance({ forceLogout: 1 });
}

onMounted(loadUsers);
</script>

<style scoped>
.detail-loading {
  padding: 40px 0;
  color: var(--life-text-muted);
  text-align: center;
}

.detail-wrap {
  display: grid;
  gap: 16px;
}

.detail-block {
  display: grid;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--life-border);
}

.detail-row span {
  color: var(--life-text-muted);
}

.detail-row strong {
  text-align: right;
}
</style>
