package com.aotal.gauge.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, String> {

    Account findByTenant(String tenant);
}
