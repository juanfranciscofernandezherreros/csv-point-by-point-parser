package com.example.csvpointbypoint.mapper;

import com.example.csvpointbypoint.avro.PointByPointKey;
import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.dto.PointByPointEventDTO;
import org.springframework.stereotype.Component;

@Component
public class PointByPointMessageMapper {
    public PointByPointKey key(String eventId) {
        return PointByPointKey.newBuilder().setSourceEventId(eventId).build();
    }

    public PointByPointValue control(String eventId, String type, String filePath, long expectedRows,
                                     String matchId, String error) {
        return PointByPointValue.newBuilder()
                .setSourceEventId(eventId).setEventType(type).setFilePath(filePath)
                .setExpectedRows(expectedRows).setRowNumber(0L).setMatchId(matchId).setError(error)
                .build();
    }

    public PointByPointValue row(String eventId, String filePath, long expectedRows, long rowNumber,
                                 PointByPointEventDTO d) {
        return PointByPointValue.newBuilder()
                .setSourceEventId(eventId).setEventType("ROW").setFilePath(filePath)
                .setExpectedRows(expectedRows).setRowNumber(rowNumber)
                .setMatchId(d.matchId()).setRecordType(d.recordType()).setQuarter(d.quarter())
                .setSequence(d.sequence()).setHomeScore(d.homeScore()).setAwayScore(d.awayScore())
                .setHomePointsAdded(d.homePointsAdded()).setAwayPointsAdded(d.awayPointsAdded())
                .setLeaderSide(d.leaderSide()).setAdvantage(d.advantage())
                .setAdvantageDirection(d.advantageDirection()).setHomeIsWinning(d.homeIsWinning())
                .setAwayIsWinning(d.awayIsWinning()).build();
    }
}
