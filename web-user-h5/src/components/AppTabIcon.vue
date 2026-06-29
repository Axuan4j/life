<template>
  <svg
    class="tab-icon"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.9"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <template v-if="name === 'home'">
      <path d="M4.5 10.5L12 4l7.5 6.5" />
      <path d="M6.5 9.5V19h11V9.5" />
      <path d="M10 19v-5h4v5" />
    </template>
    <template v-else-if="name === 'discover'">
      <circle cx="12" cy="12" r="7.5" />
      <path d="M12 7.5l1.35 3.15 3.15 1.35-3.15 1.35L12 16.5l-1.35-3.15L7.5 12l3.15-1.35L12 7.5z" />
    </template>
    <template v-else-if="name === 'compose'">
      <path d="M12 6.5v11" />
      <path d="M6.5 12h11" />
    </template>
    <template v-else-if="name === 'message'">
      <path d="M5.5 7.5h13a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2h-7l-4 3v-3h-2a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2z" />
      <path d="M8.5 11.5h7" />
      <path d="M8.5 14.5h4.5" />
    </template>
    <template v-else>
      <circle cx="12" cy="9" r="3.25" />
      <path d="M6.5 19a5.5 5.5 0 0 1 11 0" />
    </template>
  </svg>
</template>

<script setup lang="ts">
defineProps<{
  name: 'home' | 'discover' | 'compose' | 'message' | 'profile';
}>();
</script>

<style scoped>
.tab-icon {
  width: 1em;
  height: 1em;
  display: block;
}
</style>
