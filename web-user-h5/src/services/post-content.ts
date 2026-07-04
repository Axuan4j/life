export interface StructuredPoll {
  question: string;
  options: string[];
}

export interface ParsedPostContent {
  topic: string;
  bodyText: string;
  poll: StructuredPoll | null;
}

const LEADING_TOPIC_PATTERN = /^\s*#([^#\r\n]{1,20})#(?:\s+|$)/;
const POLL_BLOCK_PATTERN = /(?:^|\n{2})\[投票\]\n([\s\S]*?)\n\[\/投票\]\s*$/;

export function normalizeTopic(topic: string) {
  return topic.replace(/#/g, '').trim().slice(0, 20);
}

export function extractLeadingTopic(content: string) {
  const matched = content.match(LEADING_TOPIC_PATTERN);
  return matched?.[1]?.trim() ?? '';
}

export function normalizePollQuestion(question: string) {
  return question.replace(/\s+/g, ' ').trim().slice(0, 40);
}

export function normalizePollOption(option: string) {
  return option.replace(/\s+/g, ' ').trim().slice(0, 20);
}

export function sanitizePoll(input: { question: string; options: string[] } | null | undefined) {
  if (!input) {
    return null;
  }
  const question = normalizePollQuestion(input.question);
  const options = (input.options ?? [])
    .map(normalizePollOption)
    .filter((item, index, list) => item && list.indexOf(item) === index)
    .slice(0, 4);
  if (!question || options.length < 2) {
    return null;
  }
  return { question, options };
}

export function parsePostContent(contentText: string): ParsedPostContent {
  const topic = extractLeadingTopic(contentText);
  const poll = parsePollBlock(contentText);
  const bodyText = stripLeadingTopic(stripPollBlock(contentText)).trim();
  return {
    topic,
    bodyText,
    poll,
  };
}

export function buildPostContent(payload: {
  bodyText: string;
  topic?: string;
  poll?: StructuredPoll | null;
}) {
  const topic = normalizeTopic(payload.topic ?? '');
  const bodyText = stripPollBlock(stripLeadingTopic(payload.bodyText ?? '')).trim();
  const poll = sanitizePoll(payload.poll);
  const segments: string[] = [];

  if (topic) {
    segments.push(bodyText ? `#${topic}# ${bodyText}` : `#${topic}#`);
  } else if (bodyText) {
    segments.push(bodyText);
  }

  if (poll) {
    segments.push(buildPollBlock(poll));
  }

  return segments.join('\n\n').trim();
}

function stripLeadingTopic(content: string) {
  return content.replace(LEADING_TOPIC_PATTERN, '').trimStart();
}

function stripPollBlock(content: string) {
  return content.replace(POLL_BLOCK_PATTERN, '').trimEnd();
}

function parsePollBlock(content: string): StructuredPoll | null {
  const matched = content.match(POLL_BLOCK_PATTERN);
  if (!matched) {
    return null;
  }
  const lines = matched[1]
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);
  if (lines.length < 3) {
    return null;
  }

  const [rawQuestion, ...rawOptions] = lines;
  const question = normalizePollQuestion(rawQuestion.replace(/^问题[:：]\s*/, ''));
  const options = rawOptions
    .map((line) => line.replace(/^\d+[.)、]\s*/, '').replace(/^[-*]\s*/, ''))
    .map(normalizePollOption)
    .filter((item, index, list) => item && list.indexOf(item) === index);

  if (!question || options.length < 2) {
    return null;
  }

  return {
    question,
    options,
  };
}

function buildPollBlock(poll: StructuredPoll) {
  const lines = [
    '[投票]',
    `问题：${poll.question}`,
    ...poll.options.map((option, index) => `${index + 1}. ${option}`),
    '[/投票]',
  ];
  return lines.join('\n');
}
