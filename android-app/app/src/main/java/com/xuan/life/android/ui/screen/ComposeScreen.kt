package com.xuan.life.android.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ComposeScreen(
    innerPadding: PaddingValues,
    isSubmitting: Boolean,
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var content by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("") }
    var topicDraft by rememberSaveable { mutableStateOf("") }
    var topicEditorVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Color(0xFFFFF8F4))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("取消", color = Color(0xFF7D8795), modifier = Modifier.clickable(onClick = onCancel))
            Text("发帖", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    onSubmit(buildPostPayload(content, topic))
                    if (content.isNotBlank()) {
                        content = ""
                        topic = ""
                        topicDraft = ""
                    }
                },
                enabled = !isSubmitting && content.isNotBlank(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A3C)),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("发布")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White),
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .padding(12.dp),
                placeholder = { Text("分享你的生活...") },
                shape = RoundedCornerShape(18.dp),
            )
        }

        Row(modifier = Modifier.padding(top = 14.dp)) {
            ComposeChip(
                text = if (topic.isBlank()) "# 添加话题" else "# $topic",
                onClick = {
                    topicDraft = topic
                    topicEditorVisible = true
                },
            )
            ComposeChip("@ 提及好友", modifier = Modifier.padding(start = 10.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MediaBlock(
                label = "封面",
                brush = Brush.linearGradient(listOf(Color(0xFF9BE4B4), Color(0xFF5CA9F4))),
                modifier = Modifier.weight(1f),
            )
            MediaBlock(label = "图片 / 短视频", modifier = Modifier.weight(1f))
            MediaBlock(label = "继续添加", modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
        ) {
            SettingRow("所在位置", "去选择")
            SettingRow("谁可以看", "公开")
            SettingRow("更多设置", ">")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("☺ 表情", color = Color(0xFF7D8795))
            Text(
                "# 话题",
                color = Color(0xFF7D8795),
                modifier = Modifier.clickable {
                    topicDraft = topic
                    topicEditorVisible = true
                },
            )
            Text("📊 投票", color = Color(0xFF7D8795))
            Text("🗂 活动", color = Color(0xFF7D8795))
        }
    }

    if (topicEditorVisible) {
        AlertDialog(
            onDismissRequest = { topicEditorVisible = false },
            title = {
                Text("设置话题")
            },
            text = {
                Column {
                    Text(
                        "发布时会把话题写成 #话题# 放在正文前面，只有你主动设置的话题才会单独展示。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7D8795),
                    )
                    OutlinedTextField(
                        value = topicDraft,
                        onValueChange = { topicDraft = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        singleLine = true,
                        placeholder = { Text("输入话题，最多 20 个字") },
                    )
                    if (topic.isNotBlank()) {
                        TextButton(
                            onClick = {
                                topic = ""
                                topicDraft = ""
                                topicEditorVisible = false
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text("移除话题")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        topic = normalizeTopic(topicDraft)
                        topicDraft = topic
                        topicEditorVisible = false
                    },
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { topicEditorVisible = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun ComposeChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0x14FF916F))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text, color = Color(0xFFE06A46), style = MaterialTheme.typography.bodyMedium)
    }
}

private fun normalizeTopic(raw: String): String {
    return raw.replace("#", "").trim().take(20)
}

private fun buildPostPayload(content: String, topic: String): String {
    val body = content.trim()
    val normalizedTopic = normalizeTopic(topic)
    // 发帖接口仍然只收 contentText，所以这里统一把显式话题折叠成 #话题# 正文 的服务端约定。
    return if (normalizedTopic.isBlank()) {
        body
    } else if (body.isBlank()) {
        "#$normalizedTopic#"
    } else {
        "#$normalizedTopic# $body"
    }
}

@Composable
private fun MediaBlock(
    label: String,
    modifier: Modifier = Modifier,
    brush: Brush? = null,
) {
    Box(
        modifier = modifier
            .height(104.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush ?: Brush.linearGradient(listOf(Color.White, Color.White)))
            .then(
                if (brush == null) {
                    Modifier.background(Color.White)
                } else {
                    Modifier
                },
            ),
        contentAlignment = if (brush == null) Alignment.Center else Alignment.BottomStart,
    ) {
        if (brush == null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent),
            )
            Text(label, color = Color(0xFF7D8795), modifier = Modifier.padding(8.dp))
        } else {
            Text(label, color = Color.White, modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
private fun SettingRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(value, color = Color(0xFF7D8795))
    }
}
