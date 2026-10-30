package com.company.healthcare.repository;

import com.company.healthcare.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query("select d from Doctor d where d.department.id=:departmentId order by d.id")
    List<Doctor> findByDepartment(@Param("departmentId") Long departmentId);

    @Query("select d from Doctor d where d.department.id=:departmentId and d.yearsOfExperience>:experience order by d.yearsOfExperience desc, d.id")
    List<Doctor> findExperienced(@Param("experience") Integer experience, @Param("departmentId") Long departmentId);
}
