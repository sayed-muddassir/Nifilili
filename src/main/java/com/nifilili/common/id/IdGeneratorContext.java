package com.nifilili.common.id;

import org.springframework.stereotype.Component;

@Component
public class IdGeneratorContext {

    private static IdGenerator generator;

    public IdGeneratorContext(IdGenerator generator) {
        IdGeneratorContext.generator = generator;
    }

    public static long generate() {
        return generator.generate();
    }
}
