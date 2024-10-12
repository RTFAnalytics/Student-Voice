package ru.urfu.sv.utils.excel;

import org.apache.poi.ss.usermodel.*;

public class ExcelUtil {

    public static void addTitleStyles(Workbook workbook) {
        final Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        final CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.LEFT);
    }
}