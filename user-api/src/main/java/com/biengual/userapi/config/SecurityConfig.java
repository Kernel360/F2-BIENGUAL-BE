package com.biengual.userapi.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;

import com.biengual.userapi.filter.TokenAuthenticationFilter;
import com.biengual.userapi.oauth2.handler.OAuth2FailureHandler;
import com.biengual.userapi.oauth2.handler.OAuth2SuccessHandler;
import com.biengual.userapi.oauth2.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.biengual.userapi.oauth2.service.OAuth2UserCustomService;
import com.biengual.userapi.token.service.TokenProvider;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final AccessDeniedHandler accessDeniedHandler;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .httpBasic(HttpBasicConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        ;

        http
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
            .addFilterBefore(tokenAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> {
                authorize
                    // Can access form ANONYMOUS
                    .requestMatchers(HttpMethod.GET, "/api/contents/view/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/contents/search/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/contents/accurate-search/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/contents/preview/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/all").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/type/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/contents/details/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/user/status").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/recommender/bookmark").permitAll()

                    // Can access from USER
                    .requestMatchers("/api/bookmark/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/user/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/contents/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/questions/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/categories/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/scrap/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/missions/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/mission-history/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/learning/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/dashboard/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/recommender/**").hasAnyRole("USER", "DEVELOPER")
                    .requestMatchers("/api/point/**").hasAnyRole("USER", "DEVELOPER")

                    // Can access from ADMIN
                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "DEVELOPER")
                    .requestMatchers("/admin/**").hasAnyRole("ADMIN", "DEVELOPER")

                    /**
                     * Need To Activate DEVELOPER on DEPLOY SETTING
                     */
                    // Can access only DEVELOPER
                    .requestMatchers("/api/token").hasRole("DEVELOPER")
                    .requestMatchers("/swagger-ui/**").hasRole("DEVELOPER")
                    .requestMatchers("/api-info/**").hasRole("DEVELOPER")
                    .requestMatchers("/api/s3-test/**").hasRole("DEVELOPER")

                    // Can access form Authenticated
                    .requestMatchers(HttpMethod.POST, "/api/user/logout").authenticated()

                    .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                    .anyRequest().permitAll();
            });

        http
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                    .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository)
                )
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                    .userService(oAuth2UserCustomService)
                )
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)

            );
        http
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            );

        http.exceptionHandling(configurer -> configurer
            .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
            corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));

            List<String> originPatterns = new ArrayList<>();

            if ("local".equals(activeProfile)) {
                originPatterns.add("http://localhost:3000");
            }

            if ("dev".equals(activeProfile)) {
                originPatterns.add("https://local.biengual.store:3000");
                originPatterns.add("https://dev.biengual.store");
                originPatterns.add("http://54.66.49.200:8080");
            }

            if ("prod".equals(activeProfile)) {
                originPatterns.add("https://biengual.store");
                originPatterns.add("https://www.biengual.store");
                originPatterns.add("https://api.biengual.store");
            }

            corsConfiguration.setAllowedOriginPatterns(originPatterns);
            corsConfiguration.setAllowCredentials(true);
            return corsConfiguration;
        };

    }
}