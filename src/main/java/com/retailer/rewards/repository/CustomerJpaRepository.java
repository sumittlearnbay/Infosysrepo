package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<Customer, String> {
}
