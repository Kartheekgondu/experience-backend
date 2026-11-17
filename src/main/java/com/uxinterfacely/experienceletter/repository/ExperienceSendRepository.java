package com.uxinterfacely.experienceletter.repository;

import com.uxinterfacely.experienceletter.entity.ExperienceSend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExperienceSendRepository extends JpaRepository<ExperienceSend, Long> {
    @Query("select e from ExperienceSend e where upper(trim(e.employeeId)) = upper(trim(:employeeId)) order by e.createdAt desc")
    List<ExperienceSend> findLatestMatchingEmployeeId(@Param("employeeId") String employeeId);

    List<ExperienceSend> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
}
