package com.mefi.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        List<String> origins = List.of("http://i10d204.p.ssafy.io", "https://i10d204.p.ssafy.io");

        // CORS 설정을 모든 경로에 대해서 적용
        corsRegistry.addMapping("/**")
                // 오리진(도메인)에서 온 요청을 허용
                .allowedOrigins(String.join(",", origins))
                .allowedMethods("*");
    }
}