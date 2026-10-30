package com.company.healthcare.config;

import com.company.healthcare.entity.*;
import com.company.healthcare.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Seeds a few departments and doctors when the app starts on an empty database, so that
 * POST /hospitals has persisted department IDs to reference. Not loaded by @DataJpaTest.
 */
@Component
public class DemoDataLoader implements CommandLineRunner {
    private final DepartmentRepository departments;
    private final DoctorRepository doctors;

    public DemoDataLoader(DepartmentRepository departments, DoctorRepository doctors) {
        this.departments = departments;
        this.doctors = doctors;
    }

    @Override
    public void run(String... args) {
        if (departments.count() > 0) {
            return;
        }
        Department cardiology = departments.save(department("Cardiology"));
        Department neurology = departments.save(department("Neurology"));
        Department orthopedics = departments.save(department("Orthopedics"));

        doctors.saveAll(List.of(
                doctor("Dr. Asha Rao", "Interventional Cardiology", 12, cardiology),
                doctor("Dr. Vikram Shah", "Electrophysiology", 5, cardiology),
                doctor("Dr. Neha Iyer", "Cardiac Imaging", 8, cardiology),
                doctor("Dr. Arjun Mehta", "Stroke Care", 15, neurology),
                doctor("Dr. Priya Nair", "Epilepsy", 3, neurology),
                doctor("Dr. Rohan Das", "Sports Medicine", 7, orthopedics)));
    }

    private static Department department(String name) {
        Department d = new Department();
        d.setName(name);
        return d;
    }

    private static Doctor doctor(String name, String specialty, int years, Department department) {
        Doctor d = new Doctor();
        d.setName(name);
        d.setSpecialty(specialty);
        d.setYearsOfExperience(years);
        d.setDepartment(department);
        return d;
    }
}
