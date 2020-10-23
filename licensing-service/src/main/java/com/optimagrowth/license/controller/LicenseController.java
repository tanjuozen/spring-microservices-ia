package com.optimagrowth.license.controller;

import com.optimagrowth.license.model.License;
import com.optimagrowth.license.service.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping(value = "v1/organization/{organizationId}/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @RequestMapping(value = "/{licenseId}", method = RequestMethod.GET)
    public ResponseEntity<License> getLicense(@PathVariable("organizationId") String organizationId,
                                              @PathVariable("licenseId") String licenseId) {
        License license = licenseService.getLicense(licenseId, organizationId);
        license.add(
                linkTo(methodOn(LicenseController.class).getLicense(organizationId, license.getLicenseId())).withSelfRel(),
                linkTo(methodOn(LicenseController.class).createLicense(organizationId, license, null)).withRel("createLicense"),
                linkTo(methodOn(LicenseController.class).updateLicense(organizationId, license, null)).withRel("updateLicense"),
                linkTo(methodOn(LicenseController.class).deleteLicense(organizationId, license.getLicenseId(), null)).withRel("deleteLicense"));
        return ResponseEntity.ok(license);
    }

    @PostMapping
    public ResponseEntity<String> createLicense(@PathVariable("organizationId") String organizationId,
                                                @RequestBody License request, Locale locale) {
        return ResponseEntity.ok(licenseService.createLicense(request, organizationId, locale));
    }

    @PutMapping
    public ResponseEntity<String> updateLicense(@PathVariable("organizationId") String organizationId,
                                                @RequestBody License request, Locale locale) {
        return ResponseEntity.ok(licenseService.updateLicense(request, organizationId, locale));
    }

    @DeleteMapping(value = "/{licenseId}")
    public ResponseEntity<String> deleteLicense(@PathVariable("organizationId") String organizationId,
                                                @PathVariable("licenseId") String licenseId, Locale locale) {
        return ResponseEntity.ok(licenseService.deleteLicense(licenseId, organizationId, locale));
    }
}