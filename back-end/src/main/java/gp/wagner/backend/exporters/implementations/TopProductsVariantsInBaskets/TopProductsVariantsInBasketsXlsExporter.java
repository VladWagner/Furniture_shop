package gp.wagner.backend.exporters.implementations.TopProductsVariantsInBaskets;

import gp.wagner.backend.domain.dto.response.admin_panel.TopProductsVariantsInBasketsRespDto;
import gp.wagner.backend.exporters.interfaces.ExcelExporter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.util.List;

public class TopProductsVariantsInBasketsXlsExporter extends ExcelExporter<TopProductsVariantsInBasketsRespDto> {

    public TopProductsVariantsInBasketsXlsExporter(List<TopProductsVariantsInBasketsRespDto> dataList, String sheetName) {
        super(dataList, sheetName);
    }

    @Override
    public TopProductsVariantsInBasketsXlsExporter createHeaderLine() {

        // Создать 1-ю строку таблицы
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font =  workbook.createFont();

        font.setBold(true);
        font.setFontHeight(11);
        style.setFillBackgroundColor((short) 41);
        style.setFont(font);

        createCell(row, 0, "Product_name", style);
        createCell(row, 1, "Product_variant_title", style);
        createCell(row, 2, "Product_id", style);
        createCell(row, 3, "Product_variant_id", style);
        createCell(row, 4, "Baskets_count", style);
        return this;
    }

    @Override
    public TopProductsVariantsInBasketsXlsExporter writeTableRows() {

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        int rowNumber = 1;
        Row row;
        for (TopProductsVariantsInBasketsRespDto elem: dataList) {
            row = sheet.createRow(rowNumber++);

            createCell(row, 0, elem.getProductName(), style);
            createCell(row, 1, elem.getProductVariantTitle(), style);
            createCell(row, 2, elem.getProductId(), style);
            createCell(row, 3, elem.getProductVariantId(), style);
            createCell(row, 4, elem.getBasketsCount(), style);
        }
        return this;

    }
}
