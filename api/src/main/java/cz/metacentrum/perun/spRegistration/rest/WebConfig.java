package cz.metacentrum.perun.spRegistration.rest;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.rest.interceptors.UserSettingInterceptor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
@Setter
public class WebConfig implements WebMvcConfigurer {

    @Value("${dev.enabled}")
    public boolean devEnabled;

    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final ApplicationProperties applicationProperties;

    @Autowired
    public WebConfig(@NonNull PerunAdapter perunAdapter,
                     @NonNull AttributesProperties attributesProperties,
                     @NonNull ApplicationProperties applicationProperties)
    {
        this.perunAdapter = perunAdapter;
        this.attributesProperties = attributesProperties;
        this.applicationProperties = applicationProperties;
    }

    @Bean
    @Autowired
    public UserSettingInterceptor userSettingInterceptor(@NonNull PerunAdapter perunAdapter,
                                                         @NonNull AttributesProperties attributesProperties,
                                                         @NonNull ApplicationProperties applicationProperties)
    {
        return new UserSettingInterceptor(perunAdapter, attributesProperties, applicationProperties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String path = (devEnabled ? "" : "/auth" ) + "/**";
        registry.addInterceptor(userSettingInterceptor(perunAdapter, attributesProperties, applicationProperties))
                .addPathPatterns(path)
                .excludePathPatterns("/api/config/**");
    }

}
