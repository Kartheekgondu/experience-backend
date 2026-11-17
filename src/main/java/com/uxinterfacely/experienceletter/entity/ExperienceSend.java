package com.uxinterfacely.experienceletter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "experience_sends")
public class ExperienceSend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;
    @Column(name = "employee_id", nullable = false, length = 15)
    private String employeeId;
    @Column(name = "designation", length = 100)
    private String designation;
    @Column(name = "recipient_email", length = 190)
    private String recipientEmail;
    @Column(name = "pdf_path", length = 255)
    private String pdfPath;
    @Column(length = 30)
    private String status;
    @Column(name = "date_of_joining", length = 10)
    private String dateOfJoining;
    @Column(name = "last_working_date", length = 10)
    private String lastWorkingDate;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String v) { employeeName = v; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String v) { employeeId = v; }
    public String getDesignation() { return designation; }
    public void setDesignation(String v) { designation = v; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String v) { recipientEmail = v; }
    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String v) { pdfPath = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public String getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(String v) { dateOfJoining = v; }
    public String getLastWorkingDate() { return lastWorkingDate; }
    public void setLastWorkingDate(String v) { lastWorkingDate = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
