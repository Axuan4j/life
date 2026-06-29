package com.xuan.life.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.life.content.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    @Select({
        "SELECT * FROM post_like",
        "WHERE post_id = #{postId}",
        "ORDER BY created_at DESC, id DESC",
        "LIMIT #{limit}"
    })
    List<PostLike> selectRecentLikers(@Param("postId") Long postId, @Param("limit") int limit);
}
