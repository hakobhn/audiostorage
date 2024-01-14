package com.epam.training.microservices.audio.songs.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class BeanConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource bean = new ReloadableResourceBundleMessageSource();
        bean.addBasenames("classpath:org.hibernate.validator.ValidationMessages",
                "classpath:message");
        bean.setDefaultEncoding("UTF-8");
        return bean;
    }

}
