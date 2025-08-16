package io.github.tomaszziola.javabuildautomaton;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController {

    private final BuildService service;

    public WebhookController(BuildService service) {
        this.service = service;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        new Thread(service::startBuild).start();
        System.out.println("Received webhook payload: " + payload);
        return ResponseEntity.ok("Webhook received");
    }

}
