package com.example.csvpointbypoint.config;

import com.example.csvpointbypoint.error.NonRetryableCsvException;

import java.util.LinkedHashMap;
import java.util.Map;

public class KafkaErrorClassifier {
    public Map<Class<? extends Throwable>, Boolean> classifications() {
        Map<Class<? extends Throwable>, Boolean> classifications = new LinkedHashMap<>();
        classifications.put(NonRetryableCsvException.class, false);
        classifications.put(IllegalArgumentException.class, false);
        classifications.put(org.springframework.kafka.KafkaException.class, true);
        classifications.put(org.apache.kafka.common.KafkaException.class, true);
        return classifications;
    }

    public boolean isRetryable(Throwable error) {
        for (Map.Entry<Class<? extends Throwable>, Boolean> entry : classifications().entrySet()) {
            if (entry.getKey().isInstance(error)) return entry.getValue();
        }
        return true;
    }
}
