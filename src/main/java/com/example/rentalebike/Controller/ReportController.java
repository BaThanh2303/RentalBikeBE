package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.Report;
import com.example.rentalebike.Service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public List<Report> getAllReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @PostMapping
    public Report createReport(@RequestBody Report report) {
        return reportService.createReport(report);
    }

    @PutMapping("/{id}/resolve")
    public Report resolveReport(@PathVariable Long id) {
        return reportService.resolveReport(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
