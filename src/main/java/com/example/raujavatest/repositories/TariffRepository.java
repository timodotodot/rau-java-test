package com.example.raujavatest.repositories;

import com.example.raujavatest.domain.catalogs.Tariff;
import org.springframework.stereotype.Repository;
import su.onno.repository.CatalogRepository;

@Repository
public interface TariffRepository extends CatalogRepository<Tariff> {
}
