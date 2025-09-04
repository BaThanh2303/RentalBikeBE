package com.example.rentalebike.Service;

import com.example.rentalebike.Models.Report;
import com.example.rentalebike.Repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report getReportById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
    }

    public Report createReport(Report report) {
        return reportRepository.save(report);
    }

    public Report resolveReport(Long id) {
        Report report = getReportById(id);
        report.setStatus("RESOLVED");
        report.setResolvedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
}
