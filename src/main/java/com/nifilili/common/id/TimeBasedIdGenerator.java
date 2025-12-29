package com.nifilili.common.id;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class TimeBasedIdGenerator implements IdGenerator {

    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public synchronized long generate() {
        long timestamp = System.currentTimeMillis();
        long seq = sequence.incrementAndGet() & 0xFFF; // 12 bits
        return (timestamp << 12) | seq;
    }
}

