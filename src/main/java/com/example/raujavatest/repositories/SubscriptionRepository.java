package com.example.raujavatest.repositories;

import com.example.raujavatest.domain.documents.Subscription;
import org.springframework.stereotype.Repository;
import su.onno.repository.DocumentRepository;

@Repository
public interface SubscriptionRepository extends DocumentRepository<Subscription> {
}
