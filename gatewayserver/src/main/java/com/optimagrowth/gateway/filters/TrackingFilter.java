package com.optimagrowth.gateway.filters;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Order(1)
@Component
public class TrackingFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);


    private final FilterUtils filterUtils;

    public TrackingFilter(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        if (isCorrelationIdPresent(requestHeaders)) {
            logger.debug("tmx-correlation-id found in tracking filter: {}. ", filterUtils.getCorrelationId(requestHeaders));
        } else {
            String correlationID = generateCorrelationId();
            exchange = filterUtils.setCorrelationId(exchange, correlationID);
            logger.debug("tmx-correlation-id generated in tracking filter: {}.", correlationID);
        }
        logger.info("===> The authentication name from the token is {}", getAuthenticationName(requestHeaders));

        return chain.filter(exchange);
    }


    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
        return filterUtils.getCorrelationId(requestHeaders) != null;
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    private String getAuthenticationName(HttpHeaders requestHeaders) {
        String authenticationName = "";
        if (filterUtils.getAuthToken(requestHeaders) != null) {
            String authToken = filterUtils.getAuthToken(requestHeaders);
            JSONObject jsonObject = decodeJWT(authToken);
            authenticationName = jsonObject.getString("authentication_name");
        }
        return authenticationName;
    }

    private JSONObject decodeJWT(String authToken) {
        String[] split_string = authToken.split("\\.");
        String base64EncodedBody = split_string[1];
        Base64 base64URL = new Base64(true);
        String body = new String(base64URL.decode(base64EncodedBody));
        return new JSONObject(body);
    }
}