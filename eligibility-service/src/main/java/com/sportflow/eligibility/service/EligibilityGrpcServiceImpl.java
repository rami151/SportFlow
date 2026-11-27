package com.sportflow.eligibility.service;

import com.sportflow.eligibility.grpc.EligibilityRequest;
import com.sportflow.eligibility.grpc.EligibilityResponse;
import com.sportflow.eligibility.grpc.EligibilityServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class EligibilityGrpcServiceImpl extends EligibilityServiceGrpc.EligibilityServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(EligibilityGrpcServiceImpl.class);

    @Override
    public void checkEligibility(EligibilityRequest request, StreamObserver<EligibilityResponse> responseObserver) {
        log.info("Received eligibility check request for athlete: {} in competition: {}",
                request.getAthleteId(), request.getCompetitionId());

        // Basic skeleton response; detailed business rules will be implemented in task B3
        EligibilityResponse response = EligibilityResponse.newBuilder()
                .setEligible(true)
                .setReason("OK")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
