package io.github.progmise.api.infrastructure.adapters.input.rest;

import io.github.progmise.api.application.ports.input.PingInputPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1")
public class PingController {

    private final PingInputPort pingInputPort;

    public PingController(PingInputPort pingInputPort) {
        this.pingInputPort = pingInputPort;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("message", pingInputPort.ping()));
    }
}
