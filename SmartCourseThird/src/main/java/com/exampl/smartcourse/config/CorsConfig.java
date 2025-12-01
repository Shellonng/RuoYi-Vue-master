package com.exampl.smartcourse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    @Autowired
    private Environment env;

    @Bean
    public CorsFilter corsFilter() {
        boolean enabled = Boolean.parseBoolean(env.getProperty("smartcourse.cors.enabled", "true"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        if (enabled) {
            // 从配置文件读取允许的源（支持通配符，解决allowCredentials=true时*的兼容性问题）
            List<String> originPatterns = parseList(env.getProperty("smartcourse.cors.allowed-origins", "https://*.yourdomain.com,http://localhost:8080,http://localhost:5173"));
            // 补充PATCH方法（对齐方法二）
            List<String> methods = parseList(env.getProperty("smartcourse.cors.allowed-methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH"));
            // 重要：allowCredentials=true 时不能使用通配符 *，必须明确指定 headers
            List<String> headers = parseList(env.getProperty("smartcourse.cors.allowed-headers", "Content-Type,Authorization,userId,X-Requested-With"));
            // 默认为true（对齐方法二的allowCredentials(true)）
            boolean credentials = Boolean.parseBoolean(env.getProperty("smartcourse.cors.allow-credentials", "true"));
            long maxAge = Long.parseLong(env.getProperty("smartcourse.cors.max-age", "3600"));

            config.setAllowedOriginPatterns(originPatterns); // 注意用allowedOriginPatterns而非allowedOrigins
            config.setAllowedMethods(methods);
            config.setAllowedHeaders(headers);
            config.setAllowCredentials(credentials);
            config.setMaxAge(maxAge);
        }

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public WebMvcConfigurer staticResourceConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String mode = env.getProperty("smartcourse.file.storage.mode", "local");
                String localBase = env.getProperty("smartcourse.file.storage.local-base", "");
                String serverBase = env.getProperty("smartcourse.file.storage.server-base", "");
                String base = "server".equalsIgnoreCase(mode) ? serverBase : localBase;
                if (base != null && !base.isEmpty()) {
                    String normalized = base.replace('\\', '/');
                    if (!normalized.endsWith("/")) normalized += "/";
                    String coursesLocation = "file:" + normalized + "courses/";
                    registry.addResourceHandler("/courses/**").addResourceLocations(coursesLocation);
                }

                String uploadsBase = env.getProperty("file.upload.path", "");
                if (uploadsBase != null && !uploadsBase.isEmpty()) {
                    String normalizedUploads = uploadsBase.replace('\\', '/');
                    if (!normalizedUploads.endsWith("/")) normalizedUploads += "/";
                    String uploadsLocation = "file:" + normalizedUploads;
                    registry.addResourceHandler("/uploads/**").addResourceLocations(uploadsLocation);
                }
            }
        };
    }

    // 工具方法：解析逗号分隔的字符串为List
    private List<String> parseList(String s) {
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toList());
    }
}
