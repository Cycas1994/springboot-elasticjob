package com.cycas.springbootelasticjob.temp;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.mantis.common.util.exception.BaseException;
import com.mantis.common.util.exception.ExceptionCategory;
import com.mantis.hc.installment.dto.BangBaseRespDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class Test {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    private static String buildSignBySHA256(Map<String, String> dataMap, String appSecret) {

        if (CollectionUtils.isEmpty(dataMap)) {
            logger.info("dataMap is empty!");
            return new String();
        }
        List<String> keyList = new ArrayList<String>(dataMap.keySet());
        Collections.sort(keyList);
        StringBuilder builder = new StringBuilder();
        keyList.forEach(key -> {
            String value = dataMap.get(key);
            if (StringUtils.isNotEmpty(value)) {
                builder.append(key + "=" + value + "&");
            }
        });
        builder.setLength(builder.length() - 1);
        builder.append(appSecret);
        String originalSign = builder.toString();
        logger.info("buildSignBySHA256 originalSign:{}", originalSign);
        String encodeSign = "";
        try {
            encodeSign = URLEncoder.encode(originalSign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("对原始签名编码失败了！", e);
        }
        encodeSign = encodeSign.replaceAll("\\+", "%20");
        logger.info("buildSignBySHA256 encodeSign:{}", encodeSign);
        String sign = FddEncryptTool.SHA256(encodeSign, "UTF-8");
        logger.info("buildSignBySHA256 buildSign:{}", sign);
        return sign;
    }

    private static String getAccessTokenFromBangBang(String url, String appKey, String appSecret) {

        logger.info("getAccessTokenFromBangBang url:{}, appKey:{}, appSecret:{}", url, appKey, appSecret);
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("appKey", appKey);
        dataMap.put("appSecret", appSecret);
        String responseJson = HTTPClientUtils.sendHttpPost(url, JSONObject.toJSONString(dataMap), new HashMap<>(), "UTF-8");
        // 帮帮返回结果
        logger.info("getAccessTokenFromBangBang responseJson:{}", responseJson);
        if (StringUtils.isBlank(responseJson)) {
            throw new BaseException(ExceptionCategory.Business_Query, "请求帮帮接口获取AccessToken失败，请联系管理员！");
        }
        BangBaseRespDTO bangBaseRespDTO = JSONObject.parseObject(responseJson, BangBaseRespDTO.class);
        if (bangBaseRespDTO.getSuccess() != 1) {
            throw new BaseException(ExceptionCategory.Business_Query, bangBaseRespDTO.getMessage());
        }
        if (StringUtils.isBlank(bangBaseRespDTO.getAccessToken())) {
            throw new BaseException(ExceptionCategory.Business_Query, "帮帮返回AccessToken失败，请联系管理员！");
        }
        return bangBaseRespDTO.getAccessToken();
    }

    private static void pay() {
        List<Map> list = new ArrayList<>();
        TreeMap<String,Object> maps1 = new TreeMap<>();
        maps1.put("termNo",1);
        maps1.put("termDeadline","2021-02-13");
        maps1.put("termAmount","2000.00");
        maps1.put("paymentAmount","2000.00");
        maps1.put("paymentStatus",1);
        maps1.put("paymentTime","2021-01-15 11:40:01");
        list.add(maps1);
        TreeMap<String,Object> maps2 = new TreeMap<>();
        maps2.put("termNo",4);
        maps2.put("termDeadline","2021-02-13");
        maps2.put("termAmount","2000.00");
        maps2.put("paymentAmount","2000.00");
        maps2.put("paymentStatus",1);
        maps2.put("paymentTime","2021-01-15 11:40:01");
        list.add(maps2);
        // 请求参数
        Map<String, String> signMap = new HashMap<>();
        signMap.put("appKey", "b0f46f598f6c936d");
        signMap.put("orderNo", "80122128082801122246");
        signMap.put("notificationType", "INSTALLMENT_ORDER_PAYMENT");
        signMap.put("installmentList", JSONObject.toJSONString(list));
        // 生成签名
        String sign = buildSignBySHA256(signMap, "cee435f21198dc608510df2e61ee438a");
        // 获取accessToken
        String accessTokenUrl = "http://devp.bangbangyouxin.cn:11117/openservice/partner/access_token.do";
        String accessToken = getAccessTokenFromBangBang(accessTokenUrl, "b0f46f598f6c936d", "cee435f21198dc608510df2e61ee438a");
        // 获取分期订单
        String orderUrl = "http://devmcapi.bjmantis.net/hc-api/bangCallback/installmentPaymentNotice";
        // 请求参数
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("appKey", "b0f46f598f6c936d");
        dataMap.put("orderNo", "80122128082801122246");
        dataMap.put("notificationType", "INSTALLMENT_ORDER_PAYMENT");
        dataMap.put("installmentList", list);
        dataMap.put("sign", sign);
        dataMap.put("accessToken", accessToken);
        String paramJson = JSONObject.toJSONString(dataMap);
        logger.info("getInstallmentFromBangBang url:{},params:{}", orderUrl, paramJson);
        String responseJson = HTTPClientUtils.sendHttpPost(orderUrl, paramJson, new HashMap<>(), "UTF-8");
        // 帮帮返回结果
        logger.info("getInstallmentFromBangBang responseJson:{}", responseJson);
    }

    private static void change() {
        List<Map> list = new ArrayList<>();
        TreeMap<String,Object> maps1 = new TreeMap<>();
        maps1.put("termNo",4);
        maps1.put("termDeadline","2021-02-13");
        maps1.put("termAmount","2000.00");
        maps1.put("paymentStatus",0);
        list.add(maps1);
        TreeMap<String,Object> maps2 = new TreeMap<>();
        maps2.put("termNo",5);
        maps2.put("termDeadline","2021-02-13");
        maps2.put("termAmount","2000.00");
        maps2.put("paymentStatus",0);
        list.add(maps2);
        // 请求参数
        Map<String, String> signMap = new HashMap<>();
        signMap.put("appKey", "b0f46f598f6c936d");
        signMap.put("orderNo", "80122128082801122246");
        signMap.put("notificationType", "INSTALLMENT_ORDER_MODIFICATION");
        signMap.put("installmentList", JSONObject.toJSONString(list));
        // 生成签名
        String sign = buildSignBySHA256(signMap, "cee435f21198dc608510df2e61ee438a");
        // 获取accessToken
        String accessTokenUrl = "http://devp.bangbangyouxin.cn:11117/openservice/partner/access_token.do";
        String accessToken = getAccessTokenFromBangBang(accessTokenUrl, "b0f46f598f6c936d", "cee435f21198dc608510df2e61ee438a");
        // 获取分期订单
        String orderUrl = "http://devmcapi.bjmantis.net/hc-api/bangCallback/installmentNoticeCallback";
        // 请求参数
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("appKey", "b0f46f598f6c936d");
        dataMap.put("orderNo", "80122128082801122246");
        dataMap.put("notificationType", "INSTALLMENT_ORDER_MODIFICATION");
        dataMap.put("installmentList", list);
        dataMap.put("sign", sign);
        dataMap.put("accessToken", accessToken);
        String paramJson = JSONObject.toJSONString(dataMap);
        logger.info("getInstallmentFromBangBang url:{},params:{}", orderUrl, paramJson);
        String responseJson = HTTPClientUtils.sendHttpPost(orderUrl, paramJson, new HashMap<>(), "UTF-8");
        // 帮帮返回结果
        logger.info("getInstallmentFromBangBang responseJson:{}", responseJson);
    }

    public static void main(String[] args) {
        change();

    }
}
//本地：appKey=b0f46f598f6c936d&installmentList=[{"paymentAmount":"","paymentStatus":0,"paymentTime":"","termAmount":"2000.00","termDeadline":"2021-02-13","termNo":1}]&notificationType=INSTALLMENT_ORDER_PAYMENT&orderNo=80122128082801122246cee435f21198dc608510df2e61ee438a
//远程：appKey=b0f46f598f6c936d&installmentList=[{"paymentAmount":"","paymentStatus":0,"paymentTime":"","termAmount":"2000.00","termDeadline":"2021-02-13","termNo":1}]&notificationType=INSTALLMENT_ORDER_PAYMENT&orderNo=80122128082801122246cee435f21198dc608510df2e61ee438a