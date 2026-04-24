package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class WorkloadReader {
    public List<Double> loadPredictedWorkload(Path csvPath) {
        final List<Double> predictedWorkload = new ArrayList<>();

        if (!Files.exists(csvPath)) {
            return predictedWorkload;
        }

        try (Reader reader = Files.newBufferedReader(csvPath);
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setSkipHeaderRecord(true)
                 .setIgnoreSurroundingSpaces(true)
                 .build()
                 .parse(reader)) {

            for (CSVRecord record : csvParser) {
                String value = null;

                if (record.isMapped("predicted_workload")) {
                    value = record.get("predicted_workload");
                } else if (record.size() > 1) {
                    value = record.get(1);
                }

                if (value == null || value.isBlank()) {
                    continue;
                }

                try {
                    predictedWorkload.add(Double.parseDouble(value));
                } catch (NumberFormatException ignored) {
                    // Skip malformed rows to keep pipeline robust.
                }
            }
        } catch (IOException e) {
            System.err.println("Failed reading predictions CSV: " + e.getMessage());
        }

        return predictedWorkload;
    }

    public List<Double> generateFallbackWorkload(int horizon) {
        final List<Double> workload = new ArrayList<>(horizon);
        final Random random = new Random(42);

        for (int t = 0; t < horizon; t++) {
            final double seasonal = 60 + 25 * Math.sin((2 * Math.PI * t) / 12.0);
            final double trend = t * 0.4;
            final double noise = random.nextDouble() * 8.0 - 4.0;
            final double value = Math.max(15, seasonal + trend + noise);
            workload.add(Double.parseDouble(String.format(Locale.US, "%.3f", value)));
        }

        return workload;
    }
}
