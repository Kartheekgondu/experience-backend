package com.uxinterfacely.experienceletter.repository;
import com.uxinterfacely.experienceletter.entity.ExperienceSend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ExperienceSendRepository extends JpaRepository<ExperienceSend, Long> {
    List<ExperienceSend> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
}
