package cn.mapway.gwt_template.server.api;
import cn.mapway.biz.core.*;
import cn.mapway.gwt_template.server.service.certificate.*;
import cn.mapway.gwt_template.shared.rpc.certificate.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
@RestController @RequestMapping("/api/v1/certificate")
public class CertificateController extends ApiBaseController {
    @Resource ListCertificateExecutor listCertificateExecutor;
    @Resource SaveCertificateExecutor saveCertificateExecutor;
    @Resource ListPublicCertificateExecutor listPublicCertificateExecutor;
    @GetMapping("/list") public RpcResult<CertificateListResponse> list() {
        return toApiResult(listCertificateExecutor.execute(getBizContext(), BizRequest.wrap("", new CertificateListRequest())));
    }
    @GetMapping("/public-list") public RpcResult<PublicCertificateListResponse> publicList() {
        return toApiResult(listPublicCertificateExecutor.execute(getBizContext(), BizRequest.wrap("", new CertificateListRequest())));
    }
    @PostMapping("/save") public RpcResult<SaveCertificateResponse> save(@RequestBody SaveCertificateRequest request) {
        return toApiResult(saveCertificateExecutor.execute(getBizContext(), BizRequest.wrap("", request)));
    }
}
