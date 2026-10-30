package com.company.healthcare;

import com.company.healthcare.entity.*;
import com.company.healthcare.repository.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class JpaQueryTest {
    @Autowired HospitalRepository hospitals;
    @Autowired DepartmentRepository departments;
    @Autowired DoctorRepository doctors;

    @Test
    public void relationshipsAndQueriesWork() {
        Department c = new Department();
        c.setName("Cardiology");
        c = departments.save(c);
        Hospital h = new Hospital();
        h.setName("City");
        h.setDepartments(new ArrayList<>(List.of(c)));
        h = hospitals.saveAndFlush(h);
        Doctor a = doctor("A", 4, c), b = doctor("B", 10, c);
        doctors.saveAll(List.of(a, b));
        doctors.flush();
        assertEquals(1, hospitals.findDepartmentsByHospitalId(h.getId()).size());
        assertEquals(2, doctors.findByDepartment(c.getId()).size());
        assertEquals(1, doctors.findExperienced(5, c.getId()).size());
        assertEquals("B", doctors.findExperienced(5, c.getId()).get(0).getName());
    }

    private Doctor doctor(String n, int y, Department d) {
        Doctor x = new Doctor();
        x.setName(n);
        x.setSpecialty("Heart");
        x.setYearsOfExperience(y);
        x.setDepartment(d);
        return x;
    }
}
