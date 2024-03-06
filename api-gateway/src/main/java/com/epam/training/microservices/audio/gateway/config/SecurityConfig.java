package com.epam.training.microservices.audio.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Autowired
	private Environment env;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http
				.authorizeExchange()
				//ALLOWING REGISTER API FOR DIRECT ACCESS
				.pathMatchers("/resources/**").authenticated()
				//ALL OTHER APIS ARE AUTHENTICATED
				.anyExchange().permitAll()
				.and()
				.csrf().disable()
				.oauth2Login()
				.and()
				.oauth2ResourceServer()
				.jwt();
		return http.build();
	}

	@Bean
	public ReactiveJwtDecoder jwtDecoder() {
		return ReactiveJwtDecoders.fromIssuerLocation(
				env.getProperty("spring.security.oauth2.client.provider.my-keycloak-provider.issuer-uri"));
	}

}