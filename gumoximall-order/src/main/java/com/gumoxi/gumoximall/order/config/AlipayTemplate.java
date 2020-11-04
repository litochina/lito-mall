package com.gumoxi.gumoximall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.gumoxi.gumoximall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016102300744349";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCk8CB3sWuqcy1kFUQNz1pnnVcVVdvH1COKrv/D5eVDQOqHtaSpJo49uZPx94lgWkHJ7QYfE7/WTLLxXxIQFr+w2Bb3MXi0IEGZB2mxrmlsc7+kT+R4tXlTcr5dQ5jx84q1X3x1uezP+FjgcypY9wLyHrRuIErpc+7ll44Xuxpq7xiYZJ4NYwtfbDuyTCn1TYZDIDFgG47r9GhpXY7FPdSNfB4pxGVYcjV8KMhx310o6VOrPfuGLFsIOZaHG0iD6T3Qg+hQGl2O+6apS8y5EjzDucLKeJztBfl/AwcY+OKn3xsPtZkhPztQcG6tPoVtXizhhKkImxQNC4eqpa3kCUHZAgMBAAECggEAQLUQF8UcNyeuhBgvtbdGgWXh1zXy3mekdfEH2GPl2Mer+wfRDKdaoG6g0wnI9GsUalK51xFZbopynW0t2fACzi9IezwGXaxV7+JPneyN4YVbhuvs90pCyfE3K8GsTCe+oD9QxaLmfASzYYLs9zAZEMHpCVdT7FiWPa6CiV7XE2IMQKOlehfiCrIEI3CJpEQhdk+ArZsjNSHQ21vCSCQRdQwofv/RF9GxD1T7Hvu+o5vNVVgqkTYeqPY29pWrCXZLuBBsXBEordHcnGZ5uMyne0Ef9RSfc8UaBdX+6LLfOgvqO+f/tx9l+AXESfINRpsmesml0EIY5iPTSciBdwjsjQKBgQD0OqWQl1BmNkbi/rsxK1l/s5dVJfYalkHQ5Qo1AXc40/4QiKg2t3dZoHN1b//P0nA0t7OETVcw8lqNkREQi1oM4UECIN9xT/+C4WjMOTVURCj8WOGs1xU1ZBkhTmJVPJinzdTr3iKBOrKIO04GMcmdaYctzYLQSpn1QV8wJFWmDwKBgQCs4ys2YumNTvEBTknmMQTECOOG+cb519phJBOmg1JgVLkhs2NAyKPeAjiM/Hzp336eHlSR/f2uFRDjvAj83awvAmWdSSH2rlYx6rmM6vmIAfGz6XlXQSM9QB9EzW2PlydNbuFT6Is0+lVSDSctfZb/vhXACJCU2HiMIeMPUn3BlwKBgQDvrsKfdTBtLyVATpftOErcPabt5JRzITU0SiNLQ8X6WmpDVaooEMOrjQDipujZzv9ZbVNbKv+/xIjD2DN0Fh5o94NyEErZDJaInqjXi+lfaokCpkPL+UUSIctb2W49knuKa0FjvZwFUVK2yeTXxXLE7jTA6ahQvHX9sIs3VuFLVwKBgHD/zdevUL2WkjlrRSvacCX9NVGcYiwcpFSHsvxLqwThe1uGl2HZghlyVCn6QV7PQD/yzQ3a9/rXNAHVff5gmNDk/SuPiE5mZlob9Akh79SwiJ0yBag9H95B0e4zYt/OSNb4mPgx7wLCeA+4HxpBkvc285pfPri6wBe/clwqa52jAoGAAozFr5X40BwgjAL+GU93MVa09A6qzH1MvD1DwUMKr8xqO6toj4o5GNYrABwhgnqdW2v0W5P+K//bLVrBdjId7Ymek6n2os+Y6aKP3mlcI1gqxMkIAJ6nW+zaqnD0IC/tGyR6CWQjfCpWFtcNci3ury5QjGI1FmDhVMvhF3dO0VM=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlpgmSg/3T7VD8CFWuLLuHw982by23J2wJb7lZKgkhDwRszUGtVpkvGZ80Zhik61iP4qo//4eleDyKCBu21D7mVHa58yA9ASo/URBn0ucWjfQ8Q+tI55Ar8raoNdc3VnO3LOxMTv9CaFDqdQJsX804wyMZOEMQsafiuPesIVjbjvXcDFkPbJPux/bVA9o2pqlQoAl8WMswDxwm6WVfidJCpoPIH8ERsc5ovT/jGPTxDnS4W0TBNrOXsLIYXsy6u/Ad2/jm9AXjeUqr98KM8RpINUvVHwOJLUrMguK/o9uafbn/Wrd8qjEJQjZIMPjx5zcL2bxJUatSDqEOvYgfNbTvQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    private String timeout = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
