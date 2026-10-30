package com.company.healthcare.service;

import com.company.healthcare.entity.*;
import com.company.healthcare.exception.ResourceNotFoundException;
import com.company.healthcare.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class HealthcareServiceImpl implements HealthcareService {
    private final HospitalRepository hospitals;
    private final DepartmentRepository departments;
    private final DoctorRepository doctors;

    public HealthcareServiceImpl(HospitalRepository h, DepartmentRepository d, DoctorRepository r) {
        hospitals = h;
        departments = d;
        doctors = r;
    }

    @Override
    @Transactional
    public Hospital createHospitalAndAssignDepartments(Hospital hospital, List<Department> assigned) {
        if (hospital == null) {
            throw new IllegalArgumentException("Hospital must not be null");
        }

        // Collect requested IDs (in request order, without duplicates). An unsaved department has no ID.
        List<Long> ids = new ArrayList<>();
        for (Department department : assigned == null ? List.<Department>of() : assigned) {
            if (department == null || department.getId() == null) {
                throw new ResourceNotFoundException("Only persisted departments can be assigned");
            }
            if (!ids.contains(department.getId())) {
                ids.add(department.getId());
            }
        }

        // Re-load from the database so only persisted, managed departments are linked.
        Map<Long, Department> persisted = new HashMap<>();
        for (Department department : departments.findAllById(ids)) {
            persisted.put(department.getId(), department);
        }
        List<Long> missing = ids.stream().filter(id -> !persisted.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Department not found: " + missing);
        }

        // Keep both in-memory sides of the many-to-many in sync.
        List<Department> linked = new ArrayList<>();
        for (Long id : ids) {
            Department department = persisted.get(id);
            linked.add(department);
            if (!department.getHospitals().contains(hospital)) {
                department.getHospitals().add(hospital);
            }
        }
        hospital.setDepartments(linked);
        return hospitals.save(hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getDepartmentsByHospitalId(Long id) {
        if (!hospitals.existsById(id)) {
            throw new ResourceNotFoundException("Hospital not found: " + id);
        }
        return hospitals.findDepartmentsByHospitalId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsByDepartmentId(Long id) {
        requireDepartment(id);
        return doctors.findByDepartment(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsByExperienceAndDepartment(Integer exp, Long id) {
        if (exp == null || exp < 0) {
            throw new IllegalArgumentException("yearsOfExperience must be zero or greater");
        }
        requireDepartment(id);
        return doctors.findExperienced(exp, id);
    }

    private void requireDepartment(Long id) {
        if (!departments.existsById(id)) {
            throw new ResourceNotFoundException("Department not found: " + id);
        }
    }
}
