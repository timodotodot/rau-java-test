package com.example.raujavatest.repositories;

import com.example.raujavatest.domain.documents.Payment;
import org.springframework.stereotype.Repository;
import su.onno.repository.DocumentRepository;

@Repository
public interface PaymentRepository extends DocumentRepository<Payment> {
}
