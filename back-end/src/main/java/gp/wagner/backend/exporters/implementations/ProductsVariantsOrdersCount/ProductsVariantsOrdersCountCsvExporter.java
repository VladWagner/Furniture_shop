package gp.wagner.backend.exporters.implementations.ProductsVariantsOrdersCount;

import gp.wagner.backend.domain.dto.response.admin_panel.ProductsVariantsOrdersCountRespDto;
import gp.wagner.backend.exporters.interfaces.CsvExporter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Writer;
import java.util.List;

@AllArgsConstructor
public class ProductsVariantsOrdersCountCsvExporter implements CsvExporter {

    List<ProductsVariantsOrdersCountRespDto> dataList;


    @Override
    public void writeData(Writer responseWriter) {

    }
}
