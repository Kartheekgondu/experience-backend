package com.uxinterfacely.experienceletter.repository;

import com.uxinterfacely.experienceletter.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    // Match IDs regardless of case or accidental leading/trailing spaces in legacy rows.
    @Query("select e from Employee e where upper(trim(e.employeeId)) = upper(trim(:employeeId))")
    Optional<Employee> findByNormalizedEmployeeId(@Param("employeeId") String employeeId);
}
