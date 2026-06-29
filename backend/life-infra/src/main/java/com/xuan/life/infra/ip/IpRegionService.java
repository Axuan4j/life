package com.xuan.life.infra.ip;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@Service
public class IpRegionService {

    private Searcher searcher;

    @PostConstruct
    public void init() {
        ClassPathResource resource = new ClassPathResource("ip2region/ip2region_v4.xdb");
        try (InputStream inputStream = resource.getInputStream()) {
            // ip2region 在服务启动时直接把 xdb 读入内存，查询时不依赖文件系统随机读，
            // 这样本地开发和容器部署都更稳定。
            this.searcher = Searcher.newWithBuffer(
                Version.IPv4,
                Searcher.loadContentFromInputStream(inputStream)
            );
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "IP 属地库初始化失败");
        }
    }

    public String resolveRegion(String ip) {
        if (!StringUtils.hasText(ip) || searcher == null) {
            return "未知";
        }
        try {
            String region = searcher.search(ip);
            return formatRegion(region);
        } catch (Exception exception) {
            return "未知";
        }
    }

    private String formatRegion(String region) {
        if (!StringUtils.hasText(region)) {
            return "未知";
        }
        String[] parts = region.split("\\|");
        if (parts.length < 5) {
            return sanitize(region);
        }

        String country = sanitize(parts[0]);
        String province = sanitize(parts[2]);
        String city = sanitize(parts[3]);

        if ("中国".equals(country)) {
            if (StringUtils.hasText(province)) {
                return province;
            }
            if (StringUtils.hasText(city)) {
                return city;
            }
            return country;
        }

        if (StringUtils.hasText(country)) {
            return country;
        }
        if (StringUtils.hasText(province)) {
            return province;
        }
        if (StringUtils.hasText(city)) {
            return city;
        }
        return "未知";
    }

    private String sanitize(String value) {
        if (!StringUtils.hasText(value) || "0".equals(value)) {
            return "";
        }
        return value.trim();
    }
}
