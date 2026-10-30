package com.company.healthcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Inverse side of Hospital.departments; the join table is managed by Hospital.
    @ManyToMany(mappedBy = "departments")
    @JsonIgnore
    private List<Hospital> hospitals = new ArrayList<>();

    // Inverse side of Doctor.department; the department_id FK lives on the doctor table.
    @OneToMany(mappedBy = "department")
    @JsonIgnore
    private List<Doctor> doctors = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public List<Hospital> getHospitals() { return hospitals; }
    public void setHospitals(List<Hospital> v) { hospitals = v; }
    public List<Doctor> getDoctors() { return doctors; }
    public void setDoctors(List<Doctor> v) { doctors = v; }
}
