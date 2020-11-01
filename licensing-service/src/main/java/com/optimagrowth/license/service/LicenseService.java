package com.optimagrowth.license.service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.service.client.OrganizationDiscoveryClient;
import com.optimagrowth.license.service.client.OrganizationFeignClient;
import com.optimagrowth.license.service.client.OrganizationRestTemplateClient;
import com.optimagrowth.license.util.UserContextHolder;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class LicenseService {

    private final LicenseRepository repository;
    private final ServiceConfig config;
    private final MessageSource messages;
    private final OrganizationDiscoveryClient organizationDiscoveryClient;
    private final OrganizationRestTemplateClient organizationRestClient;
    private final OrganizationFeignClient organizationFeignClient;

    public LicenseService(LicenseRepository repository, ServiceConfig config, MessageSource messages,
                          OrganizationDiscoveryClient organizationDiscoveryClient,
                          OrganizationRestTemplateClient organizationRestClient,
                          OrganizationFeignClient organizationFeignClient) {
        this.repository = repository;
        this.config = config;
        this.messages = messages;
        this.organizationDiscoveryClient = organizationDiscoveryClient;
        this.organizationRestClient = organizationRestClient;
        this.organizationFeignClient = organizationFeignClient;
    }


    public License getLicense(String licenseId, String organizationId) {
        License license = repository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (license == null) {
            throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message",
                    null, null), licenseId, organizationId));
        }
        return license.withComment(config.getProperty());
    }

    public License createLicense(License license) {
        license.setLicenseId(UUID.randomUUID().toString());
        License saved = repository.save(license);
        return saved.withComment(config.getProperty());
    }

    public License updateLicense(License license) {
        repository.save(license);
        return license.withComment(config.getProperty());
    }

    public String deleteLicense(String licenseId) {
        String responseMessage;
        License license = new License();
        license.setLicenseId(licenseId);
        repository.delete(license);
        responseMessage = String.format(messages.getMessage("license.delete.message", null, null), licenseId);
        return responseMessage;
    }

    public License getLicense(String organizationId, String licenseId, String clientType) {
        License license = repository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (null == license) {
            throw new IllegalArgumentException(String.format(
                    messages.getMessage("license.search.error.message", null, null),
                    licenseId, organizationId));
        }

        Organization organization = retrieveOrganizationInfo(organizationId, clientType);
        if (null != organization) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license.withComment(config.getProperty());
    }

    @CircuitBreaker(name = "licenseService", fallbackMethod = "buildFallbackLicenseList")
    @Retry(name = "retryLicenseService", fallbackMethod = "buildFallbackLicenseList")
    @Bulkhead(name = "bulkheadLicenseService",
            type = Bulkhead.Type.THREADPOOL,
            fallbackMethod = "buildFallbackLicenseList")
    public List<License> getLicensesByOrganization(String organizationId) throws TimeoutException {
        log.debug("getLicensesByOrganization Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
        randomlyRunLong();
        return repository.findByOrganizationId(organizationId);
    }


    private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
        Organization organization = null;

        switch (clientType) {
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = getOrganization(organizationId);
                break;
        }

        return organization;
    }

    @CircuitBreaker(name = "organizationService")
    private Organization getOrganization(String organizationId) {
        return organizationRestClient.getOrganization(organizationId);
    }

    private List<License> buildFallbackLicenseList(String organizationId, Throwable t) {
        List<License> fallbackList = new ArrayList<>();
        License license = new License();
        license.setLicenseId("0000000-00-00000");
        license.setOrganizationId(organizationId);
        license.setProductName("Sorry no licensing information currently available");
        fallbackList.add(license);
        return fallbackList;
    }

    private void randomlyRunLong() throws TimeoutException {
        Random rand = new Random();
        int randomNum = rand.nextInt((3 - 1) + 1) + 1;
        if (randomNum == 3) sleep();
    }

    private void sleep() throws TimeoutException {
        try {
            Thread.sleep(5000);
            throw new java.util.concurrent.TimeoutException();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}