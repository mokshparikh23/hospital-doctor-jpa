package com.company.healthcare;

import com.company.healthcare.entity.*;
import com.company.healthcare.exception.*;
import com.company.healthcare.repository.*;
import com.company.healthcare.service.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HealthcareServiceTest {
    @Mock HospitalRepository hospitals;
    @Mock DepartmentRepository departments;
    @Mock DoctorRepository doctors;
    @InjectMocks HealthcareServiceImpl service;

    @Test
    public void createsHospitalWithDepartments() {
        Hospital h = new Hospital();
        h.setName("City");
        Department d = dept(1, "Cardiology");
        when(departments.findAllById(List.of(1L))).thenReturn(List.of(d));
        when(hospitals.save(any())).thenAnswer(i -> { Hospital x = i.getArgument(0); x.setId(10L); return x; });
        Hospital out = service.createHospitalAndAssignDepartments(h, List.of(d));
        assertEquals(Long.valueOf(10), out.getId());
        assertEquals(1, out.getDepartments().size());
        assertTrue(d.getHospitals().contains(out));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void missingDepartmentRejectedOnCreate() {
        Hospital h = new Hospital();
        h.setName("City");
        service.createHospitalAndAssignDepartments(h, List.of(dept(99, "Missing")));
    }

    @Test
    public void getsDepartments() {
        when(hospitals.existsById(1L)).thenReturn(true);
        when(hospitals.findDepartmentsByHospitalId(1L)).thenReturn(List.of(dept(2, "Neuro")));
        assertEquals(1, service.getDepartmentsByHospitalId(1L).size());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void missingHospitalRejected() {
        when(hospitals.existsById(1L)).thenReturn(false);
        service.getDepartmentsByHospitalId(1L);
    }

    @Test
    public void getsDoctors() {
        when(departments.existsById(2L)).thenReturn(true);
        when(doctors.findByDepartment(2L)).thenReturn(List.of(doc(1, "A", 8)));
        assertEquals(1, service.getDoctorsByDepartmentId(2L).size());
    }

    @Test
    public void getsStrictlyMoreExperiencedDoctors() {
        when(departments.existsById(2L)).thenReturn(true);
        when(doctors.findExperienced(5, 2L)).thenReturn(List.of(doc(1, "A", 8)));
        assertEquals(8, service.getDoctorsByExperienceAndDepartment(5, 2L).get(0).getYearsOfExperience());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeExperienceRejected() {
        service.getDoctorsByExperienceAndDepartment(-1, 2L);
    }

    private Department dept(long id, String n) {
        Department d = new Department();
        d.setId(id);
        d.setName(n);
        return d;
    }

    private Doctor doc(long id, String n, int e) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setName(n);
        d.setYearsOfExperience(e);
        return d;
    }
}
