package com.mefi.backend.config;

import com.mefi.backend.common.auth.JWTFilter;
import com.mefi.backend.common.auth.LoginFilter;
import com.mefi.backend.common.util.JWTUtil;
import com.mefi.backend.db.repository.TokenRepository;
import com.mefi.backend.db.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    // AuthenticationManager 등록
    // 데이터베이스에서 회원 정보를 가져와 검증 수행하는 클래스
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // BCryptPaasswordEncoder 등록
    // 비밀번호 인코딩(암호화) 기능을 구현한 클래스
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // csrf disable (CSRF 비활성화)
        http
                .csrf((auth) -> auth.disable());

        // form login disable (폼 로그인 비활성화)
        http
                .formLogin((auth) -> auth.disable());

        // http basic 인증 disable (기본 인증 비활성화)
        http
                .httpBasic((auth) -> auth.disable());

        // 경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth

                        // 작성된 경로 모든 사용자 접근 허용
                        .requestMatchers("/api/**", "/users", "/users/login", "/users/**").permitAll()
                        // Swagger 접근 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 이외 인증된 사용자만 접근 허용
                        .anyRequest().authenticated());

        // 필터 등록
        http
                // 해당 필터 이전에 수행 (수행 할 필터, 기준 필터)
                .addFilterBefore(new JWTFilter(jwtUtil,userRepository), LoginFilter.class)

                // 해당 필터 자리에서 수행 (수행 할 필터, 대체 필터 자리)
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration),jwtUtil,tokenRepository,userRepository), UsernamePasswordAuthenticationFilter.class);

        // 세션 설정 (JWT를 통한 인증/인가를 위해서 세션을 STATELESS 상태로 설정)
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Cors 설정 (Security Filter를 위한 Cors 설정)
        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        // 클라이언트에서 온 요청 중에서 허용할 도메인 설정
                        configuration.setAllowedOrigins(Collections.singletonList("https://i10d204.p.ssafy.io"));

                        // 모든 HTTP 메서드 허용 설정
                        configuration.setAllowedMethods(Collections.singletonList("*"));

                        // 자격 증명을 사용으로 설정
                        configuration.setAllowCredentials(true);

                        // 모든 HTTP 헤더 허용 설정
                        configuration.setAllowedHeaders(Collections.singletonList("*"));

                        // 허용할 시간을 1시간 설정
                        configuration.setMaxAge(3600L);

                        // 브라우저 응답으로 전달할 헤더 설정
                        configuration.setExposedHeaders(List.of("Authorization", "accessToken", "refreshToken"));
                        return configuration;
                    }
                })));

        return http.build();
    }
}