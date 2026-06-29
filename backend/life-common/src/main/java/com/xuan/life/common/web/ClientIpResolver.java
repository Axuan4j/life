package com.xuan.life.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class ClientIpResolver {

    private static final List<String> CANDIDATE_HEADERS = List.of(
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    );

    public String resolve(HttpServletRequest request) {
        for (String header : CANDIDATE_HEADERS) {
            String value = request.getHeader(header);
            if (!StringUtils.hasText(value) || "unknown".equalsIgnoreCase(value)) {
                continue;
            }
            String candidate = value.split(",")[0].trim();
            if (StringUtils.hasText(candidate)) {
                return normalizeLoopback(candidate);
            }
        }
        return normalizeLoopback(request.getRemoteAddr());
    }

    private String normalizeLoopback(String ip) {
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
