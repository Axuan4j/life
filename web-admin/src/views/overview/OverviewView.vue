<template>
  <t-loading :loading="loading">
    <div class="overview-page">
      <section class="overview-hero">
        <div class="hero-copy">
          <span class="hero-badge">Overview</span>
          <h2>内容平台当前的增长脉搏、内容体量和关系网络都在这里快速浏览。</h2>
          <p>这一页先聚焦最常用的四个核心指标，让后台首页有判断节奏，而不是只堆一排默认统计卡。</p>
        </div>
        <div class="hero-highlight">
          <span>今日新增帖子</span>
          <strong>{{ formatNumber(overview.todayPostCount) }}</strong>
          <p>作为后台首屏信号，优先帮助运营判断当天内容活跃度。</p>
        </div>
      </section>

      <div class="overview-grid">
        <article v-for="item in cards" :key="item.label" class="metric-card" :class="item.tone">
          <span class="metric-label">{{ item.label }}</span>
          <strong>{{ formatNumber(item.value) }}</strong>
          <p>{{ item.description }}</p>
        </article>
      </div>

      <div class="pulse-grid">
        <t-card title="运营脉搏">
          <div class="pulse-list">
            <div v-for="item in pulseItems" :key="item.label" class="pulse-item">
              <strong>{{ item.label }}</strong>
              <span>{{ item.value }}</span>
            </div>
          </div>
        </t-card>
        <t-card title="当日重点">
          <div class="focus-note">
            <strong>先看新增内容，再看关系增长。</strong>
            <p>
              V1 后台最常用的判断链路通常是：帖子新增是否正常、关注关系是否持续增长、用户体量是否稳定扩张。
            </p>
          </div>
        </t-card>
      </div>
    </div>
  </t-loading>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { fetchOverview } from '../../services/api';
import type { AdminOverview } from '../../types/admin';

const loading = ref(false);
const overview = ref<AdminOverview>({
  userCount: 0,
  postCount: 0,
  todayPostCount: 0,
  followCount: 0,
});

const cards = computed(() => [
  { label: '用户总数', value: overview.value.userCount, description: '当前 C 端账户池规模', tone: 'warm' },
  { label: '帖子总数', value: overview.value.postCount, description: '全站公开内容沉淀总量', tone: 'blue' },
  { label: '今日新增帖子', value: overview.value.todayPostCount, description: '今日内容生产活跃度', tone: 'gold' },
  { label: '关注关系总数', value: overview.value.followCount, description: '社交关系网络连接量', tone: 'mint' },
]);

const pulseItems = computed(() => [
  { label: '内容 / 用户比', value: ratio(overview.value.postCount, overview.value.userCount) },
  { label: '关系 / 用户比', value: ratio(overview.value.followCount, overview.value.userCount) },
  { label: '今日内容占比', value: percent(overview.value.todayPostCount, overview.value.postCount) },
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

function formatNumber(value: number) {
  return new Intl.NumberFormat('zh-CN').format(value);
}

function ratio(numerator: number, denominator: number) {
  if (!denominator) {
    return '0.00';
  }
  return (numerator / denominator).toFixed(2);
}

function percent(numerator: number, denominator: number) {
  if (!denominator) {
    return '0%';
  }
  return `${((numerator / denominator) * 100).toFixed(1)}%`;
}
</script>

<style scoped>
.overview-page {
  display: grid;
  gap: 18px;
}

.overview-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(260px, 340px);
  gap: 18px;
}

.hero-copy,
.hero-highlight,
.metric-card {
  border: 1px solid rgba(25, 35, 52, 0.07);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 18px 42px rgba(21, 35, 58, 0.06);
}

.hero-copy {
  padding: 28px 30px;
  background:
    radial-gradient(circle at top right, rgba(255, 211, 199, 0.72) 0%, transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(251, 246, 242, 0.94) 100%);
}

.hero-badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(232, 109, 79, 0.12);
  color: #d45a3d;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.hero-copy h2 {
  margin: 16px 0 0;
  color: #1f2b39;
  font-size: 28px;
  line-height: 1.3;
}

.hero-copy p {
  margin: 12px 0 0;
  max-width: 680px;
  color: #617081;
  font-size: 14px;
  line-height: 1.8;
}

.hero-highlight {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24px 26px;
  background: linear-gradient(160deg, rgba(255, 243, 237, 0.96) 0%, rgba(255, 255, 255, 0.92) 100%);
}

.hero-highlight span {
  color: #7a8593;
  font-size: 13px;
}

.hero-highlight strong {
  margin-top: 12px;
  color: #1f2b39;
  font-size: 48px;
  line-height: 1;
}

.hero-highlight p {
  margin: 12px 0 0;
  color: #667484;
  font-size: 13px;
  line-height: 1.7;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.metric-card {
  padding: 22px 22px 20px;
}

.metric-label {
  color: #758192;
  font-size: 13px;
}

.metric-card strong {
  display: block;
  margin-top: 14px;
  color: #1f2b39;
  font-size: 34px;
  line-height: 1;
}

.metric-card p {
  margin: 12px 0 0;
  color: #657384;
  font-size: 13px;
  line-height: 1.7;
}

.metric-card.warm {
  background: linear-gradient(180deg, rgba(255, 248, 244, 0.96) 0%, rgba(255, 255, 255, 0.88) 100%);
}

.metric-card.blue {
  background: linear-gradient(180deg, rgba(243, 249, 255, 0.96) 0%, rgba(255, 255, 255, 0.88) 100%);
}

.metric-card.gold {
  background: linear-gradient(180deg, rgba(255, 250, 239, 0.96) 0%, rgba(255, 255, 255, 0.88) 100%);
}

.metric-card.mint {
  background: linear-gradient(180deg, rgba(241, 252, 248, 0.96) 0%, rgba(255, 255, 255, 0.88) 100%);
}

.pulse-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.pulse-list {
  display: grid;
  gap: 12px;
}

.pulse-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(244, 247, 251, 0.8);
}

.pulse-item strong {
  color: var(--td-text-color-primary);
  font-size: 14px;
}

.pulse-item span {
  color: #d45a3d;
  font-size: 15px;
  font-weight: 700;
}

.focus-note strong {
  color: var(--td-text-color-primary);
  font-size: 18px;
}

.focus-note p {
  margin: 12px 0 0;
  color: var(--td-text-color-secondary);
  font-size: 14px;
  line-height: 1.8;
}

@media (max-width: 960px) {
  .overview-hero,
  .pulse-grid {
    grid-template-columns: 1fr;
  }
}
</style>
