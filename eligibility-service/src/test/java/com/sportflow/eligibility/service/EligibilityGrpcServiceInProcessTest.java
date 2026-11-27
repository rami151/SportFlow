package com.sportflow.eligibility.service;

import com.sportflow.eligibility.config.EligibilityProperties;
import com.sportflow.eligibility.domain.EligibilityRules;
import com.sportflow.eligibility.grpc.EligibilityRequest;
import com.sportflow.eligibility.grpc.EligibilityResponse;
import com.sportflow.eligibility.grpc.EligibilityServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityGrpcServiceInProcessTest {

    private final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private ManagedChannel channel;
    private EligibilityServiceGrpc.EligibilityServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        EligibilityProperties properties = new EligibilityProperties();
        EligibilityRules eligibilityRules = new EligibilityRules(properties);
        EligibilityGrpcServiceImpl service = new EligibilityGrpcServiceImpl(eligibilityRules);

        // Register in-process server with reflection service
        grpcCleanup.register(
                InProcessServerBuilder.forName(serverName)
                        .directExecutor()
                        .addService(service)
                        .addService(ProtoReflectionService.newInstance())
                        .build()
                        .start()
        );

        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName)
                        .directExecutor()
                        .build()
        );

        blockingStub = EligibilityServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    @Test
    @DisplayName("In-process gRPC call: valid athlete returns eligible=true, reason=OK")
    void checkEligibilityValid() {
        EligibilityRequest request = EligibilityRequest.newBuilder()
                .setAthleteId("ath-100")
                .setCompetitionId("comp-200")
                .setAthleteAge(25)
                .setAthleteWeight(72.5)
                .setLicenseNumber("LIC-VALID-123")
                .setCategory("SENIOR")
                .build();

        EligibilityResponse response = blockingStub.checkEligibility(request);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isTrue();
        assertThat(response.getReason()).isEqualTo("OK");
    }

    @Test
    @DisplayName("In-process gRPC call: blank license returns eligible=false, reason=LICENSE_INVALID")
    void checkEligibilityBlankLicense() {
        EligibilityRequest request = EligibilityRequest.newBuilder()
                .setAthleteId("ath-101")
                .setCompetitionId("comp-200")
                .setAthleteAge(25)
                .setAthleteWeight(72.5)
                .setLicenseNumber("   ")
                .setCategory("SENIOR")
                .build();

        EligibilityResponse response = blockingStub.checkEligibility(request);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isFalse();
        assertThat(response.getReason()).isEqualTo("LICENSE_INVALID");
    }

    @Test
    @DisplayName("In-process gRPC call: age out of range returns eligible=false, reason=AGE_OUT_OF_RANGE")
    void checkEligibilityAgeOutOfRange() {
        EligibilityRequest request = EligibilityRequest.newBuilder()
                .setAthleteId("ath-102")
                .setCompetitionId("comp-200")
                .setAthleteAge(95)
                .setAthleteWeight(72.5)
                .setLicenseNumber("LIC-VALID-123")
                .setCategory("VETERAN")
                .build();

        EligibilityResponse response = blockingStub.checkEligibility(request);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isFalse();
        assertThat(response.getReason()).isEqualTo("AGE_OUT_OF_RANGE");
    }

    @Test
    @DisplayName("In-process gRPC call: category mismatch returns eligible=false, reason=CATEGORY_MISMATCH")
    void checkEligibilityCategoryMismatch() {
        EligibilityRequest request = EligibilityRequest.newBuilder()
                .setAthleteId("ath-103")
                .setCompetitionId("comp-200")
                .setAthleteAge(16)
                .setAthleteWeight(60.0)
                .setLicenseNumber("LIC-VALID-123")
                .setCategory("SENIOR")
                .build();

        EligibilityResponse response = blockingStub.checkEligibility(request);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isFalse();
        assertThat(response.getReason()).isEqualTo("CATEGORY_MISMATCH");
    }
}
