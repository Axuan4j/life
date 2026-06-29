<template>
  <div class="page">
    <header class="topbar">
      <div>
        <p class="eyebrow">Inbox</p>
        <h1>消息</h1>
      </div>
      <div class="top-actions">
        <span class="action ghost">⌕</span>
        <span class="action warm">✓</span>
      </div>
    </header>

    <section class="summary-card">
      <strong>新互动 12 条</strong>
      <p>评论、点赞、关注与系统通知统一汇总，帮助你高效查看每一次互动。</p>
    </section>

    <section class="list">
      <article v-for="item in messages" :key="item.title" class="message-card">
        <div class="avatar" :style="{ background: item.color }"></div>
        <div class="content">
          <div class="row">
            <strong>{{ item.title }}</strong>
            <span>{{ item.time }}</span>
          </div>
          <p>{{ item.preview }}</p>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
const messages = [
  {
    title: '互动通知',
    preview: '小满今天也很开心 赞了你的动态，快去看看最新互动吧。',
    time: '刚刚',
    color: 'linear-gradient(135deg, #ffd3c3 0%, #ff8b68 100%)',
  },
  {
    title: '系统消息',
    preview: '欢迎来到 Life Feed，完善资料后可以获得更准确的内容推荐。',
    time: '10 分钟前',
    color: 'linear-gradient(135deg, #cfe4ff 0%, #7db0ff 100%)',
  },
  {
    title: '关注动态',
    preview: '你关注的作者发布了新内容，点击即可快速进入查看。',
    time: '1 小时前',
    color: 'linear-gradient(135deg, #d9f6e2 0%, #7dcf95 100%)',
  },
];
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 16px 16px 110px;
  background: linear-gradient(180deg, #fff8f4 0%, #f7faff 100%);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.topbar h1 {
  margin: 6px 0 0;
  font-size: 30px;
}

.eyebrow {
  margin: 0;
  color: var(--lf-color-primary);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.top-actions {
  display: flex;
  gap: 10px;
}

.action {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  font-size: 18px;
}

.ghost {
  background: rgba(255, 255, 255, 0.9);
  color: #1d2430;
  box-shadow: 0 10px 22px rgba(26, 36, 56, 0.06);
}

.warm {
  background: linear-gradient(135deg, #ff8f70 0%, #ff5e39 100%);
  color: #fff;
  box-shadow: 0 12px 24px rgba(255, 95, 61, 0.2);
}

.summary-card {
  margin-bottom: 12px;
  padding: 18px 18px 16px;
  border-radius: 24px;
  background: linear-gradient(180deg, #fff0e7 0%, #fffaf5 100%);
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.05);
}

.summary-card strong {
  display: block;
  font-size: 18px;
}

.summary-card p {
  margin: 8px 0 0;
  color: var(--lf-color-text-secondary);
  line-height: 1.6;
}

.list {
  display: grid;
  gap: 12px;
}

.message-card {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 12px;
  padding: 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.05);
  border: 1px solid rgba(241, 243, 247, 0.9);
}

.avatar {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.45);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.row span,
.content p {
  color: var(--lf-color-text-secondary);
}

.content p {
  margin: 8px 0 0;
  line-height: 1.6;
  font-size: 14px;
}
</style>
