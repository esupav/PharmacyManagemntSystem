package com.pharmacy.util;

import com.pharmacy.database.DBconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AuditLogger {

    public enum ActionType {
        LOGIN_SUCCESS,
        LOGIN_FAIL,
        LOGOUT,
        USER_CREATED,
        USER_DELETED,
        MEDICINE_ADDED,
        MEDICINE_UPDATED,
        MEDICINE_DELETED,
        SETTINGS_UPDATED,
        SALE_CREATED,
        CREDIT_PAYMENT
    }

    public static void log(String username, ActionType actionType, String details) {
        String sql = "INSERT INTO audit_log (username, action_type, details, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, actionType.toString());
            pstmt.setString(3, details);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to write to audit log.");
            e.printStackTrace();
        }
    }
}
