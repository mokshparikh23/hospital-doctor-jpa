package com.company.healthcare.controller;

import com.company.healthcare.dto.CreateHospitalRequest;
import com.company.healthcare.entity.*;
import com.company.healthcare.exception.ResourceNotFoundException;
import com.company.healthcare.repository.DepartmentRepository;
import com.company.healthcare.service.HealthcareService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class HealthcareController {
    @Autowired
    private HealthcareService service;
    @Autowired
    private DepartmentRepository departments;

    @PostMapping("/hospitals")
    public ResponseEntity<Hospital> create(@Valid @RequestBody CreateHospitalRequest request) {
        List<Long> ids = request.getDepartmentIds().stream().distinct().toList();
        if (ids.contains(null)) {
            throw new IllegalArgumentException("departmentIds must not contain null");
        }

        List<Department> resolved = departments.findAllById(ids);
        Set<Long> found = resolved.stream().map(Department::getId).collect(Collectors.toSet());
        List<Long> missing = ids.stream().filter(id -> !found.contains(id)).toList();
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Department not found: " + missing);
        }

        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        Hospital saved = service.createHospitalAndAssignDepartments(hospital, resolved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/hospitals/{hospitalId}/departments")
    public List<Department> departments(@PathVariable Long hospitalId) {
        return service.getDepartmentsByHospitalId(hospitalId);
    }

    @GetMapping("/departments/{departmentId}/doctors")
    public List<Doctor> doctors(@PathVariable Long departmentId) {
        return service.getDoctorsByDepartmentId(departmentId);
    }

    @GetMapping("/departments/{departmentId}/doctors/experience")
    public List<Doctor> experienced(@PathVariable Long departmentId, @RequestParam Integer yearsOfExperience) {
        return service.getDoctorsByExperienceAndDepartment(yearsOfExperience, departmentId);
    }
}
