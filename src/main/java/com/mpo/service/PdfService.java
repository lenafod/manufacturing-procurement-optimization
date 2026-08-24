package com.mpo.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mpo.entity.WorkOrder;
import com.mpo.entity.TechnicalSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    // ista paleta (uglavnom crno-belo, akcenat samo za RN broj i naslove sekcija - dokument se stampa)
    private static final DeviceRgb INK = new DeviceRgb(0x21, 0x26, 0x2b);
    private static final DeviceRgb STEEL = new DeviceRgb(0x5c, 0x6b, 0x74);
    private static final DeviceRgb LINE = new DeviceRgb(0xcf, 0xca, 0xbd);
    private static final DeviceRgb ACCENT = new DeviceRgb(0xc2, 0x54, 0x0a);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    // ugradjen TrueType font (ne standardni Helvetica) - jedini nacin da se ispravno prikazu
    // Č, Ć, Š, Ž, Đ, jer standardni PDF base-14 fontovi te glifove uopste nemaju, bez obzira na kodiranje
    private PdfFont loadFont(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("font resource not found: " + resourcePath);
            }
            return PdfFontFactory.createFont(inputStream.readAllBytes(), PdfEncodings.IDENTITY_H);
        }
    }

    public byte[] generateWorkOrderPdf(WorkOrder workOrder) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont font = loadFont("fonts/DejaVuSans.ttf");
        PdfFont fontBold = loadFont("fonts/DejaVuSans-Bold.ttf");

        int positionCount = workOrder.getTechnicalSheets().size();
        int positionIndex = 1;
        for (TechnicalSheet sheet : workOrder.getTechnicalSheets()) {
            addTechnicalSheetSections(document, sheet, font, fontBold, workOrder.getId(), positionIndex, positionCount);
            positionIndex++;
        }

        addSignatureSection(document, font, fontBold);

        document.close();
        return outputStream.toByteArray();
    }

    private void addTechnicalSheetSections(Document document, TechnicalSheet sheet, PdfFont font, PdfFont fontBold,
                                            String workOrderId, int positionIndex, int positionCount) {
        addTitleBlock(document, workOrderId, positionIndex, positionCount, font, fontBold);

        document.add(new Paragraph("POZ. " + positionIndex + " — " + sheet.getSheetId() + " · " + sheet.getSheetVersion())
                .setFont(font)
                .setFontSize(8)
                .setFontColor(STEEL)
                .setBorder(new SolidBorder(LINE, 0.75f))
                .setPadding(4)
                .setMarginBottom(10));

        addSectionTab(document, "OSNOVNI PODACI", fontBold);
        addFieldGrid(document, font, fontBold,
                "Naziv pozicije", sheet.getPositionName(),
                "Količina", sheet.getQuantity() + " kom");

        addSectionTab(document, "MATERIJAL", fontBold);
        addFieldGrid(document, font, fontBold,
                "Vrsta materijala", sheet.getMaterialType().getMaterialName(),
                "Presek", sheet.getMaterialSectionType().getTypeName().getDisplayName(),
                "Dužina izratka", sheet.getPartLength() + " mm",
                "Tehnički dodatak", sheet.getTechnicalAllowance() + " mm");

        addSectionTab(document, "IZRAČUNATE VREDNOSTI", fontBold);
        addFieldGrid(document, font, fontBold,
                "Dužina pripremka", sheet.getPrepLength() + " mm",
                "Masa izratka", sheet.getPartMass() + " g",
                "Masa pripremka", sheet.getBlankMass() + " g",
                "Masa za skidanje", sheet.getRemovedMass() + " g");

        addSectionTab(document, "OBRADA", fontBold);
        addFieldGrid(document, font, fontBold,
                "Tehnička obrada", sheet.getTechnicalProcessing().getName(),
                "Površinska zaštita", sheet.getSurfaceProtection().getName(),
                "Mašinska obrada", sheet.getMachiningType().getName());
    }

    private void addTitleBlock(Document document, String workOrderId, int positionIndex, int positionCount,
                                PdfFont font, PdfFont fontBold) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{34, 33, 33}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(14);

        table.addCell(titleBlockCell("RADNI NALOG", workOrderId, font, fontBold, ACCENT));
        table.addCell(titleBlockCell("DATUM", LocalDate.now().format(DATE_FORMAT), font, fontBold, INK));
        table.addCell(titleBlockCell("POZICIJA", positionIndex + " / " + positionCount, font, fontBold, INK));

        document.add(table);
    }

    private Cell titleBlockCell(String label, String value, PdfFont font, PdfFont fontBold, DeviceRgb valueColor) {
        Paragraph paragraph = new Paragraph()
                .add(new Text(label + "\n").setFont(font).setFontSize(7).setFontColor(STEEL))
                .add(new Text(value).setFont(fontBold).setFontSize(13).setFontColor(valueColor));

        return new Cell()
                .add(paragraph)
                .setBorder(new SolidBorder(INK, 1.2f))
                .setPadding(8);
    }

    private void addSectionTab(Document document, String title, PdfFont fontBold) {
        document.add(new Paragraph(title)
                .setFont(fontBold)
                .setFontSize(8)
                .setFontColor(ACCENT)
                .setBorderBottom(new SolidBorder(ACCENT, 1.2f))
                .setPaddingBottom(2)
                .setMarginTop(10)
                .setMarginBottom(5));
    }

    // parovi label/vrednost, poredjani dva po red (isti raspored kao dizajn plan za ovaj dokument)
    private void addFieldGrid(Document document, PdfFont font, PdfFont fontBold, String... labelsAndValues) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(4);

        for (int i = 0; i < labelsAndValues.length; i += 2) {
            table.addCell(fieldCell(labelsAndValues[i], labelsAndValues[i + 1], font, fontBold));
        }

        document.add(table);
    }

    private Cell fieldCell(String label, String value, PdfFont font, PdfFont fontBold) {
        Paragraph paragraph = new Paragraph()
                .add(new Text(label.toUpperCase() + "\n").setFont(font).setFontSize(7).setFontColor(STEEL))
                .add(new Text(value).setFont(fontBold).setFontSize(10).setFontColor(INK));

        return new Cell()
                .add(paragraph)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LINE, 0.5f))
                .setPadding(4);
    }

    private void addSignatureSection(Document document, PdfFont font, PdfFont fontBold) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(24);

        table.addCell(signatureBox("OPERATER", font, fontBold));
        table.addCell(signatureBox("KONTROLA KVALITETA", font, fontBold));

        document.add(table);
    }

    private Cell signatureBox(String label, PdfFont font, PdfFont fontBold) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(LINE, 0.75f))
                .setPadding(10);

        cell.add(new Paragraph(label)
                .setFont(fontBold)
                .setFontSize(8)
                .setFontColor(STEEL)
                .setMarginBottom(20));

        cell.add(new Paragraph("Ime i prezime")
                .setFont(font)
                .setFontSize(7)
                .setFontColor(STEEL)
                .setBorderBottom(new SolidBorder(LINE, 0.5f))
                .setPaddingBottom(14));

        cell.add(new Paragraph("Datum / potpis")
                .setFont(font)
                .setFontSize(7)
                .setFontColor(STEEL)
                .setBorderBottom(new SolidBorder(LINE, 0.5f))
                .setMarginTop(14)
                .setPaddingBottom(14));

        return cell;
    }

//ova metoda mora da se izmeni prema zahtevima tako da sadrzi i crtez zapravo
    public byte[] generateTechnicalSheetPdf(TechnicalSheet technicalSheet) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // CP1250 kodiranje da bi se ispravno prikazala slova Č, Ć, Š, Ž, Đ (podrazumevano kodiranje ih tiho izbacuje)
        PdfFont font = loadFont("fonts/DejaVuSans.ttf");
        PdfFont fontBold = loadFont("fonts/DejaVuSans-Bold.ttf");

        String workOrderId = technicalSheet.getWorkOrder() != null ? technicalSheet.getWorkOrder().getId() : "-";
        addTechnicalSheetSections(document, technicalSheet, font, fontBold, workOrderId, 1, 1);

        document.close();
        return outputStream.toByteArray();
    }

}
