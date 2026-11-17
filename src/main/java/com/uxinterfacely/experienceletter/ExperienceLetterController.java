package com.uxinterfacely.experienceletter;

import com.uxinterfacely.experienceletter.entity.Employee;
import com.uxinterfacely.experienceletter.entity.ExperienceSend;
import com.uxinterfacely.experienceletter.repository.EmployeeRepository;
import com.uxinterfacely.experienceletter.repository.ExperienceSendRepository;
import com.uxinterfacely.experienceletter.service.LocalFileStorageService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ExperienceLetterController {
    private final EmployeeRepository employees;
    private final ExperienceSendRepository history;
    private final LocalFileStorageService files;
    private final Optional<JavaMailSender> mailSender;
    @Value("${app.from-email:}") private String fromEmail;
    @Value("${spring.mail.username:}") private String mailUsername;

    public ExperienceLetterController(EmployeeRepository employees, ExperienceSendRepository history,
            LocalFileStorageService files, Optional<JavaMailSender> mailSender) {
        this.employees = employees; this.history = history; this.files = files; this.mailSender = mailSender;
    }

    @GetMapping("/health")
    public Map<String,Object> health() { return Map.of("status","UP","service","experience-letter-api","database","mysql"); }

    private String validate(String name, String id, String designation, String doj, String lwd, String email, boolean required) {
        if (name == null || !name.matches("[A-Za-z]+(?: [A-Za-z]+)*") || name.length()<2 || name.length()>60) return "Invalid employee name";
        if (id == null || !id.matches("(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]{3,15}")) return "Invalid employee ID";
        if (designation == null || !designation.matches("[A-Za-z]+(?:[ -][A-Za-z]+)*") || designation.length()<2 || designation.length()>60) return "Invalid designation";
        try { LocalDate j=LocalDate.parse(doj), l=LocalDate.parse(lwd); if(j.isAfter(LocalDate.now()) || !l.isAfter(j)) return "Invalid employment dates"; }
        catch(Exception ex) { return "Invalid employment dates"; }
        if(required && (email==null || email.isBlank())) return "Email is required";
        if(email!=null && !email.isBlank() && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")) return "Invalid email address";
        return null;
    }

    private ExperienceSend saveHistory(String name, String id, String role, String email, String path, String status, String doj, String lwd) {
        ExperienceSend row = new ExperienceSend();
        row.setEmployeeName(name); row.setEmployeeId(id); row.setDesignation(role); row.setRecipientEmail(email);
        row.setPdfPath(path); row.setStatus(status); row.setDateOfJoining(doj); row.setLastWorkingDate(lwd);
        return history.save(row);
    }

    @PostMapping(value="/save-experience", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(@RequestParam MultipartFile pdf, @RequestParam String employeeName,
            @RequestParam String employeeId, @RequestParam String designation,
            @RequestParam(required=false) String recipientEmail, @RequestParam String doj,
            @RequestParam String lastWorkingDate) {
        String error=validate(employeeName,employeeId,designation,doj,lastWorkingDate,recipientEmail,false);
        if(error!=null) return ResponseEntity.badRequest().body(Map.of("error",error));
        try {
            String path=files.save(pdf,employeeId);
            saveHistory(employeeName,employeeId,designation,recipientEmail,path,"saved",doj,lastWorkingDate);
            return ResponseEntity.ok(Map.of("success",true,"path",path));
        } catch(Exception ex) { return ResponseEntity.status(500).body(Map.of("error",safeMessage(ex))); }
    }

    @PostMapping(value="/send-experience", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> send(@RequestParam MultipartFile pdf, @RequestParam String employeeName,
            @RequestParam String employeeId, @RequestParam String designation, @RequestParam String recipientEmail,
            @RequestParam String doj, @RequestParam String lastWorkingDate) {
        String error=validate(employeeName,employeeId,designation,doj,lastWorkingDate,recipientEmail,true);
        if(error!=null) return ResponseEntity.badRequest().body(Map.of("error",error));
        String senderAddress = fromEmail.isBlank() ? mailUsername : fromEmail;
        if(mailSender.isEmpty() || mailUsername.isBlank() || senderAddress.isBlank())
            return ResponseEntity.status(503).body(Map.of("error","SMTP email is not configured. Set MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD and optionally MAIL_FROM."));
        try {
            String path=files.save(pdf,employeeId);
            MimeMessage message=mailSender.get().createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(senderAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("Experience Letter - "+employeeName);
            helper.setText("<p>Hi "+employeeName+",</p><p>Please find attached your experience letter confirming your employment as "+designation+" from "+doj+" to "+lastWorkingDate+".</p><p>We appreciate your contributions and wish you success.</p><p>Best Regards,<br/>Human Resources</p>",true);
            helper.addAttachment(employeeId+"_Experience_Letter.pdf",pdf);
            mailSender.get().send(message);
            saveHistory(employeeName,employeeId,designation,recipientEmail,path,"sent",doj,lastWorkingDate);
            return ResponseEntity.ok(Map.of("success",true));
        } catch(Exception ex) { return ResponseEntity.status(502).body(Map.of("error","Email send failed: "+safeMessage(ex))); }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> employee(@PathVariable String employeeId) {
        String id = employeeId == null ? "" : employeeId.trim().toUpperCase(Locale.ROOT);
        // First use the employee master table. For employees already processed by
        // this application, fall back to the latest saved/sent letter history.
        Optional<Employee> master = employees.findById(id);
        if (master.isPresent()) {
            return ResponseEntity.ok(employeeMap(master.get()));
        }

        return history.findByEmployeeIdOrderByCreatedAtDesc(id).stream()
                .findFirst()
                .<ResponseEntity<?>>map(row -> ResponseEntity.ok(historyEmployeeMap(row)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Map<String,Object> historyEmployeeMap(ExperienceSend row) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("full_name", row.getEmployeeName());
        m.put("designation", row.getDesignation());
        m.put("join_date", row.getDateOfJoining());
        m.put("last_working_date", row.getLastWorkingDate());
        m.put("email", row.getRecipientEmail());
        m.put("source", "experience_sends");
        return m;
    }
    private Map<String,Object> employeeMap(Employee e) {
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("full_name",e.getFullName()); m.put("designation",e.getDesignation());
        m.put("join_date",e.getJoinDate()); m.put("last_working_date",e.getLastWorkingDate()); m.put("email",e.getEmail());
        return m;
    }

    @GetMapping("/experience-history/{employeeId}")
    public List<Map<String,Object>> history(@PathVariable String employeeId) {
        return history.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream().map(e -> {
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("id",e.getId()); m.put("employee_name",e.getEmployeeName()); m.put("employee_id",e.getEmployeeId());
            m.put("designation",e.getDesignation()); m.put("recipient_email",e.getRecipientEmail());
            m.put("pdf_path",e.getPdfPath()); m.put("status",e.getStatus()); m.put("date_of_joining",e.getDateOfJoining());
            m.put("last_working_date",e.getLastWorkingDate()); m.put("created_at",e.getCreatedAt()); return m;
        }).toList();
    }

    @GetMapping("/experience-pdf-url")
    public ResponseEntity<?> pdfUrl(@RequestParam String path) {
        if(path.contains("/") || path.contains("\\") || !path.endsWith(".pdf")) return ResponseEntity.badRequest().body(Map.of("error","Invalid PDF path"));
        return ResponseEntity.ok(Map.of("url","/api/experience-pdf?path="+path));
    }

    @GetMapping("/experience-pdf")
    public ResponseEntity<?> pdf(@RequestParam String path) {
        try {
            if(path.contains("/") || path.contains("\\") || !path.endsWith(".pdf")) return ResponseEntity.badRequest().body(Map.of("error","Invalid PDF path"));
            Resource resource=files.load(path);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.inline().filename(path).build().toString()).body(resource);
        } catch(Exception ex) { return ResponseEntity.notFound().build(); }
    }

    private String safeMessage(Exception ex) {
        String message=ex.getMessage();
        return message==null || message.isBlank() ? "Unexpected server error" : message;
    }
}
