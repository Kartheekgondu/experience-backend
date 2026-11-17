package com.uxinterfacely.experienceletter.repository;
import com.uxinterfacely.experienceletter.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EmployeeRepository extends JpaRepository<Employee, String> {}
