package com.xuan.life.admin.web.response;

public record AdminOverviewResponse(
    long userCount,
    long postCount,
    long todayPostCount,
    long followCount
) {
}
