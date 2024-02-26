package com.epam.training.microservices.audio.songs.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;

import static com.epam.training.microservices.audio.songs.component.TracingConstants.CURRENT_TRACE_ID_HEADER;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Tracer {
    @Autowired
    private HttpServletRequest httpServletRequest;

    public String traceId() {
        return httpServletRequest.getHeader(CURRENT_TRACE_ID_HEADER);
    }
}