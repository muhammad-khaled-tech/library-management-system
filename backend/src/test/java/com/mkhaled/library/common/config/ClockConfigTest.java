package com.mkhaled.library.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ClockConfig.class);

    @Test
    void clockUsesTheLibraryTimeZoneNotTheServerOne() {
        contextRunner.withPropertyValues("library.time-zone=Africa/Cairo")
                .run(context -> assertThat(context.getBean(Clock.class).getZone())
                        .isEqualTo(ZoneId.of("Africa/Cairo")));
    }

    @Test
    void unknownTimeZoneStopsStartup() {
        contextRunner.withPropertyValues("library.time-zone=Mars/Olympus")
                .run(context -> assertThat(context).hasFailed());
    }
}
