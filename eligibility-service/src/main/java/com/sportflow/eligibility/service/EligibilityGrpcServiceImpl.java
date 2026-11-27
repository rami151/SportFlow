package com.sportflow.eligibility.service;

import com.sportflow.eligibility.domain.EligibilityRules;
import com.sportflow.eligibility.domain.EvaluationResult;
import com.sportflow.eligibility.grpc.EligibilityRequest;
import com.sportflow.eligibility.grpc.EligibilityResponse;
import com.sportflow.eligibility.grpc.EligibilityServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin gRPC service layer mapping gRPC requests to pure domain rules and returning responses.
 */
@GrpcService
public class EligibilityGrpcServiceImpl extends EligibilityServiceGrpc.EligibilityServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(EligibilityGrpcServiceImpl.class);

    private final EligibilityRules eligibilityRules;

    public EligibilityGrpcServiceImpl(EligibilityRules eligibilityRules) {
        this.eligibilityRules = eligibilityRules;
    }

    @Override
    public void checkEligibility(EligibilityRequest request, StreamObserver<EligibilityResponse> responseObserver) {
        log.info("Received eligibility check request: athleteId='{}', competitionId='{}', licenseNumber='{}', age={}, weight={}, category='{}'",
                request.getAthleteId(), request.getCompetitionId(), request.getLicenseNumber(),
                request.getAthleteAge(), request.getAthleteWeight(), request.getCategory());

        EvaluationResult result = eligibilityRules.evaluate(
                request.getAthleteAge(),
                request.getAthleteWeight(),
                request.getLicenseNumber(),
                request.getCategory()
        );

        log.info("Eligibility result for athlete '{}': eligible={}, reason='{}'",
                request.getAthleteId(), result.eligible(), result.reason());

        EligibilityResponse response = EligibilityResponse.newBuilder()
                .setEligible(result.eligible())
                .setReason(result.reason())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
