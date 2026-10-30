package com.company.healthcare;

import com.company.healthcare.controller.*;
import com.company.healthcare.entity.*;
import com.company.healthcare.repository.DepartmentRepository;
import com.company.healthcare.service.HealthcareService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class HealthcareControllerTest {
    @Mock HealthcareService service;
    @Mock DepartmentRepository departments;
    MockMvc mvc;

    @Before
    public void setup() {
        HealthcareController c = new HealthcareController();
        org.springframework.test.util.ReflectionTestUtils.setField(c, "service", service);
        org.springframework.test.util.ReflectionTestUtils.setField(c, "departments", departments);
        mvc = MockMvcBuilders.standaloneSetup(c).build();
    }

    @Test
    public void postHospitalReturns201() throws Exception {
        Department d = new Department();
        d.setId(1L);
        when(departments.findAllById(List.of(1L))).thenReturn(List.of(d));
        Hospital h = new Hospital();
        h.setId(9L);
        h.setName("City");
        h.setDepartments(List.of(d));
        when(service.createHospitalAndAssignDepartments(any(), anyList())).thenReturn(h);
        mvc.perform(post("/hospitals").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"City\",\"departmentIds\":[1]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    public void departmentsEndpointWorks() throws Exception {
        when(service.getDepartmentsByHospitalId(1L)).thenReturn(List.of());
        mvc.perform(get("/hospitals/1/departments")).andExpect(status().isOk());
    }

    @Test
    public void doctorsEndpointWorks() throws Exception {
        when(service.getDoctorsByDepartmentId(2L)).thenReturn(List.of());
        mvc.perform(get("/departments/2/doctors")).andExpect(status().isOk());
    }

    @Test
    public void experienceEndpointUsesYearsOfExperience() throws Exception {
        when(service.getDoctorsByExperienceAndDepartment(5, 2L)).thenReturn(List.of());
        mvc.perform(get("/departments/2/doctors/experience").param("yearsOfExperience", "5")).andExpect(status().isOk());
        verify(service).getDoctorsByExperienceAndDepartment(5, 2L);
    }

    @Test
    public void missingExperienceParameterIs400() throws Exception {
        mvc.perform(get("/departments/2/doctors/experience")).andExpect(status().isBadRequest());
    }
}
