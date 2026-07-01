<template>
  <n-spin :show="loading">
    <div class="admin-page">
      <section class="overview-hero">
        <n-card class="admin-card overview-hero__main" :bordered="false">
          <span class="overview-hero__badge">今日</span>
          <h3>{{ formatCount(overview.todayPostCount) }}</h3>
          <p>今日新增帖子</p>
          <div class="overview-hero__hint">
            <span>建议先看新增内容，再看用户和关注变化。</span>
          </div>
        </n-card>

        <n-card class="admin-card compact-card" :bordered="false" title="关键比例">
          <div class="pulse-list">
            <div v-for="item in pulseItems" :key="item.label" class="pulse-item">
              <div>
                <strong>{{ item.label }}</strong>
                <p>{{ item.description }}</p>
              </div>
              <span>{{ item.value }}</span>
            </div>
          </div>
        </n-card>
      </section>

      <section class="admin-stat-grid">
        <article v-for="item in cards" :key="item.label" class="admin-stat-tile" :class="item.tone">
          <span class="admin-stat-tile__label">{{ item.label }}</span>
          <div class="admin-stat-tile__value">{{ formatCount(item.value) }}</div>
          <p class="admin-stat-tile__desc">{{ item.description }}</p>
        </article>
      </section>

      <section class="admin-two-column">
        <n-card class="admin-card" :bordered="false" title="值守建议">
          <div class="focus-list">
            <article>
              <strong>先看新增内容</strong>
              <p>先确认当天发帖量是否正常，通常能最快反映站内活跃度变化。</p>
            </article>
            <article>
              <strong>再看用户关系</strong>
              <p>结合用户总量和关注关系变化，可以补充判断社区互动是否稳定。</p>
            </article>
          </div>
        </n-card>

        <n-card class="admin-card" :bordered="false" title="补充指标">
          <div class="insight-list">
            <div class="insight-item">
              <span>内容 / 用户比</span>
              <strong>{{ formatRatio(overview.postCount, overview.userCount) }}</strong>
            </div>
            <div class="insight-item">
              <span>关系 / 用户比</span>
              <strong>{{ formatRatio(overview.followCount, overview.userCount) }}</strong>
            </div>
            <div class="insight-item">
              <span>今日内容占比</span>
              <strong>{{ formatPercent(overview.todayPostCount, overview.postCount) }}</strong>
            </div>
          </div>
        </n-card>
      </section>
    </div>
  </n-spin>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { NCard, NSpin } from 'naive-ui';
import { fetchOverview } from '../../services/api';
import type { AdminOverview } from '../../types/admin';
import { formatCount, formatPercent, formatRatio } from '../../utils/format';

const loading = ref(false);
const overview = ref<AdminOverview>({
  userCount: 0,
  postCount: 0,
  todayPostCount: 0,
  followCount: 0,
});

const cards = computed(() => [
  { label: '用户总数', value: overview.value.userCount, description: '当前注册用户数', tone: 'tone-blue' },
  { label: '帖子总数', value: overview.value.postCount, description: '当前帖子总数', tone: 'tone-violet' },
  { label: '今日新增帖子', value: overview.value.todayPostCount, description: '今日新增内容数', tone: 'tone-gold' },
  { label: '关注关系总数', value: overview.value.followCount, description: '当前关注关系数', tone: 'tone-green' },
]);

const pulseItems = computed(() => [
  {
    label: '内容 / 用户比',
    value: formatRatio(overview.value.postCount, overview.value.userCount),
    description: '帖子总数与用户总数的比例',
  },
  {
    label: '关系 / 用户比',
    value: formatRatio(overview.value.followCount, overview.value.userCount),
    description: '关注关系与用户总数的比例',
  },
  {
    label: '今日内容占比',
    value: formatPercent(overview.value.todayPostCount, overview.value.postCount),
    description: '今日新增帖子占帖子总数的比例',
  },
]);

async function loadOverview() {
  loading.value = true;
  try {
    overview.value = await fetchOverview();
  } finally {
    loading.value = false;
  }
}

onMounted(loadOverview);
</script>

<style scoped>
.overview-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.overview-hero__main {
  position: relative;
  overflow: hidden;
  padding: 28px !important;
  background: var(--life-card-strong) !important;
}

.overview-hero__badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--life-primary-soft);
  color: var(--life-primary);
  font-size: 12px;
  font-weight: 700;
}

.overview-hero__main h3 {
  margin: 20px 0 0;
  color: var(--life-text);
  font-size: 64px;
  line-height: 1;
}

.overview-hero__main p {
  margin: 12px 0 0;
  color: var(--life-text-muted);
  font-size: 16px;
}

.overview-hero__hint {
  margin-top: 22px;
  max-width: 520px;
  padding: 16px 18px;
  border: 1px solid var(--life-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.74);
}

.overview-hero__hint span {
  color: var(--life-text-muted);
  font-size: 13px;
  line-height: 1.7;
}

.pulse-list,
.focus-list,
.insight-list {
  display: grid;
  gap: 12px;
}

.pulse-item,
.focus-list article,
.insight-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border: 1px solid var(--life-border);
  border-radius: 16px;
  background: var(--life-bg-soft);
}

.pulse-item strong,
.focus-list strong,
.insight-item strong {
  color: var(--life-text);
  font-size: 14px;
}

.pulse-item p,
.focus-list p,
.insight-item span {
  margin: 6px 0 0;
  color: var(--life-text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.pulse-item span,
.insight-item strong {
  flex-shrink: 0;
  color: var(--life-primary);
  font-size: 18px;
  font-weight: 700;
}

.tone-blue {
  background: rgba(241, 247, 255, 0.96);
}

.tone-violet {
  background: rgba(245, 243, 255, 0.96);
}

.tone-gold {
  background: rgba(255, 250, 235, 0.96);
}

.tone-green {
  background: rgba(240, 252, 247, 0.96);
}

@media (max-width: 1080px) {
  .overview-hero {
    grid-template-columns: 1fr;
  }

  .overview-hero__main h3 {
    font-size: 48px;
  }
}
</style>
