package ru.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.collector.service.CollectorService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

@RequiredArgsConstructor
@GrpcService
@Slf4j
public class CollectorControllerGrpc extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final CollectorService collectorService;

    public void collectUserAction(UserActionProto userActionProto, StreamObserver<Empty> responseObserver) {
        String logMsg = String.format("collect userId=%s, eventId=%s, actionType=%s, ts=%d",
                userActionProto.getUserId(),
                userActionProto.getEventId(),
                userActionProto.getActionType(),
                userActionProto.getTimestamp().getSeconds());

        try {
            log.info("start {}", logMsg);
            collectorService.collectUserAction(userActionProto);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("exception {}", logMsg);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }

        log.info("end {}", logMsg);
    }
}
