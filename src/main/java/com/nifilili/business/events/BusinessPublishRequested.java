package com.nifilili.business.events;

public record BusinessPublishRequested(Long businessId, String message) {
}
