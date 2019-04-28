package com.springboot.rsa.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.springboot.rsa.demo.RSAUtil.TestRSA;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * @Auther: Tj
 * @Date: 2019/4/28 14:00
 * @Description:
 */
@RestController
@RequestMapping(value = "/rsa")
@Api(description = "RSA仿支付宝加密消息传送")
public class RsaController {

    //公钥加密 私钥解密
    //公钥解签 私钥签名

    //（一般情况下会在不同用户创建应用的时候重新生成新的 平台对应该应用的密钥对）
    //平台应用私钥
    private String selfPrivateKey="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIFGQDLuMwd/zuVF1j4+BhQftJp9I2eLCDJPCe4esKlumd0eB8di0k00tYnWzGMvgnHyIj5IAgv1wQS26NybcCzmkOabkhM1i5fukzi+6cKBitTPGpT+6Bxg+RKdGOugOM8KqAl28Y891ZbSngwOW7HH24bgyGeWtqJL+P13hoozAgMBAAECgYABBZMgU+2nU4VJHqIw38CgjBebWP3cpaas7x7++NMgo7UnoEMjek57Ob1tl3sKFagMSoOmxl7txUV/SgrRI5FhZpg1V6GNORMKR037LH3+hPmumVoVhjSVvOTX5lE/S0IsOVPrLjJmpupb6zmXna5TzjNMDaWY+yaLqT1DZkZTcQJBAL4uTVb/7iTHUVHuCWgtOiqZUP7btdhT6DaXOkDH5Je5KLJZGzIJ0qZV1WSXKzvGjKmyWTprLjY7JfasIvGthr8CQQCuA73rK+pwg1wvyoIhxeqzwUr2dM03U/wVVpSTGd5CFyIk1xTCpBrUR/3txjcsjnrZaknY8iouTpsUmKVgbG2NAkBJEQr/wOZi1P0mSBjvIGk3kp3uvMT5rwsJZkKoPuRRcn7zPo7XQ7Td2R5aPkYK+jZywCXN7v3bMhBc3De0uKGLAkB6imoInRFoECkwAnSnYd+InDq02cXWC49+W00fVd0dP3ss5EVbWAMIsHXSJn6eIuvCBUZhvw8TMwmkdGYpQ9HVAkB/mIg/IIUoUvo4dcHCVZ39QqL55Pu/GDilHpBZ6Owz5TEvOofvefiICALrPlrBvW3CaM25PPfySq9dxIZ7zLBs";
    //平台应用公钥
    private String selfPublicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBRkAy7jMHf87lRdY+PgYUH7SafSNniwgyTwnuHrCpbpndHgfHYtJNNLWJ1sxjL4Jx8iI+SAIL9cEEtujcm3As5pDmm5ITNYuX7pM4vunCgYrUzxqU/ugcYPkSnRjroDjPCqgJdvGPPdWW0p4MDluxx9uG4MhnlraiS/j9d4aKMwIDAQAB";
    private Map<String,String> userPublicKeyMap=new HashMap<String,String>();
    private Map<String,String> userMap=new HashMap<String,String>();

    @ApiOperation(value = "获取平台应用公钥")
    @RequestMapping(value = "/getKey" , method = RequestMethod.GET)
    public String getKey(){
        JSONObject json=new JSONObject();
        json.put("key",selfPublicKey);
        return json.toJSONString();
    }

    //创建应用，并且生成自己的密钥对
    @ApiOperation(value = "创建应用，并且生成自己的密钥对")
    @RequestMapping(value = "/getSelfKey" , method = RequestMethod.GET)
    public String getSelfKey(){
        KeyPair keyPair = null;
        try {
            keyPair = TestRSA.getKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String privateKey = new String(Base64.encodeBase64(keyPair.getPrivate().getEncoded()));
        String publicKey = new String(Base64.encodeBase64(keyPair.getPublic().getEncoded()));
        JSONObject json=new JSONObject();
        json.put("publicKey",publicKey);
        json.put("privateKey",privateKey);
        int key= UUID.randomUUID().toString().hashCode();
        if (key<0){
            key=-key;
        }
        json.put("appId", key);
        userMap.put(key+"",key+"");
        return json.toJSONString();
    }


    @ApiOperation(value = "上传应用RSA公钥")
    @RequestMapping(value = "/saveUserPublicKey" , method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "应用id", required = true, dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "publicKey", value = "公钥", required = true, dataType = "User",paramType = "query")
    })
    public String saveUserPublicKey(String userId,String publicKey){
        JSONObject json=new JSONObject();
        json.put("msg","保存成功");
        json.put("yourPublicKey",publicKey);
        json.put("yourId",userId);
        userPublicKeyMap.put(userId,publicKey);
        return json.toJSONString();
    }

    @ApiOperation(value = "模拟支付宝接受到消息后解密验签")
    @RequestMapping(value = "/RSATest" , method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "应用id", required = true, dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "msg", value = "加密消息", required = true, dataType = "User",paramType = "query")
    })
    public String RSATest(String userId,String msg){
        System.out.println("未解密前数据："+msg);
        // RSA解密
        //解密后的文字
        String decryptData = null;
        try {
            decryptData = TestRSA.decrypt(msg, TestRSA.getPrivateKey(selfPrivateKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("解密后内容:" + decryptData);
        // RSA解密结束

        //解密结束后需要对 加密的数据 做处理  转对象什么的   这里直接转为json  因为我这里没有对应的实体类
        //测试的时候 传送的加密消息为对象  里面又 sign  和原始数据 data 两个字段，
        //可以自己新增其他的字段
        JSONObject json=JSONObject.parseObject(decryptData);
        String sign=json.getString("sign");
        System.out.println("签名为:"+sign);
        String data=json.getString("data");
        System.out.println("原始数据为:"+data);
        // RSA验签
        boolean result = false;
        try {
            result = TestRSA.verify(data, TestRSA.getPublicKey(userPublicKeyMap.get(userId)),sign);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //验签结束
        System.out.print("验签结果:" + result);
        //结果正确， 进行下面 自己的逻辑业务
        return String.valueOf(result);
    }


    //访问数据msg 结构 {"data"}
    @ApiOperation(value = "模仿用户发送给支付宝之前的数据过程")
    @RequestMapping(value = "/UseRSA" , method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "应用id", required = true, dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "privateKey", value = "私钥", required = true, dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "msg", value = "原始消息json", required = true, dataType = "String",paramType = "query")
    })
    public String UseRSA(String userId,String msg,String privateKey){
        System.out.println("未加密前数据："+msg);
        //先签名 再加密
        // RSA签名
        String sign=null;
        try {
            sign = TestRSA.sign(msg, TestRSA.getPrivateKey(privateKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //签名结束

        //加密数据拼凑
        JSONObject json=new JSONObject();
        json.put("data",msg);
        json.put("sign",sign);
        // RSA加密
        //加密后的文字
        String massage = null;
        try {
            try {
                //这里加密 因为是模仿的第三方用户  所以 这里 数据加密 使用 平台公钥
                massage = TestRSA.encrypt(json.toJSONString(), TestRSA.getPublicKey(selfPublicKey));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("加密后内容:" + massage);
        // RSA加密结束
        JSONObject returnJson=new JSONObject();
        returnJson.put("msg","加密完成");
        returnJson.put("data",massage);
        return returnJson.toJSONString();
    }
}