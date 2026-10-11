package com.example.raujavatest.repositories;

import com.example.raujavatest.domain.catalogs.Customer;
import org.springframework.stereotype.Repository;
import su.onno.repository.CatalogRepository;

@Repository
public interface CustomerRepository extends CatalogRepository<Customer> {
}
