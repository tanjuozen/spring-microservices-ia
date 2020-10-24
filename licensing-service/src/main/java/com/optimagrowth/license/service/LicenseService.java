package com.optimagrowth.license.service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.repository.LicenseRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class LicenseService {

    private final LicenseRepository repository;
    private final ServiceConfig config;
    private final MessageSource messages;

    public LicenseService(LicenseRepository repository, ServiceConfig config, MessageSource messages) {
        this.repository = repository;
        this.config = config;
        this.messages = messages;
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
    public String deleteLicense(String licenseId){
        String responseMessage;
        License license = new License();
        license.setLicenseId(licenseId);
        repository.delete(license);
        responseMessage = String.format(messages.getMessage("license.delete.message", null, null),licenseId);
        return responseMessage;

    }
}