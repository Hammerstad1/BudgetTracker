package org.example.basketservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.example.basketservice.model.BasketDto;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BasketSseService {

    private static final Logger log = LoggerFactory.getLogger(BasketSseService.class);
    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    public SseEmitter subscribe(BasketDto initialPayload) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> {
            log.warn("SSE emitter error", e);
            emitters.remove(emitter);
        });

        try {
            emitter.send(SseEmitter.event().name("snapshot").data(initialPayload, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.error("Failed to send SSE snapshot", e);
            emitters.remove(emitter);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    public void broadcast(BasketDto payload) {
        Iterator<SseEmitter> iterator = emitters.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
                emitter.send(SseEmitter.event().name("update").data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitter.complete();
                iterator.remove();
            }
        }
    }
}
