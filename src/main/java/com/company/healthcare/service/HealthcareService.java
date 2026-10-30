package com.company.healthcare.service;

import com.company.healthcare.entity.*;
import java.util.*;

public interface HealthcareService {
    Hospital createHospitalAndAssignDepartments(Hospital hospital, List<Department> departments);

    List<Department> getDepartmentsByHospitalId(Long hospitalId);

    List<Doctor> getDoctorsByDepartmentId(Long departmentId);

    List<Doctor> getDoctorsByExperienceAndDepartment(Integer experience, Long departmentId);
}
