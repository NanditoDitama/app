package com.example.laporan2;

import java.io.Serializable;
import java.util.Date;

public class History implements Serializable {
    private String id;
    private String reportId;
    private String userId;
    private String status;
    private Date timestamp;
    private String rejectReason;public History() {}

    public History(String id, String reportId, String userId, String status, Date timestamp) {
        this.id = id;
        this.reportId = reportId;
        this.userId = userId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public History(String id, String reportId, String userId, String status, Date timestamp, String rejectReason) {
        this.id = id;
        this.reportId = reportId;
        this.userId = userId;
        this.status = status;
        this.timestamp = timestamp;
        this.rejectReason = rejectReason;
    }

    // Getter dan Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}