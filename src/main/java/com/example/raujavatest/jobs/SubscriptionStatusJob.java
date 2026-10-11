package com.example.raujavatest.jobs;

import com.example.raujavatest.domain.documents.Subscription;
import com.example.raujavatest.domain.enumerations.SubscriptionStatus;
import com.example.raujavatest.repositories.SubscriptionRepository;
import org.springframework.stereotype.Component;
import su.onno.annotations.ScheduledJob;
import su.onno.jobs.BackgroundTask;

import java.time.LocalDate;
import java.util.Objects;

@Component
@ScheduledJob(name = "SubscriptionStatusJob", cron = "0 5 0 * * *")
public class SubscriptionStatusJob implements BackgroundTask {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionStatusJob(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public void execute() {
        updateStatuses(LocalDate.now());
    }

    public int updateStatuses(LocalDate currentDate) {
        int updatedCount = 0;

        for (Subscription subscription : subscriptionRepository.findAllActive()) {
            SubscriptionStatus newStatus = determineStatus(subscription, currentDate);

            if (newStatus != null && !Objects.equals(subscription.getStatus(), newStatus)) {
                subscription.setStatus(newStatus);
                subscriptionRepository.save(subscription);

                updatedCount++;
            }
        }

        return updatedCount;
    }

    SubscriptionStatus determineStatus(Subscription subscription, LocalDate currentDate) {
        if (!subscription.isPosted()) {
            return null;
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            return null;
        }

        LocalDate startDate = subscription.getStartDate();
        LocalDate endDate = subscription.getEndDate();

        if (endDate != null && endDate.isBefore(currentDate)) {
            return SubscriptionStatus.EXPIRED;
        }

        boolean alreadyStarted = startDate != null && !startDate.isAfter(currentDate);
        boolean notExpired = endDate == null || !endDate.isBefore(currentDate);

        if (alreadyStarted && notExpired) {
            return SubscriptionStatus.ACTIVE;
        }

        return SubscriptionStatus.DRAFT;
    }
}
