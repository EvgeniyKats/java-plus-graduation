package ru.practicum.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.service.AnalyzerService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

@RequiredArgsConstructor
@GrpcService
@Slf4j
public class AnalyzerControllerGrpc extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final AnalyzerService analyzerService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        String eventForLog = String.format("req.userId=%s, req.max=%s",
                request.getUserId(),
                request.getMaxResults());

        try {
            log.info("start getRecommendationsForUser {}", eventForLog);
            analyzerService.getRecommendationsForUser(request).forEach(responseObserver::onNext);
            log.info("end getRecommendationsForUser {}", eventForLog);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("exception getRecommendationsForUser {}", eventForLog, e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        String eventForLog = String.format("req.eventId=%s, req.userId=%s, req.max=%s",
                request.getEventId(),
                request.getUserId(),
                request.getMaxResults());

        try {
            log.info("start getSimilarEvents {}", eventForLog);
            analyzerService.getSimilarEvents(request).forEach(responseObserver::onNext);
            log.info("end getSimilarEvents {}", eventForLog);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("exception getSimilarEvents {}", eventForLog, e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        String eventForLog = String.format("req.eventIds=%s", request.getEventIdList());

        try {
            log.info("start getInteractionsCount {}", eventForLog);
            analyzerService.getInteractionsCount(request).forEach(responseObserver::onNext);
            log.info("end getInteractionsCount {}", eventForLog);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("exception getInteractionsCount {}", eventForLog, e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
