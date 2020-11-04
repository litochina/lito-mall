package com.gumoxi.gumoximall.order.web;

import com.alipay.api.AlipayApiException;
import com.gumoxi.gumoximall.order.config.AlipayTemplate;
import com.gumoxi.gumoximall.order.service.OrderService;
import com.gumoxi.gumoximall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    /**
     * 1、将支付业务让浏览器展示
     * 2、支付成功之后，我们跳转到订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

//        PayVo payVo = new PayVo();
//        payVo.setBody("");//订单备注
//        payVo.setOut_trade_no();//订单号
//        payVo.setSubject();//订单主题
//        payVo.setTotal_amount();
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }
}
