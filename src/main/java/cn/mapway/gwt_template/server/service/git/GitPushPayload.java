package cn.mapway.gwt_template.server.service.git;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class GitPushPayload implements Serializable {
    String projectId;
    String projectName;
    String ownerName;

    // push, create, or delete
    String eventType;

    // branch or tag
    String refType;

    String ref;       // Full ref: refs/heads/main
    String branchName; // main
    String tagName;    // v1.0.1

    String oldId;
    String newId;
    Timestamp pushTime;

    String message;
    String author;
}