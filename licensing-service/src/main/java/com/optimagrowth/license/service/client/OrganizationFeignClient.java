package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.ws.rs.core.MediaType;

@FeignClient("organization-service")
public interface OrganizationFeignClient {
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/v1/organization/{organizationId}",
            consumes = MediaType.APPLICATION_JSON)
    Organization getOrganization(@PathVariable("organizationId") String organizationId);
}
