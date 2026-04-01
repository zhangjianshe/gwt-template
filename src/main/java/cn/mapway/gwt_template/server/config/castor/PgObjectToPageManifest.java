package cn.mapway.gwt_template.server.config.castor;

import cn.mapway.gwt_template.shared.db.PageManifest;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;
import org.nutz.json.Json;
import org.postgresql.util.PGobject;

public class PgObjectToPageManifest extends Castor<PGobject, PageManifest> {
    @Override
    public PageManifest cast(PGobject src, Class<?> toType, String... args) throws FailToCastObjectException {
        return Json.fromJson(PageManifest.class, src.getValue());
    }
}
