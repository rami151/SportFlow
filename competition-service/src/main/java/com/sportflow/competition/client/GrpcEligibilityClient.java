package com.sportflow.competition.client;

import com.sportflow.competition.exception.EligibilityServiceUnavailableException;
import com.sportflow.eligibility.grpc.EligibilityRequest;
import com.sportflow.eligibility.grpc.EligibilityResponse;
import com.sportflow.eligibility.grpc.EligibilityServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcEligibilityClient implements EligibilityClient {

    private static final Logger log = LoggerFactory.getLogger(GrpcEligibilityClient.class);

    @GrpcClient("eligibility-service")
    private EligibilityServiceGrpc.EligibilityServiceBlockingStub eligibilityStub;

    private final long timeoutSeconds;

    public GrpcEligibilityClient() {
        this.timeoutSeconds = 3;
    }

    @Autowired
    public GrpcEligibilityClient(@Value("${eligibility.grpc.timeout-seconds:3}") long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public GrpcEligibilityClient(EligibilityServiceGrpc.EligibilityServiceBlockingStub eligibilityStub, long timeoutSeconds) {
        this.eligibilityStub = eligibilityStub;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setEligibilityStub(EligibilityServiceGrpc.EligibilityServiceBlockingStub eligibilityStub) {
        this.eligibilityStub = eligibilityStub;
    }

    @Override
    public EligibilityCheckResult checkEligibility(UUID athleteId, UUID competitionId, int athleteAge,
                                                  double athleteWeight, String licenseNumber, String category) {
        log.info("Sending gRPC CheckEligibility request: athleteId={}, compId={}, age={}, weight={}, license={}, category={}",
                athleteId, competitionId, athleteAge, athleteWeight, licenseNumber, category);

        if (eligibilityStub == null) {
            throw new EligibilityServiceUnavailableException("Eligibility gRPC stub is not initialized", null);
        }

        EligibilityRequest request = EligibilityRequest.newBuilder()
                .setAthleteId(athleteId.toString())
                .setCompetitionId(competitionId.toString())
                .setAthleteAge(athleteAge)
                .setAthleteWeight(athleteWeight)
                .setLicenseNumber(licenseNumber)
                .setCategory(category)
                .build();

        try {
            EligibilityResponse response = eligibilityStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .checkEligibility(request);

            log.info("Received gRPC CheckEligibility response: eligible={}, reason='{}'",
                    response.getEligible(), response.getReason());

            return new EligibilityCheckResult(response.getEligible(), response.getReason());

        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            log.error("gRPC call to EligibilityService failed with code {}: {}", code, e.getMessage());

            if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED
                    || code == Status.Code.UNKNOWN || code == Status.Code.INTERNAL) {
                throw new EligibilityServiceUnavailableException(
                        "Eligibility service unavailable [" + code + "]: " + e.getMessage(),
                        e,
                        null
                );
            }
            throw e;
        }
    }
}
