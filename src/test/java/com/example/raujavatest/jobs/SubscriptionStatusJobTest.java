package com.example.raujavatest.jobs;

import com.example.raujavatest.domain.documents.Subscription;
import com.example.raujavatest.domain.enumerations.SubscriptionStatus;
import com.example.raujavatest.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionStatusJobTest {

    private SubscriptionRepository subscriptionRepository;
    private SubscriptionStatusJob job;

    @BeforeEach
    void setUp() {
        subscriptionRepository =
                mock(SubscriptionRepository.class);

        job = new SubscriptionStatusJob(
                subscriptionRepository
        );
    }

    @Test
    void activatesStartedSubscription() {
        Subscription subscription = new Subscription();
        subscription.setPosted(true);
        subscription.setStatus(SubscriptionStatus.DRAFT);
        subscription.setStartDate(LocalDate.of(2026, 10, 1));
        subscription.setEndDate(LocalDate.of(2026, 11, 1));

        when(subscriptionRepository.findAllActive())
                .thenReturn(List.of(subscription));

        int updated = job.updateStatuses(
                LocalDate.of(2026, 10, 11)
        );

        assertThat(updated).isEqualTo(1);
        assertThat(subscription.getStatus())
                .isEqualTo(SubscriptionStatus.ACTIVE);

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void expiresSubscriptionAfterEndDate() {
        Subscription subscription = new Subscription();
        subscription.setPosted(true);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.of(2026, 8, 1));
        subscription.setEndDate(LocalDate.of(2026, 9, 1));

        when(subscriptionRepository.findAllActive())
                .thenReturn(List.of(subscription));

        int updated = job.updateStatuses(
                LocalDate.of(2026, 10, 11)
        );

        assertThat(updated).isEqualTo(1);
        assertThat(subscription.getStatus())
                .isEqualTo(SubscriptionStatus.EXPIRED);

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void keepsFutureSubscriptionInDraftStatus() {
        Subscription subscription = new Subscription();
        subscription.setPosted(true);
        subscription.setStatus(SubscriptionStatus.DRAFT);
        subscription.setStartDate(LocalDate.of(2026, 11, 1));
        subscription.setEndDate(LocalDate.of(2026, 12, 1));

        when(subscriptionRepository.findAllActive())
                .thenReturn(List.of(subscription));

        int updated = job.updateStatuses(
                LocalDate.of(2026, 10, 11)
        );

        assertThat(updated).isZero();
        assertThat(subscription.getStatus())
                .isEqualTo(SubscriptionStatus.DRAFT);

        verify(subscriptionRepository, never())
                .save(subscription);
    }

    @Test
    void ignoresCancelledSubscription() {
        Subscription subscription = new Subscription();
        subscription.setPosted(true);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setStartDate(LocalDate.of(2026, 8, 1));
        subscription.setEndDate(LocalDate.of(2026, 9, 1));

        when(subscriptionRepository.findAllActive())
                .thenReturn(List.of(subscription));

        int updated = job.updateStatuses(
                LocalDate.of(2026, 10, 11)
        );

        assertThat(updated).isZero();
        assertThat(subscription.getStatus())
                .isEqualTo(SubscriptionStatus.CANCELLED);

        verify(subscriptionRepository, never())
                .save(subscription);
    }

    @Test
    void ignoresUnpostedSubscription() {
        Subscription subscription = new Subscription();
        subscription.setPosted(false);
        subscription.setStatus(SubscriptionStatus.DRAFT);
        subscription.setStartDate(LocalDate.of(2026, 9, 1));
        subscription.setEndDate(LocalDate.of(2026, 12, 1));

        when(subscriptionRepository.findAllActive())
                .thenReturn(List.of(subscription));

        int updated = job.updateStatuses(
                LocalDate.of(2026, 10, 11)
        );

        assertThat(updated).isZero();
        assertThat(subscription.getStatus())
                .isEqualTo(SubscriptionStatus.DRAFT);

        verify(subscriptionRepository, never())
                .save(subscription);
    }
}