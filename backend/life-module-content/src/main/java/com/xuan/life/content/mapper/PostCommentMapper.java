package com.xuan.life.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.life.content.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {

    @Select({
        "<script>",
        "SELECT * FROM post_comment",
        "WHERE post_id = #{postId}",
        "AND status = 1",
        "ORDER BY created_at ASC, id ASC",
        "</script>"
    })
    List<PostComment> selectVisibleCommentsByPostId(@Param("postId") Long postId);
}
