package com.example.pitchboxd.global.infrastructure;

import com.example.pitchboxd.global.domain.ClockHolder;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SystemClockHolder implements ClockHolder {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
