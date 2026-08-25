package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DockerAppData {
    public DockerAppData(ClickIntention clickIntention, DockerAppEntity app, String serviceName) {
        this.intention=clickIntention;
        this.serviceName=serviceName;
        this.appEntity=app;
    }

    public static enum ClickIntention{
        CI_APP_INFO,
        CI_APP_FILES,
        CI_APP_SERVICE,
        CI_APP_LOAD_SERVICE
    }
    ClickIntention intention;
    DockerAppEntity appEntity;
    String serviceName;
    boolean loading=false;
}
