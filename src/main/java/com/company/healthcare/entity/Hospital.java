package com.company.healthcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Owning side: Hospital writes the hospital_department join table rows.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "hospital_department",
            joinColumns = @JoinColumn(name = "hospital_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"))
    @JsonIgnoreProperties("hospitals")
    private List<Department> departments = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public List<Department> getDepartments() { return departments; }
    public void setDepartments(List<Department> v) { departments = v; }
}
