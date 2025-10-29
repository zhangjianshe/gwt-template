package cn.mapway.gwt_template.server.service.dns;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.dns.model.CloudflareConfig;
import cn.mapway.gwt_template.shared.rpc.dns.model.CloudflareError;
import cn.mapway.gwt_template.shared.rpc.dns.model.CloudflareResult;
import cn.mapway.gwt_template.shared.rpc.dns.model.DnsEntry;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Slf4j
@Service
public class CloudflareService {
    @Resource
    SystemConfigService systemConfigService;

    /**
     * 查找我可以管控的DNS列表
     * curl -X GET "https://api.cloudflare.com/client/v4/zones/<YOUR_ZONE_ID>/dns_records?type=A" \
     * -H "Authorization: Bearer <YOUR_API_TOKEN>"
     * <p>
     * {"result":[{"id":"d9cd1b583404f7f5a1377244aa72191c","name":"a.bhg.cangling.cn","type":"A","content":"106.38.198.176","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-08-29T08:11:24.936833Z","modified_on":"2024-08-29T08:11:24.936833Z"},{"id":"28de19e06aa4788ee390a7c8f7a46419","name":"agri.cangling.cn","type":"A","content":"123.113.38.3","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.181913Z","modified_on":"2025-10-25T08:24:23.580791Z"},{"id":"a87e5767054139ccfa93d8120e882083","name":"a.hhal.cangling.cn","type":"A","content":"39.162.42.75","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-10-15T05:55:20.974524Z","modified_on":"2024-10-28T09:39:28.319656Z"},{"id":"935cce4704a78fd536d8ca8401bfd744","name":"aiwork2.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-09T06:55:04.04618Z","modified_on":"2025-10-18T03:32:47.111926Z"},{"id":"c22607789c3685983eb62e5243c685f7","name":"aiwork.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.092566Z","modified_on":"2025-10-18T03:32:52.011771Z"},{"id":"95d76766ee4ebe825ab94da96f62d0f9","name":"app.cangling.cn","type":"A","content":"8.141.81.155","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-06-26T07:04:54.389786Z","modified_on":"2024-06-26T07:04:54.389786Z"},{"id":"d58a40507cc6fffe50fcfdc4b7f1ea20","name":"base.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-18T06:22:55.783204Z","modified_on":"2025-10-18T03:32:58.562388Z"},{"id":"056a98898276fe1fd7774c7f3cbc4f24","name":"base-zjk.cangling.cn","type":"A","content":"172.16.54.4","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-18T02:53:37.689077Z","modified_on":"2025-08-18T03:06:52.87916Z"},{"id":"200548a606be8afea1dbc113da30a9cc","name":"base.zjk.cangling.cn","type":"A","content":"111.63.57.169","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-15T08:12:21.287211Z","modified_on":"2025-08-18T03:12:11.436426Z"},{"id":"c3d37fd0ac7ae5b6117a89540f085360","name":"b.bhg.cangling.cn","type":"A","content":"106.38.198.176","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-08-29T08:11:33.244564Z","modified_on":"2024-08-29T08:11:48.377651Z"},{"id":"d822d53c881bce0b95005639914e4739","name":"bhg.cangling.cn","type":"A","content":"106.38.198.176","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-08-29T08:11:14.876472Z","modified_on":"2024-08-29T08:11:14.876472Z"},{"id":"8ec91a80e01337a7689e99c4771ccdb1","name":"bhgzw1.cangling.cn","type":"A","content":"119.36.105.171","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-27T02:51:35.264091Z","modified_on":"2025-08-27T02:51:35.264091Z"},{"id":"fc61cafe19e13908db3953375b44dfae","name":"bhgzw.cangling.cn","type":"A","content":"119.36.105.240","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":"北洪港政务云","tags":[],"created_on":"2024-12-20T01:51:57.722793Z","modified_on":"2024-12-22T02:26:02.3889Z","comment_modified_on":"2024-12-20T01:51:57.722793Z"},{"id":"5e21ff9b57cd8838347b776004a06907","name":"b.hhal.cangling.cn","type":"A","content":"39.162.42.75","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-10-15T05:55:52.482601Z","modified_on":"2024-10-28T09:39:18.737332Z"},{"id":"6c3aac3b28b4c9f31142df5e91b87a52","name":"build.cangling.cn","type":"A","content":"192.168.1.161","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-08T07:32:12.239798Z","modified_on":"2025-10-13T00:30:39.888674Z"},{"id":"ab1fb270d3d554c0682a18456140c972","name":"casdoor.hhal.cangling.cn","type":"A","content":"39.162.42.75","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-10-24T05:01:15.018182Z","modified_on":"2024-10-28T15:03:33.978052Z"},{"id":"28d72647a1f18192eec79ae870a32be0","name":"c.bhg.cangling.cn","type":"A","content":"106.38.198.176","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-08-29T08:11:42.139692Z","modified_on":"2024-08-29T08:11:42.139692Z"},{"id":"9a5607591550017385e545a72ad07b5f","name":"cert.cangling.cn","type":"A","content":"8.141.81.155","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-31T11:03:20.857889Z","modified_on":"2025-08-31T11:03:20.857889Z"},{"id":"10493c9404f7ea22838a9567d0df79a5","name":"c.hhal.cangling.cn","type":"A","content":"39.162.42.75","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-10-15T05:56:09.187252Z","modified_on":"2024-10-28T09:39:04.843557Z"},{"id":"6dced7e66deb6de53070b943669b652e","name":"ci.cangling.cn","type":"A","content":"192.168.1.162","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-07-04T07:09:01.329485Z","modified_on":"2025-10-12T10:27:56.825476Z"},{"id":"15a168a55f406f79cd468c56998c40fc","name":"cyjg.cangling.cn","type":"A","content":"192.168.0.218","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-06-25T10:53:37.075362Z","modified_on":"2025-06-25T10:53:37.075362Z"},{"id":"51bcf4708655fce80100bb33be72a29f","name":"data.cangling.cn","type":"A","content":"192.168.1.238","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-05T09:27:47.733299Z","modified_on":"2025-08-05T09:27:47.733299Z"},{"id":"f462fd214a5880a4b53e31e319c092cf","name":"dev.cangling.cn","type":"A","content":"192.168.1.162","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-07-16T10:44:58.423105Z","modified_on":"2025-10-12T10:28:05.433787Z"},{"id":"ec04466a03bbceec8e4a4f1fb747d311","name":"docker.cangling.cn","type":"A","content":"192.168.1.202","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-06-18T04:51:49.13252Z","modified_on":"2025-07-29T11:16:32.229345Z"},{"id":"d92b60016ec66a42d13d2b9d021c0b9d","name":"emi.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.041009Z","modified_on":"2025-10-18T03:33:04.081005Z"},{"id":"b301dddffbf55209c695f58151147934","name":"file.cangling.cn","type":"A","content":"192.168.1.160","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-10-12T06:04:44.082823Z","modified_on":"2025-10-13T06:54:17.435404Z"},{"id":"5851d9d085834cf895910e2a6e2f2a94","name":"fire.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.041426Z","modified_on":"2025-10-18T03:33:11.333113Z"},{"id":"75f29d52af487838d557245357672234","name":"ftp.cangling.cn","type":"A","content":"192.168.1.160","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-12T07:25:12.202369Z","modified_on":"2025-09-12T07:25:12.202369Z"},{"id":"cb66482ea12f613a76681f25e3f7bf21","name":"git.cangling.cn","type":"A","content":"192.168.1.162","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-07-07T06:44:45.075466Z","modified_on":"2025-10-12T10:27:14.706184Z"},{"id":"23f183d852dd82683b826f638f5549c9","name":"gscy.cangling.cn","type":"A","content":"61.178.15.132","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-06-27T05:19:03.39835Z","modified_on":"2025-06-27T05:19:03.39835Z"},{"id":"246b19dbff09a95966fc0aa5fe89c84f","name":"harbor.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-10-19T02:16:26.595244Z","modified_on":"2025-10-19T02:16:26.595244Z"},{"id":"9f3a64f0dc7e5d1463a24c25bd05be51","name":"hhal.cangling.cn","type":"A","content":"39.162.42.75","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-10-11T03:43:41.535651Z","modified_on":"2024-10-28T09:39:10.995866Z"},{"id":"3b4112128e4df346d7a27685c5e13de6","name":"hub.cangling.cn","type":"A","content":"192.168.1.162","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-02-23T16:32:18.630633Z","modified_on":"2025-10-13T09:21:27.941387Z"},{"id":"01eab94bf7e94beef40efc4a861132fd","name":"ib.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.0486Z","modified_on":"2025-10-18T03:33:17.065489Z"},{"id":"d9d348393a99ab86cd82f4ec305cf77e","name":"ib-zjk.cangling.cn","type":"A","content":"172.16.54.4","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-18T02:52:58.959927Z","modified_on":"2025-08-18T03:11:35.518112Z"},{"id":"b91df3a39a43acd6dfe1c159384c7b15","name":"ib.zjk.cangling.cn","type":"A","content":"111.63.57.169","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-15T10:06:23.737762Z","modified_on":"2025-08-18T03:07:54.680546Z"},{"id":"3c29003a4f4ea9edf7a0d3c194372577","name":"image.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-05T08:32:15.314953Z","modified_on":"2025-10-18T03:33:23.569073Z"},{"id":"0d3ff603ed58c3cc7a99b861ece8402c","name":"im.bhg.cangling.cn","type":"A","content":"106.38.198.176","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-12-16T06:48:06.245871Z","modified_on":"2024-12-16T06:48:06.245871Z"},{"id":"91daa56209b0eb293d31c24681228fbe","name":"iot.bhg.cangling.cn","type":"A","content":"106.38.198.176","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-11-17T17:05:11.528692Z","modified_on":"2024-11-17T17:05:11.528692Z"},{"id":"db9e5aa4c41cbe34fcf8798190c3c58e","name":"iot.bhgzw.cangling.cn","type":"A","content":"119.36.105.240","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-05-12T01:33:50.607509Z","modified_on":"2025-05-12T01:33:50.607509Z"},{"id":"56b83e781b1e94c47f0d252f43b49404","name":"iot.lincao.cangling.cn","type":"A","content":"60.165.62.206","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-11-18T06:37:22.685689Z","modified_on":"2024-11-18T06:37:22.685689Z"},{"id":"239d6a70cb17a823d749ab93b623c841","name":"iot.zjk.cangling.cn","type":"A","content":"111.63.57.170","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-19T11:54:21.796494Z","modified_on":"2025-08-19T11:55:51.484826Z"},{"id":"8b1cd6251945b01a3ef2c2b729004bd4","name":"jiayu.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.151349Z","modified_on":"2025-10-18T03:33:28.625709Z"},{"id":"a23c03f4ba76b7b24e85feeb0453afce","name":"k8s.cangling.cn","type":"A","content":"192.168.1.173","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-10-11T06:25:43.551335Z","modified_on":"2025-10-19T13:18:15.407836Z"},{"id":"6eebd404a931b1f02a6ebb8522fd1064","name":"lab.cangling.cn","type":"A","content":"10.122.1.3","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-07-25T03:21:07.061854Z","modified_on":"2024-07-25T03:21:07.061854Z"},{"id":"9cf9f2695c9684b9a1fb7b063a40ab00","name":"lc.cangling.cn","type":"A","content":"123.113.38.3","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":"林草本地","tags":[],"created_on":"2025-08-02T12:49:22.110304Z","modified_on":"2025-10-25T08:24:04.961762Z","comment_modified_on":"2025-08-02T12:49:22.110304Z"},{"id":"1e18a260d510a0a6c71e1cd41d898a34","name":"ldap.cangling.cn","type":"A","content":"192.168.1.162","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-07-29T05:00:19.814899Z","modified_on":"2025-10-12T10:27:28.567239Z"},{"id":"d200745629b4d315902814d447d2ef55","name":"lincao.cangling.cn","type":"A","content":"60.165.62.206","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2024-11-18T06:37:02.165922Z","modified_on":"2024-11-18T06:37:02.165922Z"},{"id":"63618255e8f8a5831f5607f224e68ea5","name":"local.cangling.cn","type":"A","content":"192.168.1.160","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-02-20T02:18:00.279312Z","modified_on":"2025-02-20T02:18:00.279312Z"},{"id":"ebb04e3515e2aefea519c4e9d7794003","name":"mieco.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.123884Z","modified_on":"2025-10-18T03:33:34.643823Z"},{"id":"e927e83d0df916da26d8baa8f31ff3c1","name":"n8n.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-16T02:25:48.47276Z","modified_on":"2025-10-18T03:33:40.766282Z"},{"id":"e012920d420cc76a16cf80c05fdb351a","name":"nanyang.cangling.cn","type":"A","content":"117.158.64.252","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-04-21T06:33:56.933208Z","modified_on":"2025-04-23T11:25:49.572292Z"},{"id":"a63be5e87710726922736a391cbc2539","name":"ndrcc.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.126353Z","modified_on":"2025-10-18T03:34:03.274108Z"},{"id":"91affd4c4797784693502cc263d09640","name":"ns1.cangling.cn","type":"A","content":"100.76.253.54","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-07-13T12:50:22.278426Z","modified_on":"2025-07-13T12:50:22.278426Z"},{"id":"5e9996192717718443be5147e49d0646","name":"rag.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.127533Z","modified_on":"2025-10-18T03:34:10.851437Z"},{"id":"dc98d02560c6e192e9c8884070f60a90","name":"repo.cangling.cn","type":"A","content":"192.168.1.202","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-07-29T11:15:42.197707Z","modified_on":"2025-07-29T11:15:42.197707Z"},{"id":"96daddd60f953b3c731eb574a90d21c6","name":"resource.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-10T08:44:06.977423Z","modified_on":"2025-10-18T03:34:17.040579Z"},{"id":"0d2a36876b20e3a5bdadf79a838321c2","name":"resource-zgj.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-22T04:03:37.086546Z","modified_on":"2025-10-18T03:34:22.778119Z"},{"id":"2926cb49697ec4158c21eab301677ec2","name":"solr.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-16T07:38:30.642765Z","modified_on":"2025-10-18T03:34:29.89517Z"},{"id":"9769699bd2eca1240f1d28ef57589138","name":"spapi.zjk.cangling.cn","type":"A","content":"111.63.57.169","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-20T09:20:24.38201Z","modified_on":"2025-08-20T09:20:24.38201Z"},{"id":"9a1110a44a825823e39cf98faa2e1985","name":"ssh.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.133556Z","modified_on":"2025-10-18T03:34:36.318808Z"},{"id":"3322b4a1eb98fbfb529fc8fd1a3da6b8","name":"test.cangling.cn","type":"A","content":"192.168.1.173","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-10-19T13:18:51.536331Z","modified_on":"2025-10-19T13:18:51.536331Z"},{"id":"70b0be6fe24dd2ba04bca72978d919a6","name":"tools.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.14261Z","modified_on":"2025-10-18T03:34:41.922818Z"},{"id":"ada9e6a3652958b60eab88c5048617c3","name":"traefik.cangling.cn","type":"A","content":"192.168.1.173","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-10-19T13:18:37.866221Z","modified_on":"2025-10-19T13:18:37.866221Z"},{"id":"523a72a232362a51b13af2fd443063b6","name":"vpn.cangling.cn","type":"A","content":"123.113.38.3","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.139579Z","modified_on":"2025-10-25T08:23:01.838779Z"},{"id":"94766f35b6db17d3b11cf44a63bfb7a2","name":"vpn.zjk.cangling.cn","type":"A","content":"172.16.54.4","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-18T02:52:29.516177Z","modified_on":"2025-08-18T02:52:29.516177Z"},{"id":"c19143087520978b5fe70a3a5622f46d","name":"water.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.028757Z","modified_on":"2025-10-18T03:34:48.062879Z"},{"id":"f3cf80df68ac85136eafebd1a0f538bd","name":"weifang.cangling.cn","type":"A","content":"8.141.81.155","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-03-18T06:17:41.253318Z","modified_on":"2025-03-18T06:17:41.253318Z"},{"id":"b4379151abcf531a247d825c4c0e89e5","name":"wiki.cangling.cn","type":"A","content":"192.168.1.162","proxiable":false,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-05T03:56:37.257443Z","modified_on":"2025-10-12T10:28:26.435103Z"},{"id":"8fd6b93caf50b40ac833e405fae5bafc","name":"www.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.05884Z","modified_on":"2025-10-18T03:34:53.468606Z"},{"id":"22a139a62bf4a05a4a82695cd10fa82c","name":"www-zgj.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-23T03:51:44.2663Z","modified_on":"2025-10-18T03:34:58.777373Z"},{"id":"236c7e9df4d42786ab7a781469d1f008","name":"yb.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-08-02T12:49:22.17554Z","modified_on":"2025-10-18T03:35:04.983503Z"},{"id":"f45f1d26fddeb96288ccad64b4930d37","name":"yld.cangling.cn","type":"A","content":"27.19.220.185","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-07-24T10:14:35.345645Z","modified_on":"2025-07-24T10:14:35.345645Z"},{"id":"1aab3560d01282278de649f60fe34a85","name":"zgj.cangling.cn","type":"A","content":"123.113.33.0","proxiable":true,"proxied":false,"ttl":1,"settings":{},"meta":{},"comment":null,"tags":[],"created_on":"2025-09-22T04:03:12.486096Z","modified_on":"2025-10-18T03:35:12.639893Z"}],"success":true,"errors":[],"messages":[],"result_info":{"page":1,"per_page":100,"count":74,"total_count":74,"total_pages":1}}
     *
     * @return
     */
    public BizResult<List<DnsEntry>> queryDnsList(String zoneId) {
        CloudflareConfig zonConfig = getZoneConfig(zoneId);
        if (zonConfig == null) {
            return BizResult.error(500, "没有配置Cloudflare Token");
        }

        String url = String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records?type=A", zonConfig.zoneId);
        Header header = Header.create().set("Authorization", String.format("Bearer %s", zonConfig.token));
        Response response = Http.get(url, header, 0);
        if (response.isOK()) {
            TypeToken<CloudflareResult<List<DnsEntry>>> typeToken = new TypeToken<CloudflareResult<List<DnsEntry>>>() {
            };
            CloudflareResult<List<DnsEntry>> result = (CloudflareResult<List<DnsEntry>>) Json.fromJson(typeToken.getType(), response.getContent());
            return BizResult.success(result.getResult());
        } else {
            return BizResult.error(500, response.getContent());
        }
    }

