package com.epam.training.microservices.audio.resources.config;

import com.epam.training.microservices.audio.resources.component.RequestTraceFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import static com.epam.training.microservices.audio.resources.controller.ControllerEndpoints.RESOURCES_URL;

@Configuration
@RequiredArgsConstructor
public class BeanConfig {

    private final RequestTraceFilter traceFilter;

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource bean = new ReloadableResourceBundleMessageSource();
        bean.addBasenames("classpath:org.hibernate.validator.ValidationMessages",
                "classpath:message");
        bean.setDefaultEncoding("UTF-8");
        return bean;
    }

    @Bean
    public FilterRegistrationBean dawsonApiFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(traceFilter);
        registration.addUrlPatterns(RESOURCES_URL + "/*");
        registration.setOrder(2);
        return registration;
    }

}
