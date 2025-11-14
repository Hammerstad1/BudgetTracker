package org.example.budgetservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
public class BudgetAlertController {

    private final BudgetAlertService budgetAlertService;

    @GetMapping(value = "/alerts/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAlerts(){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        budgetAlertService.addEmitter(emitter);

        emitter.onCompletion(() -> budgetAlertService.removeEmitter(emitter));
        emitter.onTimeout(() -> budgetAlertService.removeEmitter(emitter));
        emitter.onError((e) -> budgetAlertService.removeEmitter(emitter));

        return emitter;
    }
}

@Slf4j
@Component
@RequiredArgsConstructor
class BudgetAlertService{

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter){
        emitters.add(emitter);
        log.info("SSE emitter added. Total emitters: {}", emitters.size());
    }

    public void removeEmitter(SseEmitter emitter){
        emitters.remove(emitter);
        log.info("SSE emitter removed. Total emitters: {}", emitters.size());
    }

    @RabbitListener(queues = "frontend.budget.alert")
    public void onBudgetAlert(Map<String, Object> event) {
        log.info("Received budget alert, broadcasting to {} emitters", emitters.size());

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("budgetAlert")
                        .data(event));
                    log.info("Set alert to emitter");
            } catch (IOException e) {
                log.error("Failed to send SSE", e);
                removeEmitter(emitter);
            }
        });
    }
}
