package com.epam.training.microservices.audio.resources.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocalizedMessageProvider {

    private final MessageSource messageSource;

    public String getMessage(String code , Object... arguments) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, arguments, locale);
    }
}