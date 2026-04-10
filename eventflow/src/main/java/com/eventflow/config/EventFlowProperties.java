package com.eventflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventflow")
@Data
public class EventFlowProperties {

    private Ai ai = new Ai();
    private Admin admin = new Admin();
    private Pagination pagination = new Pagination();

    @Data
    public static class Ai {
        private boolean enabled = true;
        private String systemPrompt;
    }

    @Data
    public static class Admin {
        private String defaultUsername = "admin";
        private String defaultPassword = "Admin@1234";
    }

    @Data
    public static class Pagination {
        private int defaultPageSize = 12;
    }
}
