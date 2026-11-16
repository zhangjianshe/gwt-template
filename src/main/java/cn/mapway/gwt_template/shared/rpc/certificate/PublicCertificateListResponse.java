package cn.mapway.gwt_template.shared.rpc.certificate;
import lombok.Data;
import java.io.Serializable;
import java.util.List;
import com.google.gwt.user.client.rpc.IsSerializable;
@Data public class PublicCertificateListResponse implements Serializable, IsSerializable {
    List<PublicCertificate> certificates;
}
