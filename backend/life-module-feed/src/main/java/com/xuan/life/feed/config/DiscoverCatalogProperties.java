package com.xuan.life.feed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "life.discover")
public class DiscoverCatalogProperties {

    private List<HotKeyword> hotKeywords = new ArrayList<>();
    private List<Topic> topics = new ArrayList<>();
    private List<RecommendedAuthor> recommendedAuthors = new ArrayList<>();

    public List<HotKeyword> getHotKeywords() {
        return hotKeywords;
    }

    public void setHotKeywords(List<HotKeyword> hotKeywords) {
        this.hotKeywords = hotKeywords;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public List<RecommendedAuthor> getRecommendedAuthors() {
        return recommendedAuthors;
    }

    public void setRecommendedAuthors(List<RecommendedAuthor> recommendedAuthors) {
        this.recommendedAuthors = recommendedAuthors;
    }

    public static class HotKeyword {
        private String keyword;
        private String title;
        private String trendLabel;
        private String heatLabel;
        private List<String> matchKeywords = new ArrayList<>();

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTrendLabel() {
            return trendLabel;
        }

        public void setTrendLabel(String trendLabel) {
            this.trendLabel = trendLabel;
        }

        public String getHeatLabel() {
            return heatLabel;
        }

        public void setHeatLabel(String heatLabel) {
            this.heatLabel = heatLabel;
        }

        public List<String> getMatchKeywords() {
            return matchKeywords;
        }

        public void setMatchKeywords(List<String> matchKeywords) {
            this.matchKeywords = matchKeywords;
        }
    }

    public static class Topic {
        private String topicKey;
        private String title;
        private String summary;
        private String coverStyle;
        private List<String> matchKeywords = new ArrayList<>();

        public String getTopicKey() {
            return topicKey;
        }

        public void setTopicKey(String topicKey) {
            this.topicKey = topicKey;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getCoverStyle() {
            return coverStyle;
        }

        public void setCoverStyle(String coverStyle) {
            this.coverStyle = coverStyle;
        }

        public List<String> getMatchKeywords() {
            return matchKeywords;
        }

        public void setMatchKeywords(List<String> matchKeywords) {
            this.matchKeywords = matchKeywords;
        }
    }

    public static class RecommendedAuthor {
        private String username;
        private String reason;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
