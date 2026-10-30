package com.company.healthcare.repository;

import com.company.healthcare.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