    public CloudflareConfig getZoneConfig(String zoneId) {
        if (Strings.isBlank(zoneId)) {
            return null;
        }
        List<CloudflareConfig> configs = getCloudflareConfigList();
        for (CloudflareConfig cloudflareConfig : configs) {
            if (Objects.equals(cloudflareConfig.zoneId, zoneId)) {
                return cloudflareConfig;
            }
        }
        return null;
    }

    /**
     * Cloudflare config list
     *
     * @return
     */
    private List<CloudflareConfig> getCloudflareConfigList() {
        return systemConfigService.getConfigFromKeyAsList(AppConstant.KEY_CLOUDFLARE_TOKEN, CloudflareConfig.class);
    }

    /**
     * curl -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records" \
     * -H "Authorization: Bearer $API_TOKEN" \
     * -H "Content-Type: application/json" \
     * --data '{
     * "type": "A",
     * "name": "sub",
     * "content": "192.0.2.1",
     * "ttl": 3600,
     * "proxied": true
     * }'
     *
     * @param dns
     */
    public BizResult<CloudflareResult<DnsEntry>> createDns(String zoneId, DnsEntry dns) {
        CloudflareConfig zonConfig = getZoneConfig(zoneId);
        if (zonConfig == null) {
            return BizResult.error(500, "没有配置Cloudflare Token");
        }

        String url = String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records", zonConfig.zoneId);
        Header header = Header.create().asJsonContentType().set("Authorization", String.format("Bearer %s", zonConfig.token));
        Map<String, Object> data = new HashMap<>();
        data.put("type", "A");
        data.put("name", dns.getName().substring(0, dns.getName().length() - zonConfig.suffix.length()));
        data.put("content", dns.getContent());
        data.put("ttl", 3600);
        data.put("proxied", false);
        Response response = Http.post3(url, data, header, 0);
        if (response.isOK()) {
            TypeToken<CloudflareResult<DnsEntry>> typeToken = new TypeToken<CloudflareResult<DnsEntry>>() {
            };
            CloudflareResult<DnsEntry> result = (CloudflareResult<DnsEntry>) Json.fromJson(typeToken.getType(), response.getContent());
            return BizResult.success(result);
        } else {
            return BizResult.error(500, response.getContent());
        }
    }

