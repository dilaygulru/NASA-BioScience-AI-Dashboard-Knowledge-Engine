package com.example.astrogenesis.service;

import com.example.astrogenesis.entity.ChatHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ChatHistoryPdfService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(74, 124, 89));
    private static final Font HEADING_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(139, 69, 54));
    private static final Font SUBHEADING_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(74, 124, 89));
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] generatePdf(ChatHistory chatHistory) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        // Add header/footer
        HeaderFooter event = new HeaderFooter();
        writer.setPageEvent(event);

        document.open();

        // Add header box
        PdfPTable headerBox = new PdfPTable(1);
        headerBox.setWidthPercentage(100);
        headerBox.setSpacingAfter(25);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new BaseColor(74, 124, 89));
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);

        // Title
        Paragraph title = new Paragraph("🔬 AstroChat Research Report",
            new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.WHITE));
        title.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(title);

        // Query in header
        Paragraph query = new Paragraph(chatHistory.getQuery(),
            new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.WHITE));
        query.setAlignment(Element.ALIGN_CENTER);
        query.setSpacingBefore(10);
        headerCell.addElement(query);

        headerBox.addCell(headerCell);
        document.add(headerBox);

        // Metadata in a clean info box
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(90);
        metaTable.setSpacingAfter(25);
        metaTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell labelCell1 = new PdfPCell(new Phrase("📅 Report Date:", SUBHEADING_FONT));
        labelCell1.setBorder(Rectangle.NO_BORDER);
        labelCell1.setBackgroundColor(new BaseColor(234, 240, 234));
        labelCell1.setPadding(10);
        metaTable.addCell(labelCell1);

        PdfPCell valueCell1 = new PdfPCell(new Phrase(
            chatHistory.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy - HH:mm")),
            NORMAL_FONT));
        valueCell1.setBorder(Rectangle.NO_BORDER);
        valueCell1.setBackgroundColor(BaseColor.WHITE);
        valueCell1.setPadding(10);
        metaTable.addCell(valueCell1);

        PdfPCell labelCell2 = new PdfPCell(new Phrase("👥 Target Audience:", SUBHEADING_FONT));
        labelCell2.setBorder(Rectangle.NO_BORDER);
        labelCell2.setBackgroundColor(new BaseColor(234, 240, 234));
        labelCell2.setPadding(10);
        metaTable.addCell(labelCell2);

        PdfPCell valueCell2 = new PdfPCell(new Phrase(
            chatHistory.getAudience().substring(0, 1).toUpperCase() + chatHistory.getAudience().substring(1),
            NORMAL_FONT));
        valueCell2.setBorder(Rectangle.NO_BORDER);
        valueCell2.setBackgroundColor(BaseColor.WHITE);
        valueCell2.setPadding(10);
        metaTable.addCell(valueCell2);

        document.add(metaTable);

        // Add separator line
        LineSeparator separator = new LineSeparator();
        separator.setLineColor(new BaseColor(74, 124, 89));
        separator.setLineWidth(2);
        document.add(new Chunk(separator));
        document.add(new Paragraph(" "));

        // Summary Section
        if (chatHistory.getSummary() != null && !chatHistory.getSummary().isEmpty()) {
            addSection(document, "📄 Summary", chatHistory.getSummary());
        }

        // Key Findings
        if (chatHistory.getKeyFindings() != null && !chatHistory.getKeyFindings().equals("null")) {
            try {
                List<String> findings = objectMapper.readValue(chatHistory.getKeyFindings(), List.class);
                if (findings != null && !findings.isEmpty()) {
                    addHeading(document, "🔬 Key Findings");
                    com.itextpdf.text.List list = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
                    list.setListSymbol("✓ ");
                    findings.forEach(finding -> list.add(new ListItem(finding, NORMAL_FONT)));
                    document.add(list);
                    document.add(new Paragraph(" "));
                }
            } catch (Exception e) {
                System.err.println("Error parsing key findings: " + e.getMessage());
            }
        }

        // Charts
        if (chatHistory.getCharts() != null && !chatHistory.getCharts().equals("null")) {
            try {
                List<Map<String, Object>> charts = objectMapper.readValue(chatHistory.getCharts(), List.class);
                if (charts != null && !charts.isEmpty()) {
                    addHeading(document, "📊 Data Visualization");
                    for (Map<String, Object> chartData : charts) {
                        addChartToPdf(document, chartData);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing charts: " + e.getMessage());
            }
        }

        // Tables
        if (chatHistory.getTables() != null && !chatHistory.getTables().equals("null")) {
            try {
                List<Map<String, Object>> tables = objectMapper.readValue(chatHistory.getTables(), List.class);
                if (tables != null && !tables.isEmpty()) {
                    addHeading(document, "📋 Comparative Data");
                    for (Map<String, Object> tableData : tables) {
                        addTableToPdf(document, tableData);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing tables: " + e.getMessage());
            }
        }

        // Recommendations
        if (chatHistory.getRecommendations() != null && !chatHistory.getRecommendations().equals("null")) {
            try {
                Map<String, Object> recommendations = objectMapper.readValue(chatHistory.getRecommendations(), Map.class);
                if (recommendations != null) {
                    addHeading(document, "💡 Recommendations");

                    if (recommendations.containsKey("forScientists")) {
                        addSubheading(document, "👨‍🔬 For Scientists");
                        List<String> recs = (List<String>) recommendations.get("forScientists");
                        addBulletList(document, recs);
                    }

                    if (recommendations.containsKey("forManagers")) {
                        addSubheading(document, "👔 For Managers");
                        List<String> recs = (List<String>) recommendations.get("forManagers");
                        addBulletList(document, recs);
                    }

                    if (recommendations.containsKey("forMissionPlanners")) {
                        addSubheading(document, "🏗️ For Mission Planners");
                        List<String> recs = (List<String>) recommendations.get("forMissionPlanners");
                        addBulletList(document, recs);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing recommendations: " + e.getMessage());
            }
        }

        // Sources
        if (chatHistory.getSources() != null && !chatHistory.getSources().equals("null")) {
            try {
                List<Map<String, String>> sources = objectMapper.readValue(chatHistory.getSources(), List.class);
                if (sources != null && !sources.isEmpty()) {
                    addHeading(document, "📚 Sources & References");
                    for (Map<String, String> source : sources) {
                        Paragraph sourcePara = new Paragraph();
                        sourcePara.setFont(SMALL_FONT);
                        sourcePara.add(new Chunk("• " + source.getOrDefault("title", "Untitled"), NORMAL_FONT));
                        if (source.containsKey("doi")) {
                            sourcePara.add(new Chunk(" | DOI: " + source.get("doi"), SMALL_FONT));
                        }
                        document.add(sourcePara);
                        document.add(new Paragraph(" "));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing sources: " + e.getMessage());
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private void addMetaRow(PdfPTable table, String key, String value) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key, SUBHEADING_FONT));
        keyCell.setBorder(Rectangle.NO_BORDER);
        keyCell.setBackgroundColor(new BaseColor(234, 240, 234));
        keyCell.setPadding(8);
        table.addCell(keyCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setBackgroundColor(BaseColor.WHITE);
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }

    private void addSection(Document document, String title, String content) throws DocumentException {
        addHeading(document, title);

        // Parse HTML content properly
        String cleanContent = content
                .replaceAll("<h2>", "\n\n")
                .replaceAll("</h2>", "\n")
                .replaceAll("<h3>", "\n")
                .replaceAll("</h3>", "\n")
                .replaceAll("<p>", "")
                .replaceAll("</p>", "\n\n")
                .replaceAll("<strong>", "")
                .replaceAll("</strong>", "")
                .replaceAll("<em>", "")
                .replaceAll("</em>", "")
                .replaceAll("<br>|<br/>", "\n")
                .replaceAll("<ul>", "\n")
                .replaceAll("</ul>", "\n")
                .replaceAll("<ol>", "\n")
                .replaceAll("</ol>", "\n")
                .replaceAll("<li>", "• ")
                .replaceAll("</li>", "\n")
                .replaceAll("<[^>]*>", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\n ", "\n")
                .trim();

        // Split by double newlines to create paragraphs
        String[] paragraphs = cleanContent.split("\n\n+");

        for (String para : paragraphs) {
            if (para.trim().isEmpty()) continue;

            // Check if it's a heading
            if (para.startsWith("Research Context") || para.startsWith("Key Findings") ||
                para.startsWith("Implications") || para.startsWith("Future Directions")) {
                Paragraph heading = new Paragraph(para.trim(), SUBHEADING_FONT);
                heading.setSpacingBefore(10);
                heading.setSpacingAfter(8);
                document.add(heading);
            } else if (para.startsWith("•")) {
                // It's a bullet point
                String[] lines = para.split("\n");
                for (String line : lines) {
                    if (line.trim().startsWith("•")) {
                        Paragraph bullet = new Paragraph(line.trim(), NORMAL_FONT);
                        bullet.setIndentationLeft(20);
                        bullet.setSpacingAfter(5);
                        document.add(bullet);
                    }
                }
            } else {
                // Regular paragraph
                Paragraph p = new Paragraph(para.trim(), NORMAL_FONT);
                p.setAlignment(Element.ALIGN_JUSTIFIED);
                p.setSpacingAfter(12);
                document.add(p);
            }
        }

        document.add(new Paragraph(" "));
    }

    private void addHeading(Document document, String text) throws DocumentException {
        Paragraph heading = new Paragraph(text, HEADING_FONT);
        heading.setSpacingBefore(15);
        heading.setSpacingAfter(10);
        document.add(heading);
    }

    private void addSubheading(Document document, String text) throws DocumentException {
        Paragraph subheading = new Paragraph(text, SUBHEADING_FONT);
        subheading.setSpacingBefore(10);
        subheading.setSpacingAfter(5);
        document.add(subheading);
    }

    private void addBulletList(Document document, List<String> items) throws DocumentException {
        if (items == null || items.isEmpty()) return;

        com.itextpdf.text.List list = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
        list.setListSymbol("• ");
        items.forEach(item -> list.add(new ListItem(item, NORMAL_FONT)));
        document.add(list);
        document.add(new Paragraph(" "));
    }

    private void addChartToPdf(Document document, Map<String, Object> chartData) throws Exception {
        String chartType = (String) chartData.getOrDefault("type", "bar");
        String title = (String) chartData.getOrDefault("title", "Chart");
        List<String> xLabels = (List<String>) chartData.get("x");
        List<Number> yValues = (List<Number>) chartData.get("y");

        if (xLabels == null || yValues == null || xLabels.isEmpty() || yValues.isEmpty()) {
            return;
        }

        // Add chart title
        Paragraph chartTitle = new Paragraph(title, SUBHEADING_FONT);
        chartTitle.setAlignment(Element.ALIGN_CENTER);
        chartTitle.setSpacingBefore(10);
        chartTitle.setSpacingAfter(10);
        document.add(chartTitle);

        JFreeChart chart;

        if ("pie".equals(chartType)) {
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (int i = 0; i < Math.min(xLabels.size(), yValues.size()); i++) {
                dataset.setValue(xLabels.get(i), yValues.get(i));
            }
            chart = ChartFactory.createPieChart("", dataset, true, true, false);
        } else {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < Math.min(xLabels.size(), yValues.size()); i++) {
                dataset.addValue(yValues.get(i), "Value", xLabels.get(i));
            }
            chart = ChartFactory.createBarChart("", "", "", dataset);
        }

        // Customize chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        if (chart.getPlot() != null) {
            chart.getPlot().setBackgroundPaint(new Color(245, 248, 245));
            chart.getPlot().setOutlineVisible(false);
        }

        // Convert chart to image with better quality
        BufferedImage bufferedImage = chart.createBufferedImage(520, 320);
        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(bufferedImage, "png", chartStream);

        Image chartImage = Image.getInstance(chartStream.toByteArray());
        chartImage.scaleToFit(480, 280);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        chartImage.setBorder(Rectangle.BOX);
        chartImage.setBorderWidth(1);
        chartImage.setBorderColor(new BaseColor(201, 220, 201));
        chartImage.setSpacingAfter(10);

        document.add(chartImage);

        // Add insight if available
        if (chartData.containsKey("insight")) {
            String insightText = (String) chartData.get("insight");

            // Create a bordered box for insight
            PdfPTable insightTable = new PdfPTable(1);
            insightTable.setWidthPercentage(90);
            insightTable.setSpacingAfter(20);

            PdfPCell insightCell = new PdfPCell();
            insightCell.setBackgroundColor(new BaseColor(234, 240, 234));
            insightCell.setBorderColor(new BaseColor(74, 124, 89));
            insightCell.setBorderWidth(2);
            insightCell.setPadding(12);

            Paragraph insightPara = new Paragraph();
            insightPara.add(new Chunk("💡 Key Insight: ", SUBHEADING_FONT));
            insightPara.add(new Chunk(insightText, NORMAL_FONT));
            insightCell.addElement(insightPara);

            insightTable.addCell(insightCell);
            document.add(insightTable);
        }
    }

    private void addTableToPdf(Document document, Map<String, Object> tableData) throws DocumentException {
        String tableTitle = (String) tableData.getOrDefault("title", "Data Table");
        List<String> columns = (List<String>) tableData.get("columns");
        List<List<String>> rows = (List<List<String>>) tableData.get("rows");

        if (columns == null || rows == null || columns.isEmpty() || rows.isEmpty()) {
            return;
        }

        Paragraph tableHeading = new Paragraph(tableTitle, SUBHEADING_FONT);
        tableHeading.setSpacingBefore(15);
        tableHeading.setSpacingAfter(10);
        document.add(tableHeading);

        PdfPTable table = new PdfPTable(columns.size());
        table.setWidthPercentage(95);
        table.setSpacingAfter(10);

        // Header row with better styling
        for (String column : columns) {
            PdfPCell cell = new PdfPCell(new Phrase(column, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
            cell.setBackgroundColor(new BaseColor(74, 124, 89));
            cell.setPadding(10);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        // Data rows with alternating colors
        boolean isEvenRow = false;
        for (List<String> row : rows) {
            for (String cellData : row) {
                PdfPCell dataCell = new PdfPCell(new Phrase(cellData, NORMAL_FONT));
                dataCell.setPadding(8);
                dataCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                dataCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                // Alternating row colors
                if (isEvenRow) {
                    dataCell.setBackgroundColor(new BaseColor(245, 248, 245));
                } else {
                    dataCell.setBackgroundColor(BaseColor.WHITE);
                }

                table.addCell(dataCell);
            }
            isEvenRow = !isEvenRow;
        }

        document.add(table);

        // Add insight if available with better styling
        if (tableData.containsKey("insight")) {
            String insightText = (String) tableData.get("insight");

            PdfPTable insightTable = new PdfPTable(1);
            insightTable.setWidthPercentage(90);
            insightTable.setSpacingBefore(10);
            insightTable.setSpacingAfter(20);

            PdfPCell insightCell = new PdfPCell();
            insightCell.setBackgroundColor(new BaseColor(234, 240, 234));
            insightCell.setBorderColor(new BaseColor(74, 124, 89));
            insightCell.setBorderWidth(2);
            insightCell.setPadding(12);

            Paragraph insightPara = new Paragraph();
            insightPara.add(new Chunk("💡 Key Insight: ", SUBHEADING_FONT));
            insightPara.add(new Chunk(insightText, NORMAL_FONT));
            insightCell.addElement(insightPara);

            insightTable.addCell(insightCell);
            document.add(insightTable);
        }
    }

    // Header and Footer handler
    static class HeaderFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Header
            Phrase header = new Phrase("Dragons AstroGenesis | NASA BioScience AI Dashboard", SMALL_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    header,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.top() + 30, 0);

            // Footer
            Phrase footer = new Phrase("Page " + writer.getPageNumber(), SMALL_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 20, 0);
        }
    }
}
