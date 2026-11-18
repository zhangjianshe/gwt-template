package cn.mapway.gwt_template.server.service.drive;

public class DriveException extends RuntimeException {
    private final int status;
    public DriveException(int status, String message) { super(message); this.status = status; }
    public int getStatus() { return status; }
}
