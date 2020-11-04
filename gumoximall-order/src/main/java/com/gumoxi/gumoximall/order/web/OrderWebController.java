package com.gumoxi.gumoximall.order.web;

import com.gumoxi.gumoximall.common.exception.NoStockException;
import com.gumoxi.gumoximall.order.service.OrderService;
import com.gumoxi.gumoximall.order.vo.OrderConfirmVo;
import com.gumoxi.gumoximall.order.vo.OrderSubmitVo;
import com.gumoxi.gumoximall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    /**
     * 下单功能
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {

        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if(responseVo.getCode()== 0) {
                // 下单成功，到支付选择也
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            } else {
                // 下单失败回到订单确认也重新确认订单信息
                String msg = "下单失败；";
                switch (responseVo.getCode()) {
                    case 1 : msg += "订单信息过期，请刷新再次提交"; break;
                    case 2 : msg += "订单商品价格发生变化，请确认后再次提交"; break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.gumoxi.com/toTrade";
            }
        } catch (NoStockException e) {
            String msg = "下单失败；库存锁定失败，商品库存不足";
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gumoxi.com/toTrade";
        } catch (Exception e) {
            String msg = e.getMessage();
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gumoxi.com/toTrade";
        }

    }
}
