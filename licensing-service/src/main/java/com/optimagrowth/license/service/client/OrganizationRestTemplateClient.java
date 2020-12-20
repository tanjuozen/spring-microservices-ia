package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrganizationRestTemplateClient {

    private final OAuth2RestTemplate oAuth2RestTemplate;

    public OrganizationRestTemplateClient(OAuth2RestTemplate oAuth2RestTemplate) {
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }


    public Organization getOrganization(String organizationId) {
        ResponseEntity<Organization> restExchange =
                oAuth2RestTemplate.exchange(
                        "http://gateway-server:8072/organization/v1/organization/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);
        return restExchange.getBody();
    }
}