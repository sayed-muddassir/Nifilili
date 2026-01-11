package com.nifilili.business.events;

public record BusinessPublishRequestedEvent(Long businessId, String message) {
}
