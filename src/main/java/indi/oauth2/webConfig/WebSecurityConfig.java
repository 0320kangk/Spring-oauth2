package indi.oauth2.webConfig;

import indi.oauth2.user.service.CustomOauth2UserService;
import indi.oauth2.user.entity.enumurate.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig  {
    private final CustomOauth2UserService customOauth2UserService;

    //정적 리소스 검증 x
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf -> csrf.disable())

                .authorizeHttpRequests( request -> request.requestMatchers("/")
                        .permitAll()
                        .requestMatchers( "/api/v1/**")
                        .hasRole(Role.GUEST.name())//앞에 ROLE_ 이 붙음
                );
        http.logout( logout ->{
            logout.logoutUrl("/logout")
                    .logoutSuccessHandler( ((request, response, authentication) -> {
                        log.info("로그아웃 성공");
                        response.sendRedirect("/");
                    }));
        });

        http.oauth2Login( oauth -> {
            oauth.userInfoEndpoint(userInfoEndpointConfig -> {
                userInfoEndpointConfig.userService(customOauth2UserService);
            });
        });
        http.exceptionHandling( httpSecurityExceptionHandlingConfigurer ->
              httpSecurityExceptionHandlingConfigurer.accessDeniedHandler( (req,res, exception) ->{
                  log.info(req.getRequestURI());
                  log.info("에러 내용: {}", exception.getMessage());
              }));

        return http.build();
    }
    @Bean
    public static BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
