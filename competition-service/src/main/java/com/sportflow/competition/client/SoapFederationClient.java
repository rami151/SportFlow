package com.sportflow.competition.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("soap")
public class SoapFederationClient implements FederationClient {
    private static final Logger log = LoggerFactory.getLogger(SoapFederationClient.class);

    @Override
    public boolean verifyLicense(String licenseNumber) {
        log.info("[SOAP] Real SOAP verification for license: {}", licenseNumber);
        throw new UnsupportedOperationException("SoapFederationClient pending live Federation Service");
    }
}
