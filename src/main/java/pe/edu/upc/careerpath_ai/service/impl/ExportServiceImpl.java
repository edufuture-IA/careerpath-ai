package pe.edu.upc.careerpath_ai.service.impl;

// ===== Imports explícitos de OpenPDF (NO usar wildcard) =====
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

// ===== Imports explícitos de Apache POI (NO usar wildcard) =====
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.service.ExportService;
import pe.edu.upc.careerpath_ai.service.TestService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final TestService testService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =====================================================
    // PDF — usar siempre com.lowagie.text.Document (fully qualified)
    // =====================================================
    @Override
    public void exportTestResultToPdf(TestResult result, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=resultado_" + result.getId() + ".pdf");

        // ⚠️ Clase fully qualified para evitar ambigüedad
        com.lowagie.text.Document doc = new com.lowagie.text.Document(PageSize.A4);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font h2 = new Font(Font.HELVETICA, 13, Font.BOLD);

        doc.add(new Paragraph("CareerPath AI - Reporte Vocacional", title));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Estudiante: " + result.getStudent().getFullName()));
        doc.add(new Paragraph("Fecha: " + result.getCompletedAt().format(FMT)));
        doc.add(new Paragraph("Área predominante: " + result.getTopArea()));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Puntajes por área", h2));
        Map<String, Integer> scores = testService.parseAreaScores(result.getAreaScores());
        for (var e : scores.entrySet()) {
            doc.add(new Paragraph("• " + e.getKey() + ": " + e.getValue() + " pts"));
        }

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Carreras recomendadas", h2));
        List<String> recs = testService.parseRecommendedCareers(result.getRecommendedCareers());
        for (String c : recs) {
            doc.add(new Paragraph("• " + c));
        }

        doc.close();
    }

    // =====================================================
    // WORD — usar siempre org.apache.poi.xwpf.usermodel.XWPFDocument
    // =====================================================
    @Override
    public void exportTestResultToWord(TestResult result, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition",
                "attachment; filename=resultado_" + result.getId() + ".docx");

        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun tr = title.createRun();
            tr.setText("CareerPath AI - Reporte Vocacional");
            tr.setBold(true);
            tr.setFontSize(18);

            doc.createParagraph().createRun().setText(" ");
            doc.createParagraph().createRun().setText("Estudiante: " + result.getStudent().getFullName());
            doc.createParagraph().createRun().setText("Fecha: " + result.getCompletedAt().format(FMT));
            doc.createParagraph().createRun().setText("Área predominante: " + result.getTopArea());
            doc.createParagraph().createRun().setText(" ");

            XWPFParagraph h2a = doc.createParagraph();
            XWPFRun r2a = h2a.createRun();
            r2a.setText("Puntajes por área");
            r2a.setBold(true);
            r2a.setFontSize(13);

            Map<String, Integer> scores = testService.parseAreaScores(result.getAreaScores());
            for (var e : scores.entrySet()) {
                doc.createParagraph().createRun().setText("• " + e.getKey() + ": " + e.getValue() + " pts");
            }

            doc.createParagraph().createRun().setText(" ");

            XWPFParagraph h2b = doc.createParagraph();
            XWPFRun r2b = h2b.createRun();
            r2b.setText("Carreras recomendadas");
            r2b.setBold(true);
            r2b.setFontSize(13);

            List<String> recs = testService.parseRecommendedCareers(result.getRecommendedCareers());
            for (String c : recs) {
                doc.createParagraph().createRun().setText("• " + c);
            }

            doc.write(response.getOutputStream());
        }
    }
    @Override
    public byte[] generatePdfBytes(TestResult result) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        com.lowagie.text.Document doc = new com.lowagie.text.Document(PageSize.A4);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font h2 = new Font(Font.HELVETICA, 13, Font.BOLD);

        doc.add(new Paragraph("CareerPath AI - Reporte Vocacional", title));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Estudiante: " + result.getStudent().getFullName()));
        doc.add(new Paragraph("Correo: " + result.getStudent().getUsername()));
        doc.add(new Paragraph("Fecha: " + result.getCompletedAt().format(FMT)));
        doc.add(new Paragraph("Área predominante: " + result.getTopArea()));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Puntajes por área", h2));
        Map<String, Integer> scores = testService.parseAreaScores(result.getAreaScores());
        for (var e : scores.entrySet()) {
            doc.add(new Paragraph("• " + e.getKey() + ": " + e.getValue() + " pts"));
        }

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Carreras recomendadas", h2));
        List<String> recs = testService.parseRecommendedCareers(result.getRecommendedCareers());
        for (String c : recs) {
            doc.add(new Paragraph("• " + c));
        }

        doc.close();
        return baos.toByteArray();
    }
}