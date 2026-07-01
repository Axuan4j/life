export function formatDateTime(value?: string | null) {
  return value ? value.replace('T', ' ') : '-';
}

export function formatCount(value: number) {
  return new Intl.NumberFormat('zh-CN').format(value);
}

export function formatRatio(numerator: number, denominator: number) {
  if (!denominator) {
    return '0.00';
  }
  return (numerator / denominator).toFixed(2);
}

export function formatPercent(numerator: number, denominator: number) {
  if (!denominator) {
    return '0%';
  }
  return `${((numerator / denominator) * 100).toFixed(1)}%`;
}
