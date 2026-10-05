package io.github.progmise.api.application.usecases;

import io.github.progmise.api.application.ports.input.PingInputPort;
import org.springframework.stereotype.Service;

@Service
public class PingUseCase implements PingInputPort {

    @Override
    public String ping() {
        return "pong";
    }
}
