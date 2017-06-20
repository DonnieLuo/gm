package com;

import com.repository.UserRepository;
import com.service.AuthenticationProviderImp;
import com.service.UserDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by Donnie on 2017/2/22.
 */
@Configuration
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private UserRepository userRepository;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailService userDetailService() {
        return new UserDetailService(userRepository);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests()
//                    .antMatchers("/test2", "/login", "/oauth/**","/gmtoken").authenticated()
                    .antMatchers("/test2").authenticated()
                    .anyRequest().permitAll()
                    .and()
                .formLogin()
                    .usernameParameter("username").passwordParameter("password")
                    .loginProcessingUrl("/login").loginPage("/login")
                    .and()
//                .logout().permitAll().logoutUrl("/logout").logoutSuccessUrl("/login")
//                    .logoutSuccessHandler(logoutSuccessHandler)
//                    .invalidateHttpSession(true).addLogoutHandler(logoutHandler).deleteCookies(new String[] { "cookie名字" })
                   .logout()
                        .permitAll()
                    .and()
                .rememberMe();


    }
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /**
         * 共四种：auth.jdbcAuthentication() auth.inMemoryAuthentication() auth.ldapAuthentication()
         * 以及auth.userDetailsService(userDetailService())
         */
        auth.userDetailsService(userDetailService());

//        auth.authenticationProvider(new JwtAuthenticationProvider(baseUserService, tokenService));
        auth.authenticationProvider(new AuthenticationProviderImp(userRepository,userDetailService()));

    }
}
