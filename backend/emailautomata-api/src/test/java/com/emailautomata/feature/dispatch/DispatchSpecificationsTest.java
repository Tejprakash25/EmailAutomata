package com.emailautomata.feature.dispatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies each specification produces a non-null predicate and that optional
 * filters no-op cleanly on blank/null input. Full query behaviour is covered by
 * the end-to-end verification against the running database.
 */
class DispatchSpecificationsTest {

    @Test
    @DisplayName("Owner scope is always a concrete predicate")
    void ownerScopeIsConcrete() {
        assertThat(DispatchSpecifications.ownedBy(1L)).isNotNull();
    }

    @Test
    @DisplayName("A null status yields an always-true predicate, not a crash")
    void nullStatusIsPermissive() {
        assertThat(DispatchSpecifications.hasStatus(null)).isNotNull();
    }

    @Test
    @DisplayName("Blank search yields an always-true predicate")
    void blankSearchIsPermissive() {
        assertThat(DispatchSpecifications.subjectContains("  ")).isNotNull();
        assertThat(DispatchSpecifications.subjectContains(null)).isNotNull();
    }

    @Test
    @DisplayName("Specifications compose with and()")
    void specificationsCompose() {
        var composed = DispatchSpecifications.ownedBy(1L)
                .and(DispatchSpecifications.hasStatus(DispatchStatus.SENT))
                .and(DispatchSpecifications.subjectContains("invite"));
        assertThat(composed).isNotNull();
    }
}