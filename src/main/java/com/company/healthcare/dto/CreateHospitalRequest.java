package com.company.healthcare.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class CreateHospitalRequest {
    @NotBlank
    private String name;

    @NotEmpty
    private List<Long> departmentIds;

    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public List<Long> getDepartmentIds() { return departmentIds; }
    public void setDepartmentIds(List<Long> v) { departmentIds = v; }
}