    /**
     * 单独更新域名
     * curl -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records/12345abc" \
     * -H "Authorization: Bearer $API_TOKEN" \
     * -H "Content-Type: application/json" \
     * --data '{
     * "content": "203.0.113.99",
     * "proxied": false
     * }'
     *
     * @param dns //TODO 目前只更新了 IP和 注释 未来可以添加更能多的内容
     */
    public BizResult<CloudflareResult<DnsEntry>> updateDns(String zoneId, DnsEntry dns) {
        CloudflareConfig zonConfig = getZoneConfig(zoneId);
        if (zonConfig == null) {
            return BizResult.error(500, "没有配置Cloudflare Token");
        }

        String url = String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s", zonConfig.zoneId, dns.getId());
        System.out.println(url);
        Map<String, Object> data = new HashMap<>();
        data.put("content", dns.getContent());
        data.put("comment", dns.getComment());
        BizResult<String> result1 = patchToUrl(url, data, zonConfig.token);
        if (result1.isFailed()) {
            return result1.asBizResult();
        }
        TypeToken type = new TypeToken<CloudflareResult<DnsEntry>>() {
        };
        CloudflareResult<DnsEntry> result = (CloudflareResult<DnsEntry>) Json.fromJson(type.getType(), result1.getData());
        if (result.isSuccess()) {
            return BizResult.success(result);
        } else {
            StringBuilder msg = new StringBuilder();
            for (CloudflareError error : result.getErrors()) {
                msg.append(error.getMessage()).append("\n");
            }
            return BizResult.error(500, msg.toString());
        }

    }

