package ru.practicum.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Slf4j
@Component
public class UserActionClientGrpc {
    private final UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public UserActionClientGrpc(@GrpcClient("collector") UserActionControllerGrpc.UserActionControllerBlockingStub client) {
        this.client = client;
    }

    public void collectUserAction(long userId,
                                  long eventId,
                                  ActionTypeProto actionTypeProto,
                                  Timestamp timestamp) {
        UserActionProto userActionProto = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionTypeProto)
                .setTimestamp(timestamp)
                .build();

        try {
            client.collectUserAction(userActionProto);
        } catch (Exception e) {
            log.warn("Неудачный вызов collectUserAction", e);
        }
    }
}
