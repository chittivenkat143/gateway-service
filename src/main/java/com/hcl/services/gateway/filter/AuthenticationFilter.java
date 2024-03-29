package com.hcl.services.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.hcl.services.gateway.utils.JwtUtil;
import com.hcl.services.gateway.validator.RouterValidator;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {
	private Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	@Autowired
	private RouterValidator routerValidator;

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		logger.info("AF: filter\t" + exchange);
		ServerHttpRequest request = exchange.getRequest();
		logger.info("AF: filter Request\t" + request);
		if (routerValidator.isSecured.test(request)) {
			if (this.isAuthMissing(request)) {
				logger.error("Authorization header is missing in request");
				return this.onError(exchange, "Authorization header is missing in request", HttpStatus.UNAUTHORIZED);
			}

			final String token = this.getAuthHeader(request).substring(7);

			if (jwtUtil.isInvalid(token)) {
				logger.error("Authorization header is invalid");
				return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);
			}

			this.populateRequestWithHeaders(exchange, token);
		}
		return chain.filter(exchange);
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		logger.error("onError" + err + "\n HttpStatus\t" + httpStatus);
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}

	private String getAuthHeader(ServerHttpRequest request) {
		return request.getHeaders().getOrEmpty("Authorization").get(0);
	}

	private boolean isAuthMissing(ServerHttpRequest request) {
		return !request.getHeaders().containsKey("Authorization");
	}

	private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
		logger.info("AF: populateRequestWithHeaders");
		Claims claims = jwtUtil.getAllClaimsFromToken(token);
		logger.info("AF: populateRequestWithHeaders:\t" + claims);
		exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id")))
				.header("role", String.valueOf(claims.get("role"))).build();
	}

}
