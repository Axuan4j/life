<template>
  <t-submenu v-if="item.children.length > 0" :value="item.routeName || item.routePath" :title="item.menuName">
    <template #icon>
      <span class="menu-icon">{{ resolveMenuIcon(item.iconName) }}</span>
    </template>
    <SideMenuBranch v-for="child in item.children" :key="child.id" :item="child" />
  </t-submenu>
  <t-menu-item v-else :value="item.routePath">
    <template #icon>
      <span class="menu-icon">{{ resolveMenuIcon(item.iconName) }}</span>
    </template>
    {{ item.menuName }}
  </t-menu-item>
</template>

<script setup lang="ts">
import type { AdminMenuNode } from '../../types/admin';
import { resolveMenuIcon } from '../../utils/iconRegistry';

defineOptions({
  name: 'SideMenuBranch',
});

defineProps<{
  item: AdminMenuNode;
}>();
</script>

<style scoped>
.menu-icon {
  display: inline-flex;
  width: 1rem;
  justify-content: center;
  color: var(--td-text-color-brand);
  font-size: 12px;
}
</style>
