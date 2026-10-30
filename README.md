# Hospital, Department & Doctor JPA

A Spring Boot application that manages hospitals, departments and doctors.

- Hospitals and departments have a **many-to-many** relationship.
- A department has **many doctors**, and every doctor belongs to exactly **one** department.

| | |
|---|---|
| Stack | Java 17, Spring Boot 3.5.7, Spring Data JPA, H2 (in-memory) |
| Testing | JUnit 4 via the JUnit Vintage engine, Mockito, MockMvc, `@DataJpaTest` |
| Build | Maven (`./mvnw`) and Gradle (`./gradlew`), both wrapped |

## Run it

```bash
# Maven
./mvnw spring-boot:run

# or Gradle
./gradlew bootRun
```

The app starts on `http://localhost:8080`. On startup, `DemoDataLoader` seeds three departments and six doctors, so `POST /hospitals` has department IDs to reference:

| ID | Department | Doctors (years of experience) |
|---|---|---|
| 1 | Cardiology | Asha Rao (12), Vikram Shah (5), Neha Iyer (8) |
| 2 | Neurology | Arjun Mehta (15), Priya Nair (3) |
| 3 | Orthopedics | Rohan Das (7) |

You can browse the database at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:healthcare`, user `sa` and an empty password.

## API

| Endpoint | Behavior |
|---|---|
| `POST /hospitals` | Body `{"name": "...", "departmentIds": [..]}`. Returns **201** with the saved hospital and its departments. Returns **404** if a department ID does not exist and **400** if the name is blank or the list is empty. |
| `GET /hospitals/{hospitalId}/departments` | Returns **200** with the hospital's departments ordered by ID, or **404** if the hospital does not exist. |
| `GET /departments/{departmentId}/doctors` | Returns **200** with the department's doctors ordered by ID, or **404** if the department does not exist. |
| `GET /departments/{departmentId}/doctors/experience?yearsOfExperience=X` | Returns **200** with doctors whose experience is **strictly greater than X**, ordered by experience (descending) and then ID. Returns **400** if X is negative or missing and **404** if the department does not exist. |

Errors are returned as RFC 7807 `application/problem+json`.

```bash
curl -i -X POST localhost:8080/hospitals \
  -H 'Content-Type: application/json' \
  -d '{"name":"City Hospital","departmentIds":[1,2]}'
# 201 {"id":1,"name":"City Hospital","departments":[{"id":1,"name":"Cardiology"},{"id":2,"name":"Neurology"}]}

curl localhost:8080/hospitals/1/departments
curl localhost:8080/departments/1/doctors
curl "localhost:8080/departments/1/doctors/experience?yearsOfExperience=5"
# Asha Rao (12) and Neha Iyer (8). Vikram Shah (5) is excluded because the filter is strictly greater than.
```

## Tests

| Purpose | Maven | Gradle |
|---|---|---|
| Compile | `./mvnw clean compile` | `./gradlew clean compileJava` |
| All tests | `./mvnw clean test` | `./gradlew clean test` |
| Service tests | `./mvnw -Dtest=HealthcareServiceTest test` | `./gradlew test --tests HealthcareServiceTest` |
| JPA tests | `./mvnw -Dtest=JpaQueryTest test` | `./gradlew test --tests JpaQueryTest` |
| Controller tests | `./mvnw -Dtest=HealthcareControllerTest test` | `./gradlew test --tests HealthcareControllerTest` |

All 13 tests pass with both build tools: 7 service, 1 JPA and 5 controller tests.

## Design notes

### Ownership and join tables

- **Hospital ↔ Department (many-to-many).** `Hospital.departments` is the **owning side**. It declares `@JoinTable(name = "hospital_department")` with the columns `hospital_id` and `department_id`. Hibernate writes rows to that join table only when the owning collection changes. `Department.hospitals` is the **inverse side** (`mappedBy = "departments"`), so changing only that list never writes to the database.
- **Department → Doctor (one-to-many).** `Doctor.department` is the **owning side**. It is a `@ManyToOne(optional = false)` with `@JoinColumn(name = "department_id", nullable = false)`, so the foreign key lives on the `doctor` table and a doctor cannot be saved without a department. `Department.doctors` is the inverse side (`mappedBy = "department"`).

### Fetching

- `Hospital.departments` uses `FetchType.EAGER`, as the contract requires. Loading a hospital also loads its departments, so the POST response can be serialized after the transaction closes (`spring.jpa.open-in-view=false`).
- `Doctor.department` keeps the JPA default for `@ManyToOne`, which is EAGER. Each doctor in a response therefore includes its department's `id` and `name`.
- The inverse collections (`Department.hospitals` and `Department.doctors`) stay LAZY. They are never serialized.

### Preventing JSON recursion

- `Department.hospitals` and `Department.doctors` are marked `@JsonIgnore`.
- `Hospital.departments` is annotated `@JsonIgnoreProperties("hospitals")`, and `Doctor.department` is annotated `@JsonIgnoreProperties({"doctors", "hospitals"})`.
- As a result, Jackson never follows a reference from Hospital to Department and back to Hospital, or from Department to Doctor and back to Department.

### Creating a hospital

1. The controller removes duplicate `departmentIds` and resolves them with `DepartmentRepository.findAllById`. If any ID does not exist, it returns **404**.
2. `HealthcareServiceImpl.createHospitalAndAssignDepartments` runs in a single transaction and does the following:
   - It rejects any department that has no ID, because only persisted departments can be assigned.
   - It loads the departments again with `findAllById` and throws `ResourceNotFoundException` if any are missing.
   - It updates **both** in-memory sides of the association: it sets `hospital.departments` and adds the hospital to each `department.hospitals`.
   - It saves the hospital. Because `Hospital` owns the join table, this inserts the `hospital_department` rows. No cascade is needed, since the departments are already managed entities.

### Query behavior (JPQL)

```sql
-- HospitalRepository.findDepartmentsByHospitalId
select distinct d from Hospital h join h.departments d where h.id = :hospitalId order by d.id

-- DoctorRepository.findByDepartment
select d from Doctor d where d.department.id = :departmentId order by d.id

-- DoctorRepository.findExperienced
select d from Doctor d
where d.department.id = :departmentId and d.yearsOfExperience > :experience
order by d.yearsOfExperience desc, d.id
```

- Each query declares an explicit `order by`, so results come back in a deterministic order.
- The experience filter uses `>`, which makes it **strictly greater than**. Doctors with exactly X years are excluded.
- The service validates input before it queries anything. A negative or null experience value raises `IllegalArgumentException` (400). A hospital or department that does not exist raises `ResourceNotFoundException` (404), which is checked with `existsById`.

## Project layout

```
README.md
build.gradle / settings.gradle / gradlew        Gradle build + wrapper
pom.xml / mvnw / .mvn/                          Maven build + wrapper
src/main/java/com/company/healthcare/
  HealthcareApplication.java
  config/DemoDataLoader.java                    demo seed data (app runtime only)
  controller/HealthcareController.java
  dto/CreateHospitalRequest.java
  entity/{Hospital,Department,Doctor}.java
  exception/ResourceNotFoundException.java
  exception/GlobalExceptionHandler.java         404 / 400 problem+json mapping
  repository/{Hospital,Department,Doctor}Repository.java
  service/HealthcareService.java
  service/HealthcareServiceImpl.java
src/main/resources/application.properties
src/test/java/com/company/healthcare/
  HealthcareServiceTest.java
  JpaQueryTest.java
  HealthcareControllerTest.java
```
