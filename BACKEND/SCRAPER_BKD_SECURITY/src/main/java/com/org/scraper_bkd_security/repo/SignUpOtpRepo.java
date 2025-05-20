package com.org.scraper_bkd_security.repo;

import com.org.scraper_bkd_security.models.SignUpOtpManage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignUpOtpRepo extends JpaRepository<SignUpOtpManage,Long> {
    List<SignUpOtpManage> findAllByUserID(String userId);

}
