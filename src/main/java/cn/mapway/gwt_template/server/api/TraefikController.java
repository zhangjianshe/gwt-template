package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.app.DeleteAppServiceExecutor;
import cn.mapway.gwt_template.server.service.app.IndexAppServiceExecutor;
import cn.mapway.gwt_template.server.service.app.QueryAppServiceExecutor;
import cn.mapway.gwt_template.server.service.app.UpdateAppServiceExecutor;
import cn.mapway.gwt_template.shared.rpc.app.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.nutz.lang.Streams;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;

@Doc(value = "工具", group = "其他")
@RestController
@RequestMapping("/api/v1/traefik")
public class TraefikController extends ApiBaseController {
    @Resource
    QueryAppServiceExecutor queryAppServiceExecutor;
    @Resource
    UpdateAppServiceExecutor updateAppServiceExecutor;
    @Resource
    DeleteAppServiceExecutor deleteAppServiceExecutor;
    @Resource
    IndexAppServiceExecutor indexAppServiceExecutor;

    @RequestMapping(value = "/root-ca.crt", method = RequestMethod.GET)
    public void cert(HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        String certFile="/certs/root_ca.crt";
        File file=new File(certFile);
        if(file.exists())
        {
            Streams.write(response.getOutputStream(), Streams.fileIn(file));
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public void index(HttpServletResponse response) throws Exception {
        response.setContentType("text/json");
        BizResult<String> index = indexAppServiceExecutor.index();
        response.setContentLength(index.getData().length());
        Streams.write(response.getOutputStream(), index.getData().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * QueryAppService
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryAppService", retClazz = {QueryAppServiceResponse.class})
    @RequestMapping(value = "/queryAppService", method = RequestMethod.POST)
    public RpcResult<QueryAppServiceResponse> queryAppService(@RequestBody QueryAppServiceRequest request) {
        BizResult<QueryAppServiceResponse> bizResult = queryAppServiceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateAppService
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateAppService", retClazz = {UpdateAppServiceResponse.class})
    @RequestMapping(value = "/updateAppService", method = RequestMethod.POST)
    public RpcResult<UpdateAppServiceResponse> updateAppService(@RequestBody UpdateAppServiceRequest request) {
        BizResult<UpdateAppServiceResponse> bizResult = updateAppServiceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteAppService
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteAppService", retClazz = {DeleteAppServiceResponse.class})
    @RequestMapping(value = "/deleteAppService", method = RequestMethod.POST)
    public RpcResult<DeleteAppServiceResponse> deleteAppService(@RequestBody DeleteAppServiceRequest request) {
        BizResult<DeleteAppServiceResponse> bizResult = deleteAppServiceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
