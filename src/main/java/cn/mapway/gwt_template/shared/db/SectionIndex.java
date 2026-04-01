package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * block manifest
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionIndex implements Serializable, IsSerializable {
    String sectionId;
    String version;
}
