package com.sportflow.competition.client;

import com.sportflow.competition.exception.EligibilityServiceUnavailableException;
import com.sportflow.eligibility.grpc.EligibilityRequest;
import com.sportflow.eligibility.grpc.EligibilityResponse;
import com.sportflow.eligibility.grpc.EligibilityServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompetitionGrpcEligibilityIntegrationTest {

    private final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private ManagedChannel channel;
    private GrpcEligibilityClient client;
    private FakeEligibilityService fakeService;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        fakeService = new FakeEligibilityService();

        grpcCleanup.register(
                InProcessServerBuilder.forName(serverName)
                        .directExecutor()
                        .addService(fakeService)
                        .build()
                        .start()
        );

        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName)
                        .directExecutor()
                        .build()
        );

        EligibilityServiceGrpc.EligibilityServiceBlockingStub stub =
                EligibilityServiceGrpc.newBlockingStub(channel);

        client = new GrpcEligibilityClient(stub, 3);
    }

    @AfterEach
    void tearDown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    @Test
    @DisplayName("gRPC client receives eligible response")
    void checkEligibility_Eligible() {
        fakeService.nextResponse = EligibilityResponse.newBuilder()
                .setEligible(true)
                .setReason("OK")
                .build();

        EligibilityCheckResult result = client.checkEligibility(
                UUID.randomUUID(), UUID.randomUUID(), 24, 73.0, "LIC-12345", "SENIOR"
        );

        assertThat(result.eligible()).isTrue();
        assertThat(result.reason()).isEqualTo("OK");
    }

    @Test
    @DisplayName("gRPC client receives ineligible response with reason")
    void checkEligibility_Ineligible() {
        fakeService.nextResponse = EligibilityResponse.newBuilder()
                .setEligible(false)
                .setReason("AGE_OUT_OF_RANGE")
                .build();

        EligibilityCheckResult result = client.checkEligibility(
                UUID.randomUUID(), UUID.randomUUID(), 90, 73.0, "LIC-12345", "SENIOR"
        );

        assertThat(result.eligible()).isFalse();
        assertThat(result.reason()).isEqualTo("AGE_OUT_OF_RANGE");
    }

    @Test
    @DisplayName("gRPC client translates UNAVAILABLE error into EligibilityServiceUnavailableException")
    void checkEligibility_ServiceUnavailable() {
        fakeService.statusError = Status.UNAVAILABLE.withDescription("Service offline");

        assertThatThrownBy(() -> client.checkEligibility(
                UUID.randomUUID(), UUID.randomUUID(), 24, 73.0, "LIC-12345", "SENIOR"
        )).isInstanceOf(EligibilityServiceUnavailableException.class)
          .hasMessageContaining("UNAVAILABLE");
    }

    @Test
    @DisplayName("gRPC client translates DEADLINE_EXCEEDED error into EligibilityServiceUnavailableException")
    void checkEligibility_DeadlineExceeded() {
        fakeService.statusError = Status.DEADLINE_EXCEEDED.withDescription("Timeout waiting for response");

        assertThatThrownBy(() -> client.checkEligibility(
                UUID.randomUUID(), UUID.randomUUID(), 24, 73.0, "LIC-12345", "SENIOR"
        )).isInstanceOf(EligibilityServiceUnavailableException.class)
          .hasMessageContaining("DEADLINE_EXCEEDED");
    }

    private static class FakeEligibilityService extends EligibilityServiceGrpc.EligibilityServiceImplBase {
        EligibilityResponse nextResponse;
        Status statusError;

        @Override
        public void checkEligibility(EligibilityRequest request, StreamObserver<EligibilityResponse> responseObserver) {
            if (statusError != null) {
                responseObserver.onError(statusError.asRuntimeException());
                return;
            }
            responseObserver.onNext(nextResponse);
            responseObserver.onCompleted();
        }
    }
}
