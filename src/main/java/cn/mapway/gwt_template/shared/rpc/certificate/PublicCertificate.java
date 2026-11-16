package cn.mapway.gwt_template.shared.rpc.certificate;
import lombok.Data;
import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;
@Data public class PublicCertificate implements Serializable, IsSerializable {
    String id;
    String name;
    String publicKey;
}
