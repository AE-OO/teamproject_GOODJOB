package com.goodjob.certification.repository;

import com.goodjob.certification.CertificateName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 박채원 22.10.04 작성
 */

public interface CertificationNameRepository extends JpaRepository<CertificateName, String> {

    @Query("select c from CertificateName c where c.certiName like %:keyword%")
    List<CertificateName> findCertiName(String keyword);
}
