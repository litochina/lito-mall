package com.gumoxi.gumoximall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.gumoxi.gumoximall.order.config.AlipayTemplate;
import com.gumoxi.gumoximall.order.service.OrderItemService;
import com.gumoxi.gumoximall.order.service.OrderService;
import com.gumoxi.gumoximall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * //获取支付宝POST过来反馈信息
 * 	Map<String,String> params = new HashMap<String,String>();
 * 	Map<String,String[]> requestParams = request.getParameterMap();
 * 	for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
 * 		String name = (String) iter.next();
 * 		String[] values = (String[]) requestParams.get(name);
 * 		String valueStr = "";
 * 		for (int i = 0; i < values.length; i++) {
 * 			valueStr = (i == values.length - 1) ? valueStr + values[i]
 * 					: valueStr + values[i] + ",";
 *                }
 * 		//乱码解决，这段代码在出现乱码时使用
 * 		valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
 * 		params.put(name, valueStr);* 	}
 *
 * 	boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名
 */
//——请在这里编写您的程序（以下代码仅作参考）——

	/* 实际验证过程建议商户务必添加以下校验：
	1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
	2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
	3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
	4、验证app_id是否为该商户本身。
	*/

/**
 * if(signVerified) {//验证成功
 * 		//商户订单号
 * 		String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
 *
 * 		//支付宝交易号
 * 		String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
 *
 * 		//交易状态
 * 		String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
 *
 * 		if(trade_status.equals("TRADE_FINISHED")){
 * 			//判断该笔订单是否在商户网站中已经做过处理
 * 			//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
 * 			//如果有做过处理，不执行商户的业务程序
 *
 * 			//注意：
 * 			//退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
 *                }else if (trade_status.equals("TRADE_SUCCESS")){
 * 			//判断该笔订单是否在商户网站中已经做过处理
 * 			//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
 * 			//如果有做过处理，不执行商户的业务程序
 *
 * 			//注意：
 * 			//付款完成后，支付宝系统发送该交易状态通知
 *        }
 *
 * 		out.println("success");
 ** 	}else {//验证失败
 * 		out.println("fail");
 *
 * 		//调试用，写文本函数记录程序运行情况是否正常
 * 		//String sWord = AlipaySignature.getSignCheckContentV1(params);
 * 		//AlipayConfig.logResult(sWord);* 	}
 *
 * 	//——请在这里编写您的程序（以上代码仅作参考）——
 */
@RestController
public class OrderPayedListener {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    /**
     * 支付宝成功异步通知
     * @param request
     * @return
     */
    @PostMapping("/payed/alipay/notify")
    public String handleAlipayed(PayAsyncVo vo, HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        //只要我们受到了支付宝给我们的异步通知，告诉我们订单支付成功。返回success,支付宝就再也不通知
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String key : parameterMap.keySet()) {
            String value = request.getParameter(key);
            System.out.println("参数名：" + key + "===> 参数值：" + value);
        }
        System.out.println("支付宝通知到位了。。。数据："+parameterMap);

        // 验签

        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        if(signVerified) {//验证成功
            System.out.println("签名验证成功...");
            String result = orderService.handlePayResult(vo);
            return result;
        } else {
            System.out.println("签名验证失败...");
            return "fail";
        }
    }
}
