package com.company.healthcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String specialty;
    private int yearsOfExperience;

    // Owning side: every doctor must belong to exactly one department.
    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties({"doctors", "hospitals"})
    private Department department;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String v) { specialty = v; }
    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int v) { yearsOfExperience = v; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department v) { department = v; }
}
