package com.desarrollo.raffy.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.AuditLog;

@Repository
public interface AuditLogsRepository extends JpaRepository<AuditLog, Long>{
    
}
