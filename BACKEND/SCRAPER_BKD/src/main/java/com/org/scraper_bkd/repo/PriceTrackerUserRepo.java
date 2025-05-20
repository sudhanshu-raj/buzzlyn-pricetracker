package com.org.scraper_bkd.repo;

import com.org.scraper_bkd.model.PriceTrackerUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceTrackerUserRepo extends JpaRepository<PriceTrackerUsers,Long> {
}
