package cz.metacentrum.perun.spRegistration.web;

import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_" + ADMIN);
    public static final SimpleGrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_" + USER);

    public static final String[] ANGULAR_FILES = new String[] {
        "/fontawesome-webfont.*.*",
        "/*.*.js",
        "/main.*.js",
        "/polyfills.*.js",
        "/runtime.*.js",
        "/scripts.*.js",
        "/styles.*.css",
        "/assets/**",
        "/index.html",
        "/**.js",
        "/**.css",
    };

    private final PerunAdapter perunAdapter;
    private final AppBeansContainer appBeansContainer;

    @Autowired
    public SecurityConfiguration(PerunAdapter perunAdapter,
                                 AppBeansContainer appBeansContainer)
    {
        this.perunAdapter = perunAdapter;
        this.appBeansContainer = appBeansContainer;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/", "/login", "/error", "/notFound", "/api/config/**", "/api/getUser")
                .permitAll()
                .and()
            .authorizeRequests()
                .antMatchers(ANGULAR_FILES)
                .permitAll()
                .and()
            .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
            .oauth2Login(o -> {
                o.failureHandler(failureHandler());
                o.successHandler(successHandler(perunAdapter, appBeansContainer));
            })
            .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/");
    }
    @Autowired
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successHandler(
        PerunAdapter adapter, AppBeansContainer appBeansContainer
    ) {
        return new AuthSuccessHandler(adapter, appBeansContainer);
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler failureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/");
    }

}