    /**
     * 批量更新域名的IP
     * https://api.cloudflare.com/client/v4/zones/{ZONE_ID}/dns_records/batch
     * {
     * "patches": [
     * {
     * "id": "12345abcde678901234567890abcdefg",
     * "content": "203.0.113.10",
     * "proxied": true
     * },
     * {
     * "id": "98765abcde432109876543210fedcba9",
     * "ttl": 3600,
     * "comment": "Updated TTL"
     * }
     * ],
     * "posts": [
     * // array of records to create
     * ],
     * "puts": [
     * // array of records to overwrite
     * ],
     * "deletes": [
     * // array of records to delete
     * ]
     * }
     *
     * @param zoneId
     * @param idList
     * @param ip
     */
    public BizResult<CloudflareResult> updateIps(String zoneId, List<DnsEntry> idList, String ip) {
        CloudflareConfig zonConfig = getZoneConfig(zoneId);
        if (zonConfig == null) {
            return BizResult.error(500, "没有配置Cloudflare Token");
        }

        String url = String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records/batch", zonConfig.zoneId);
        Header header = Header.create().asJsonContentType().set("Authorization", String.format("Bearer %s", zonConfig.token));
        Map<String, Object> data = new HashMap<>();
        ArrayList<Object> list = new ArrayList<>();
        for (DnsEntry dns : idList) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("id", dns.getId());
            obj.put("content", ip);
            /*obj.put("type", dns.getType());
            obj.put("name", dns.getName());
            obj.put("proxied", dns.getProxied());
            obj.put("ttl",120);*/
            list.add(obj);
        }
        data.put("patches", list);
        data.put("deletes", new ArrayList<>());
        data.put("puts", new ArrayList<>());
        data.put("posts", new ArrayList<>());
        Response response = Http.post3(url, Json.toJson(data), header, 0);
        System.out.println(response.getContent());
        if (response.isOK()) {
            CloudflareResult result = Json.fromJson(CloudflareResult.class, response.getContent());
            return BizResult.success(result);
        } else {
            return BizResult.error(500, response.getContent());
        }
    }


    public BizResult<String> patchToUrl(String targetUrl, Map<String, Object> data, String token) {
        String jsonPayload = Json.toJson(data);

        log.info("jsonPayload");
        // 2. Build the HttpRequest object
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        log.info(request.headers().toString());

        // 3. Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        try {
            // 4. Send the request and get the response
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // 5. Process the response
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:\n" + response.body());
            return BizResult.success(response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred during the HTTP request: " + e.getMessage());
            return BizResult.error(500, e.getMessage());
        }
    }
}
