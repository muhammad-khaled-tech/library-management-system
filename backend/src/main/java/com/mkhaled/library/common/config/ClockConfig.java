package com.mkhaled.library.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Services ask this clock for "today" instead of calling LocalDate.now(),
 * so tests can pin the date. The zone is the library's, not the server's:
 * a container running on UTC would otherwise date a loan made after midnight in Cairo to the day before.
 */
@Configuration(proxyBeanMethods = false)
public class ClockConfig {

    @Bean
    Clock clock(@Value("${library.time-zone}") ZoneId zone) {
        return Clock.system(zone);
    }
}
