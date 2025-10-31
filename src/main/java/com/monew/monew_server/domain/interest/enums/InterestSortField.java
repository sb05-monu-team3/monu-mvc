package com.monew.monew_server.domain.interest.enums;

public enum InterestSortField {
    NAME,
    SUBSCRIBER_COUNT;

    public static InterestSortField from(String raw) {
        if ("subscriberCount".equals(raw)) {
            return SUBSCRIBER_COUNT;
        }
        return NAME;
    }
}
