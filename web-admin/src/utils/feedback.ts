import { createDiscreteApi } from 'naive-ui';

const { message, dialog } = createDiscreteApi(['message', 'dialog']);

export { dialog, message };

export function confirmAction(options: {
  title: string;
  content: string;
  positiveText?: string;
  negativeText?: string;
}) {
  return new Promise<boolean>((resolve) => {
    dialog.warning({
      title: options.title,
      content: options.content,
      positiveText: options.positiveText ?? '确认',
      negativeText: options.negativeText ?? '取消',
      maskClosable: false,
      onPositiveClick: () => resolve(true),
      onNegativeClick: () => resolve(false),
      onClose: () => resolve(false),
    });
  });
}
