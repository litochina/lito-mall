package com.gumoxi.gumoximall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import com.gumoxi.gumoximall.thirdparty.component.SmsComponent;
import com.gumoxi.gumoximall.thirdparty.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GumoximallThirdPartyApplicationTests {

    @Autowired
    OSSClient ossClient;
    @Autowired
    SmsComponent smsComponent;

    @Test
    void testSmsComponent() {
        smsComponent.sendSmsCode("13894890622", "20010309");
    }

    @Test
    void testSmsSendCode() {
        String host = "https://smsmsgs.market.alicloudapi.com";
        String path = "/sms/";
        String method = "GET";
        String appcode = "f9f031685c3d415cb8e59a0368c8b1aa";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("code", "010309");
        querys.put("phone", "13894890622");
        querys.put("skin", "1");
        querys.put("sign", "1");
        //JDK 1.8示例代码请在这里下载：  http://code.fegine.com/Tools.zip

        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 或者直接下载：
             * http://code.fegine.com/HttpUtils.zip
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             * 相关jar包（非pom）直接下载：
             * http://code.fegine.com/aliyun-jar.zip
             */
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //System.out.println(response.toString());如不输出json, 请打开这行代码，打印调试头部状态码。
            //状态码: 200 正常；400 URL无效；401 appCode错误； 403 次数用完； 500 API网管错误
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testUpload(){
        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
// 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
//        String accessKeyId = "LTAI4G3BNW3v31k4V5cGyPyp";
//        String accessKeySecret = "4iXYUlC9YIcj944hCzLkxkhxdZrpz9";

// 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 创建PutObjectRequest对象。
        PutObjectRequest putObjectRequest = new PutObjectRequest("gumoximall", "default.jpeg", new File("C:\\Users\\gumoxi\\Pictures\\jaw102ikidBf1Dd2qhn0M2ZAvLtS4vwd3Vb1kGtI.jpeg"));

// 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
// ObjectMetadata metadata = new ObjectMetadata();
// metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
// metadata.setObjectAcl(CannedAccessControlList.Private);
// putObjectRequest.setMetadata(metadata);

// 上传文件。
        ossClient.putObject(putObjectRequest);

// 关闭OSSClient。
        ossClient.shutdown();
    }

    @Test
    void contextLoads() {
    }

}
