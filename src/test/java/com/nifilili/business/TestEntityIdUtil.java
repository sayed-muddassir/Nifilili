package com.nifilili.business;

import com.nifilili.core.entity.BaseEntity;

import java.lang.reflect.Field;

/**
 * Shared helper for tests that need deterministic entity ids.
 */
public final class TestEntityIdUtil {

    private TestEntityIdUtil() {
    }

    public static <T extends BaseEntity> T withId(T entity, long id) {
        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
            return entity;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to set BaseEntity.id for test", exception);
        }
    }
}
