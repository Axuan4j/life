import { h, type Component } from 'vue';
import { NIcon } from 'naive-ui';
import {
  DocumentTextOutline,
  GridOutline,
  KeyOutline,
  ListOutline,
  MegaphoneOutline,
  PeopleOutline,
  PersonCircleOutline,
  ShieldCheckmarkOutline,
} from '@vicons/ionicons5';

const iconRegistry: Record<string, Component> = {
  dashboard: GridOutline,
  user: PeopleOutline,
  content: DocumentTextOutline,
  message: MegaphoneOutline,
  shield: ShieldCheckmarkOutline,
  menu: ListOutline,
  role: KeyOutline,
  account: PersonCircleOutline,
};

export function resolveMenuIconComponent(iconName?: string | null) {
  return iconRegistry[iconName ?? ''] ?? GridOutline;
}

export function renderMenuIcon(iconName?: string | null) {
  const icon = resolveMenuIconComponent(iconName);
  return () =>
    h(
      NIcon,
      {
        size: 18,
      },
      {
        default: () => h(icon),
      },
    );
}
