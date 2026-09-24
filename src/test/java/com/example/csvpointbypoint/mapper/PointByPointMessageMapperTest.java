package com.example.csvpointbypoint.mapper;

import com.example.csvpointbypoint.dto.PointByPointEventDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointByPointMessageMapperTest {
    @Test
    void mapsRowAndKeepsSourceEventId() {
        var dto = new PointByPointEventDTO("m1","point_event","Q1",1,2,0,2,0,
                "home","+2","positive",true,false);
        var mapper = new PointByPointMessageMapper();
        var key = mapper.key("event-1");
        var value = mapper.row("event-1","/data/pbp.csv",1,1,dto);
        assertEquals("event-1", key.getSourceEventId());
        assertEquals("ROW", value.getEventType());
        assertEquals("m1", value.getMatchId());
        assertEquals(1, value.getSequence());
        assertEquals(1L, value.getRowNumber());
    }
}
