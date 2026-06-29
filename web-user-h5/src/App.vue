<template>
  <router-view />
  <AppTabbar v-if="showTabbar" />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import AppTabbar from './components/AppTabbar.vue';

const route = useRoute();
const showTabbar = computed(() => route.meta.showTabbar === true);
</script>
