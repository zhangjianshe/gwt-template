package cn.mapway.gwt_template.server.service.drive;

import java.io.IOException;
import javax.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DriveBlobMigration {
  @Resource DriveService driveService;

  @EventListener(ApplicationReadyEvent.class)
  public void migrate() throws IOException {
    driveService.migrateLegacy();
    driveService.initializeStatisticsAsync();
  }
}
