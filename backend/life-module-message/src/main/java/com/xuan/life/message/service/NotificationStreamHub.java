package com.xuan.life.message.service;

import com.xuan.life.message.web.response.NotificationStreamEventResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NotificationStreamHub {

    private static final long EMITTER_TIMEOUT = 0L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId, NotificationStreamEventResponse snapshotEvent) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        emitterMap.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(error -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event().data(snapshotEvent));
        } catch (IOException exception) {
            removeEmitter(userId, emitter);
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    public void emit(Long userId, NotificationStreamEventResponse event) {
        List<SseEmitter> emitters = emitterMap.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> brokenEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(event));
            } catch (IOException exception) {
                brokenEmitters.add(emitter);
            }
        }
        brokenEmitters.forEach(emitter -> removeEmitter(userId, emitter));
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emitterMap.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emitterMap.remove(userId);
        }
    }
}
