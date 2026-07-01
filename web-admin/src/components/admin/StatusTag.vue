<template>
  <n-tag :bordered="false" round :type="type" size="small">
    {{ label }}
  </n-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { NTag } from 'naive-ui';

const props = defineProps<{
  tone?: 'success' | 'error' | 'warning' | 'info' | 'default';
  label: string;
}>();

const type = computed(() => {
  if (props.tone === 'success' || props.tone === 'error' || props.tone === 'warning' || props.tone === 'info') {
    return props.tone;
  }
  return 'default';
});
</script>
