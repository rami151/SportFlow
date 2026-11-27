package com.sportflow.competition.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!soap")
public class MockFederationClient implements FederationClient {
    private static final Logger log = LoggerFactory.getLogger(MockFederationClient.class);

    @Override
    public boolean verifyLicense(String licenseNumber) {
        log.info("[MOCK] Verifying license: {}", licenseNumber);
        if (licenseNumber == null || licenseNumber.isBlank()) {
            return false;
        }
        return !licenseNumber.toUpperCase().startsWith("EXPIRED") && !licenseNumber.toUpperCase().startsWith("INVALID");
    }
}
