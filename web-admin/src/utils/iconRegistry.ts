const iconRegistry: Record<string, string> = {
  dashboard: '◫',
  user: '◪',
  content: '▤',
  message: '✉',
  shield: '◩',
  menu: '☰',
  role: '◎',
  account: '◉',
};

export function resolveMenuIcon(iconName?: string | null) {
  return iconRegistry[iconName ?? ''] ?? '•';
}
