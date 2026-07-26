package com.mpo.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mpo.entity.WorkOrder;
import com.mpo.entity.TechnicalSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfService {

    public byte[] generateWorkOrderPdf(WorkOrder workOrder) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        //naslov
        document.add(new Paragraph("RADNI NALOG")
                .setFont(fontBold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));

        document.add(new Paragraph(workOrder.getId())
                .setFont(fontBold)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        //osnovni podaci

        for (TechnicalSheet sheet : workOrder.getTechnicalSheets()) {
                document.add(sectionTitle("OSNOVNI PODACI", fontBold));
                Table basicTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                        .setWidth(UnitValue.createPercentValue(100));

                addRow(basicTable, "Naziv pozicije:", sheet.getPositionName(), font, fontBold);
                addRow(basicTable, "Kolicina:", sheet.getQuantity() + " kom", font, fontBold);
                document.add(basicTable);

                //materijal
                document.add(sectionTitle("MATERIJAL", fontBold));
                Table materialTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                        .setWidth(UnitValue.createPercentValue(100));

                addRow(materialTable, "Vrsta materijala:", sheet.getMaterialType().getMaterialName(), font, fontBold);
                addRow(materialTable, "Presek:", sheet.getMaterialSectionType().getTypeName().name(), font, fontBold);
                addRow(materialTable, "Duzina izratka:", sheet.getPartLength() + " mm", font, fontBold);
                addRow(materialTable, "Tehnicki dodatak:", sheet.getTechnicalAllowance() + " mm", font, fontBold);
                document.add(materialTable);

                //izracunate vrednosti
                document.add(sectionTitle("IZRACUNATE VREDNOSTI", fontBold));
                Table calcTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                        .setWidth(UnitValue.createPercentValue(100));

                addRow(calcTable, "Duzina pripremka:", sheet.getPrepLength() + " mm", font, fontBold);
                addRow(calcTable, "Masa izratka:", sheet.getPartMass() + " g", font, fontBold);
                addRow(calcTable, "Masa pripremka:", sheet.getBlankMass() + " g", font, fontBold);
                addRow(calcTable, "Masa koja se uklanja:", sheet.getRemovedMass() + " g", font, fontBold);
                document.add(calcTable);

                //obrada
                document.add(sectionTitle("OBRADA", fontBold));
                Table processingTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                        .setWidth(UnitValue.createPercentValue(100));

                addRow(processingTable, "Tehnicka obrada:", sheet.getTechnicalProcessing().getName(), font, fontBold);
                addRow(processingTable, "Povrsinska zastita:", sheet.getSurfaceProtection().getName(), font, fontBold);
                addRow(processingTable, "Masinska obrada:", sheet.getMachiningType().getName(), font, fontBold);
                document.add(processingTable);
        }

        document.close();
        return outputStream.toByteArray();
    }

    private Paragraph sectionTitle(String title, PdfFont fontBold) {
        return new Paragraph(title)
                .setFont(fontBold)
                .setFontSize(11)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setMarginTop(15)
                .setMarginBottom(0)
                .setPadding(5);
    }

    private void addRow(Table table, String label, String value, PdfFont font, PdfFont fontBold) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(fontBold).setFontSize(10))
                .setBorderRight(null)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(font).setFontSize(10))
                .setPadding(5));
    }

//ova metoda mora da se izmeni prema zahtevima tako da sadrzi i crtez zapravo
    public byte[] generateTechnicalSheetPdf(TechnicalSheet technicalSheet) throws IOException {
        WorkOrder workOrder = technicalSheet.getWorkOrder();
        return generateWorkOrderPdf(workOrder);
    }

}