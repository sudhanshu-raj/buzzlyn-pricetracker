package com.org.scraper_bkd_security.repo;

import com.org.scraper_bkd_security.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Customer, Long> {

    Customer findUserByEmail(String username);

    Customer findUserByPhoneNumber(String phoneNumber);

    Optional<Customer> findByEmail(String email);

}
