package com.portfolio.controller;

import com.portfolio.dto.TaxReportDto;
import com.portfolio.model.User;
import com.portfolio.service.PdfReportService;
import com.portfolio.service.TaxCalculatorService;
import com.portfolio.service.UserService;
import com.itextpdf.text.DocumentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/tax")
public class TaxController {

    private final TaxCalculatorService taxService;
    private final UserService userService;
    private final PdfReportService pdfService;

    public TaxController(TaxCalculatorService taxService, UserService userService,
                         PdfReportService pdfService) {
        this.taxService = taxService;
        this.userService = userService;
        this.pdfService = pdfService;
    }

    @GetMapping
    public String taxReport(@AuthenticationPrincipal UserDetails principal,
                            @RequestParam(required = false) Integer fy,
                            Model model) {
        User user = userService.getByUsername(principal.getUsername());
        int currentFy = computeCurrentFY();
        int financialYear = (fy != null) ? fy : currentFy;
        TaxReportDto report = taxService.calculateTax(user, financialYear);
        model.addAttribute("user", user);
        model.addAttribute("report", report);
        model.addAttribute("currentFy", currentFy);
        return "tax-report";
    }

    @GetMapping("/pdf/{fy}")
    public ResponseEntity<byte[]> downloadPdf(@AuthenticationPrincipal UserDetails principal,
                                              @PathVariable int fy) throws DocumentException {
        User user = userService.getByUsername(principal.getUsername());
        TaxReportDto report = taxService.calculateTax(user, fy);
        byte[] pdf = pdfService.generateTaxReportPdf(report, user.getFullName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "tax_report_FY" + fy + "-" + (fy + 1) + ".pdf");
        return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
    }

    private int computeCurrentFY() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
    }
}
