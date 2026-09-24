package com.example.csvpointbypoint.consumer;

import com.example.csvpointbypoint.service.PointByPointPublishService;
import com.example.csvwatcher.watcher.FileEventKey;
import com.example.csvwatcher.watcher.FileEventValue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FileReadyConsumer {
    private final PointByPointPublishService publisher;

    public FileReadyConsumer(PointByPointPublishService publisher) {
        this.publisher = publisher;
    }

    @KafkaListener(topics = "${app.kafka.topics.file-ready}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<FileEventKey, FileEventValue> record) {
        FileEventValue value = record.value();
        if (value == null || !"POINT_BY_POINT".equalsIgnoreCase(value.getFileType())) return;
        publisher.publishFile(record.key().getUniqueId(), value.getFilePath(), value.getExpectedRows());
    }
}
