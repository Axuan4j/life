package com.xuan.life.feed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("feed_exposure")
public class FeedExposure {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long viewerUserId;
    private Long postId;
    private String sourceType;
    private String cursorToken;
    private LocalDateTime shownAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getViewerUserId() {
        return viewerUserId;
    }

    public void setViewerUserId(Long viewerUserId) {
        this.viewerUserId = viewerUserId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCursorToken() {
        return cursorToken;
    }

    public void setCursorToken(String cursorToken) {
        this.cursorToken = cursorToken;
    }

    public LocalDateTime getShownAt() {
        return shownAt;
    }

    public void setShownAt(LocalDateTime shownAt) {
        this.shownAt = shownAt;
    }
}
