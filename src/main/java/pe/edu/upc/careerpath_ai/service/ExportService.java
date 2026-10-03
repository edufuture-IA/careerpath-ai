package pe.edu.upc.careerpath_ai.service;

import jakarta.servlet.http.HttpServletResponse;
import pe.edu.upc.careerpath_ai.model.TestResult;
import java.io.IOException;

public interface ExportService {
    void exportTestResultToPdf(TestResult result, HttpServletResponse response) throws IOException;
    void exportTestResultToWord(TestResult result, HttpServletResponse response) throws IOException;

    // 🆕 NUEVO: para exportar varios en un ZIP
    byte[] generatePdfBytes(TestResult result) throws IOException;
}