package com.contactmanager.contactmanager.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService getUserDetailsService(){
        return new UserDetailsServiceImpl() ;
    }
    // Password Encoding
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(this.getUserDetailsService());
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public ProviderManager authManagerBean(HttpSecurity security) throws Exception {
        return (ProviderManager) security.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider()).
                build();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // It is recommended to secure your application at the API endpoint level.
                .authorizeHttpRequests(authorizeHttpRequests -> {
                    try {
                        authorizeHttpRequests
                                .requestMatchers("/user/**")
                                .authenticated()
                                .anyRequest()
                                .permitAll()
                                .and()
                                .formLogin(form -> form
                                .loginPage("/signIn")
                                .loginProcessingUrl("/dologin")
                                .defaultSuccessUrl("/user/user_dashboard", true)
                                // .failureUrl("/login-fail")
                                )
                                ;
                                
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                )
                .headers(headers -> headers.disable()) // This is for H2 browser console access.
                .csrf(csrf -> csrf.disable())
                .build();
    }



}
