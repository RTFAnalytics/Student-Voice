package ru.urfu.sv.utils.formatters;

import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CsvFormatter {

    public static <T> String toCsv(List<T> objects, String firstLine) {
        try (StringWriter writer = new StringWriter()) {
            writer.write(firstLine);
            var csvWriter =
                    new StatefulBeanToCsvBuilder<T>(writer)
                            .withSeparator(';')
                            .withQuotechar(ICSVWriter.NO_QUOTE_CHARACTER)
                            .build();
            csvWriter.write(objects);
            return writer.toString();
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("Во время форматирования произошла ошибка", e);
            return "Во время форматирования произошла ошибка - %s".formatted(e.getMessage());
        }
    }
}
