package com.xuan.life.content.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("post_stat")
public class PostStat {

    @TableId
    private Long postId;
    private Long likeCount;
    private Long commentCount;
    private Long repostCount;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(Long repostCount) {
        this.repostCount = repostCount;
    }
}
