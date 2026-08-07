package com.emailautomata.feature.dispatch.dto;

/**
 * The sent / failed / pending split for one dispatch, surfaced in each history
 * row so delivery health is visible without opening the dispatch.
 */
public record DeliveryBreakdownResponse(
        long sent,
        long failed,
        long pending
) {

    public static DeliveryBreakdownResponse empty() {
        return new DeliveryBreakdownResponse(0, 0, 0);
    }
}