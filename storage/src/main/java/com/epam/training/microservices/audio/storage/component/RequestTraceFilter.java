package com.epam.training.microservices.audio.storage.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.epam.training.microservices.audio.storage.component.TracingConstants.TRACE_ID;


@Slf4j
@Component
@RequiredArgsConstructor
public class RequestTraceFilter implements Filter {

    private final Tracer tracer;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        log.debug("Request path {}", httpServletRequest.getRequestURI());
        log.debug("Request method {}", httpServletRequest.getMethod());
        String traceId = tracer.traceId();
        log.info("Trace id header: {}", traceId);
        MDC.put(TRACE_ID, traceId);
        chain.doFilter(request, response);
        MDC.remove(TRACE_ID);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}