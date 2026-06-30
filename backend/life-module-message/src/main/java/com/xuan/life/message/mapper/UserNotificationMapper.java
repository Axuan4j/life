package com.xuan.life.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.life.message.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {

    @Update({
        "UPDATE user_notification",
        "SET is_read = 1, read_at = #{readAt}",
        "WHERE receiver_user_id = #{receiverUserId}",
        "AND is_read = 0"
    })
    int markAllAsRead(@Param("receiverUserId") Long receiverUserId, @Param("readAt") LocalDateTime readAt);
}
