package com.xuan.life.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.life.content.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select({
        "<script>",
        "SELECT * FROM post",
        "WHERE status = 1",
        "AND review_status = 1",
        "AND visibility = 'PUBLIC'",
        "AND author_id = #{authorId}",
        "ORDER BY published_at DESC, id DESC",
        "</script>"
    })
    List<Post> selectPublicPostsByAuthor(Page<Post> page, @Param("authorId") Long authorId);

    @Select({
        "<script>",
        "SELECT * FROM post",
        "WHERE status = 1",
        "AND review_status = 1",
        "AND visibility = 'PUBLIC'",
        "<if test='authorIds != null and authorIds.size() > 0'>",
        "AND author_id IN",
        "<foreach collection='authorIds' item='authorId' open='(' separator=',' close=')'>#{authorId}</foreach>",
        "</if>",
        "<if test='cursorPublishedAt != null and cursorPostId != null'>",
        "AND (published_at &lt; #{cursorPublishedAt} OR (published_at = #{cursorPublishedAt} AND id &lt; #{cursorPostId}))",
        "</if>",
        "ORDER BY published_at DESC, id DESC",
        "LIMIT #{limit}",
        "</script>"
    })
    List<Post> selectRecentVisiblePostsByAuthors(
        @Param("authorIds") List<Long> authorIds,
        @Param("cursorPublishedAt") LocalDateTime cursorPublishedAt,
        @Param("cursorPostId") Long cursorPostId,
        @Param("limit") int limit
    );

    @Select({
        "<script>",
        "SELECT * FROM post",
        "WHERE status = 1",
        "AND review_status = 1",
        "AND visibility = 'PUBLIC'",
        "<if test='excludedAuthorIds != null and excludedAuthorIds.size() > 0'>",
        "AND author_id NOT IN",
        "<foreach collection='excludedAuthorIds' item='authorId' open='(' separator=',' close=')'>#{authorId}</foreach>",
        "</if>",
        "<if test='excludedPostIds != null and excludedPostIds.size() > 0'>",
        "AND id NOT IN",
        "<foreach collection='excludedPostIds' item='postId' open='(' separator=',' close=')'>#{postId}</foreach>",
        "</if>",
        "<if test='cursorPublishedAt != null and cursorPostId != null'>",
        "AND (published_at &lt; #{cursorPublishedAt} OR (published_at = #{cursorPublishedAt} AND id &lt; #{cursorPostId}))",
        "</if>",
        "ORDER BY published_at DESC, id DESC",
        "LIMIT #{limit}",
        "</script>"
    })
    List<Post> selectRecommendationCandidates(
        @Param("excludedAuthorIds") Set<Long> excludedAuthorIds,
        @Param("excludedPostIds") Set<Long> excludedPostIds,
        @Param("cursorPublishedAt") LocalDateTime cursorPublishedAt,
        @Param("cursorPostId") Long cursorPostId,
        @Param("limit") int limit
    );

    @Select({
        "<script>",
        "SELECT * FROM post",
        "WHERE status = 1",
        "AND review_status = 1",
        "AND visibility = 'PUBLIC'",
        "AND (",
        "<foreach collection='keywords' item='keyword' separator=' OR '>",
        "content_text LIKE CONCAT('%', #{keyword}, '%')",
        "</foreach>",
        ")",
        "<if test='cursorPublishedAt != null and cursorPostId != null'>",
        "AND (published_at &lt; #{cursorPublishedAt} OR (published_at = #{cursorPublishedAt} AND id &lt; #{cursorPostId}))",
        "</if>",
        "ORDER BY published_at DESC, id DESC",
        "LIMIT #{limit}",
        "</script>"
    })
    List<Post> selectLatestVisiblePostsByKeywords(
        @Param("keywords") List<String> keywords,
        @Param("cursorPublishedAt") LocalDateTime cursorPublishedAt,
        @Param("cursorPostId") Long cursorPostId,
        @Param("limit") int limit
    );

    @Select({
        "<script>",
        "SELECT COUNT(1) FROM post",
        "WHERE status = 1",
        "AND review_status = 1",
        "AND visibility = 'PUBLIC'",
        "AND (",
        "<foreach collection='keywords' item='keyword' separator=' OR '>",
        "content_text LIKE CONCAT('%', #{keyword}, '%')",
        "</foreach>",
        ")",
        "</script>"
    })
    Long countVisiblePostsByKeywords(@Param("keywords") List<String> keywords);
}
