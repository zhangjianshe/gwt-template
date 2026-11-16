package cn.mapway.gwt_template.shared.rpc.certificate;
import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;
import lombok.Data;
import cn.mapway.gwt_template.shared.db.CanglingCertificateEntity;
@Data public class SaveCertificateResponse implements Serializable, IsSerializable { CanglingCertificateEntity certificate; }
