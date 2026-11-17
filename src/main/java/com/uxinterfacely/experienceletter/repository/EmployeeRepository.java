package com.uxinterfacely.experienceletter.repository;

import com.uxinterfacely.experienceletter.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    // Handles legacy IDs stored with different casing or accidental surrounding spaces.
    @Query("select e from Employee e where upper(trim(e.employeeId)) = upper(trim(:employeeId))")
    Optional<Employee> findByEmployeeIdNormalized(@Param("employeeId") String employeeId);
}
