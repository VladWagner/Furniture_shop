package gp.wagner.backend.exporters.interfaces;

import java.io.Writer;

public interface CsvExporter {

    void writeData(Writer responseWriter);

}
