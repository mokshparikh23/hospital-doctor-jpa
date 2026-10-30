package com.company.healthcare.repository;

import com.company.healthcare.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    @Query("select distinct d from Hospital h join h.departments d where h.id=:hospitalId order by d.id")
    List<Department> findDepartmentsByHospitalId(@Param("hospitalId") Long hospitalId);
}
