package gp.wagner.backend.exporters.interfaces;

import gp.wagner.backend.domain.exceptions.classes.ApiException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class ExcelExporter<T> {

    protected XSSFWorkbook workbook;
    protected XSSFSheet sheet;

    protected List<T> dataList;

    public ExcelExporter(List<T> dataList, String sheetName) {
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet(sheetName);
        this.dataList = dataList;
    }

    // Сформировать заголовок страницы
    public abstract ExcelExporter<T> createHeaderLine();

    // Сформировать ячейку
    protected void createCell(Row row, int columnNumber, Object data, CellStyle style){
        sheet.autoSizeColumn(columnNumber);
        Cell cell = row.createCell(columnNumber);

        Class<?> dataClass = data.getClass();
        // Определение типа
        if (dataClass.equals(Integer.class))
            cell.setCellValue((Integer) data);
        else if (dataClass.equals(Long.class))
            cell.setCellValue((Long) data);
        else if (dataClass.equals(Double.class))
            cell.setCellValue((Double) data);
        else if (dataClass.equals(Float.class))
            cell.setCellValue((Float) data);
        else if (dataClass.equals(Boolean.class))
            cell.setCellValue((Boolean) data);
        else if (dataClass.equals(String.class))
            cell.setCellValue((String) data);
        else
            cell.setCellValue(data.toString());

        cell.setCellStyle(style);

    }

    // Сформировать строки таблицы
    public abstract ExcelExporter<T> writeTableRows();

    // Сформировать массив для экспорта
    public Resource export(){

        if (workbook == null)
            return null;

        byte[] bytes;

        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()){

            workbook.write(bos);

            bytes = bos.toByteArray();

        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return new ByteArrayResource(bytes);
    }


}
