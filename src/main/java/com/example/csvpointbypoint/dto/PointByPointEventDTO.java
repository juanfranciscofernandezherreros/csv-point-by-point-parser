package com.example.csvpointbypoint.dto;

public record PointByPointEventDTO(
        String matchId,
        String recordType,
        String quarter,
        int sequence,
        int homeScore,
        int awayScore,
        int homePointsAdded,
        int awayPointsAdded,
        String leaderSide,
        String advantage,
        String advantageDirection,
        boolean homeIsWinning,
        boolean awayIsWinning) {
}
