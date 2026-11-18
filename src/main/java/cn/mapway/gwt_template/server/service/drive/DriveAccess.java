package cn.mapway.gwt_template.server.service.drive;

/** Trusted request identity. Never constructed from request-supplied user ids or roles. */
public final class DriveAccess {
  final Long userId;
  final boolean admin;
  final String token;

  public DriveAccess(Long userId, boolean admin, String token) {
    this.userId = userId;
    this.admin = admin;
    this.token = token;
  }

  static DriveAccess legacy(String token, boolean admin) {
    return new DriveAccess(null, admin, token);
  }

  String uploadOwner() {
    if (token != null) return DriveService.hash(token);
    if (userId != null) return "user:" + userId;
    if (admin) return "admin";
    throw new DriveException(401, "请先登录");
  }
}
