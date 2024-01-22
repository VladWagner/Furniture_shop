package gp.wagner.backend.exporters.implementations.TopProductsVariantsInBaskets;

import gp.wagner.backend.domain.dto.response.admin_panel.ProductsVariantsOrdersCountRespDto;
import gp.wagner.backend.exporters.interfaces.CsvExporter;
import lombok.AllArgsConstructor;

import java.io.Writer;
import java.util.List;

@AllArgsConstructor
public class TopProductsVariantsInBasketsCsvExporter implements CsvExporter {

    List<ProductsVariantsOrdersCountRespDto> dataList;


    @Override
    public void writeData(Writer responseWriter) {

    }
}
