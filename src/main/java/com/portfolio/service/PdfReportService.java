package com.portfolio.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.portfolio.dto.TaxReportDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportService {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public byte[] generateTaxReportPdf(TaxReportDto report, String userName) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Title
        Paragraph title = new Paragraph("Capital Gains Tax Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // Subtitle
        Paragraph sub = new Paragraph(
                "Investor: " + userName + "  |  Financial Year: " +
                report.getFinancialYear() + "-" + (report.getFinancialYear() + 1),
                BOLD_FONT);
        sub.setAlignment(Element.ALIGN_CENTER);
        document.add(sub);
        document.add(new Paragraph(" "));

        // Summary table
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(100);
        addCell(summary, "Short-Term Capital Gains (STCG)", BOLD_FONT, BaseColor.LIGHT_GRAY);
        addCell(summary, "Rs. " + report.getStcgGains(), NORMAL_FONT, null);
        addCell(summary, "STCG Tax @ 15%", BOLD_FONT, BaseColor.LIGHT_GRAY);
        addCell(summary, "Rs. " + report.getStcgTax(), NORMAL_FONT, null);
        addCell(summary, "Long-Term Capital Gains (LTCG)", BOLD_FONT, BaseColor.LIGHT_GRAY);
        addCell(summary, "Rs. " + report.getLtcgGains(), NORMAL_FONT, null);
        addCell(summary, "Less: LTCG Exemption", BOLD_FONT, BaseColor.LIGHT_GRAY);
        addCell(summary, "Rs. " + report.getLtcgExemption(), NORMAL_FONT, null);
        addCell(summary, "LTCG Taxable", BOLD_FONT, BaseColor.LIGHT_GRAY);
        addCell(summary, "Rs. " + report.getLtcgTaxable(), NORMAL_FONT, null);
        addCell(summary, "LTCG Tax @ 10%", BOLD_FONT, BaseColor.LIGHT_GRAY);
        addCell(summary, "Rs. " + report.getLtcgTax(), NORMAL_FONT, null);
        addCell(summary, "TOTAL TAX PAYABLE", BOLD_FONT, new BaseColor(255, 200, 100));
        addCell(summary, "Rs. " + report.getTotalTax(), BOLD_FONT, new BaseColor(255, 200, 100));
        document.add(summary);
        document.add(new Paragraph(" "));

        // Lot details
        Paragraph lotsTitle = new Paragraph("Sale Details (FIFO Matched)", BOLD_FONT);
        document.add(lotsTitle);
        document.add(new Paragraph(" "));

        PdfPTable lotsTable = new PdfPTable(8);
        lotsTable.setWidthPercentage(100);
        String[] headers = {"Symbol", "Qty", "Buy Price", "Sell Price", "Buy Date", "Sell Date", "Days", "Gain (Rs.)"};
        for (String h : headers) addCell(lotsTable, h, HEADER_FONT, BaseColor.DARK_GRAY);

        for (TaxReportDto.TaxLot lot : report.getLots()) {
            addCell(lotsTable, lot.getSymbol(), NORMAL_FONT, null);
            addCell(lotsTable, String.valueOf(lot.getQuantity()), NORMAL_FONT, null);
            addCell(lotsTable, lot.getBuyPrice().toPlainString(), NORMAL_FONT, null);
            addCell(lotsTable, lot.getSellPrice().toPlainString(), NORMAL_FONT, null);
            addCell(lotsTable, lot.getBuyDate().format(DATE_FMT), NORMAL_FONT, null);
            addCell(lotsTable, lot.getSellDate().format(DATE_FMT), NORMAL_FONT, null);
            addCell(lotsTable, lot.getHoldingDays() + " (" + lot.getType() + ")", NORMAL_FONT, null);
            addCell(lotsTable, lot.getGainLoss().toPlainString(), NORMAL_FONT, null);
        }
        document.add(lotsTable);

        // Footer
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph(
                "Disclaimer: This is a computed estimate based on FIFO accounting. Please consult a CA for filing.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        if (bg != null) cell.setBackgroundColor(bg);
        table.addCell(cell);
    }
}
