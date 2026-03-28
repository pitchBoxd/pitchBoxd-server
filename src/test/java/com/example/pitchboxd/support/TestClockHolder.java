package com.example.pitchboxd.support;

import com.example.pitchboxd.global.domain.ClockHolder;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@Primary         // 동일한 타입의 빈이 있을 경우 이 녀석을 우선적으로 주입함
public class TestClockHolder implements ClockHolder {

    private LocalDateTime currentTime;

    // 스프링이 빈을 생성할 때 이 생성자를 사용하도록 합니다.
    public TestClockHolder() {
        this.currentTime = LocalDateTime.now(); // 기본값으로 현재 시간 설정
    }

    // 테스트 코드에서 직접 시간을 넣고 싶을 때 사용하는 생성자
    public TestClockHolder(LocalDateTime initialTime) {
        this.currentTime = initialTime;
    }

    @Override
    public LocalDateTime now() {
        return currentTime;
    }

    public void setTime(LocalDateTime dateTime) {
        this.currentTime = dateTime;
    }

    public void plusHours(long hours) {
        this.currentTime = this.currentTime.plusHours(hours);
    }
}
