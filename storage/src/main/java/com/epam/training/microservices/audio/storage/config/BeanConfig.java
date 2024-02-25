package com.epam.training.microservices.audio.storage.config;

import com.epam.training.microservices.audio.storage.component.RequestTraceFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BeanConfig {

    private final RequestTraceFilter traceFilter;

    @Bean
    public FilterRegistrationBean dawsonApiFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(traceFilter);
        registration.setOrder(2);
        return registration;
    }

}
