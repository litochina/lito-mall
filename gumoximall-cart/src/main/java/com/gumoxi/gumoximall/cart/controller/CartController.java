package com.gumoxi.gumoximall.cart.controller;

import com.gumoxi.gumoximall.cart.interceptor.CartInterceptor;
import com.gumoxi.gumoximall.cart.service.CartService;
import com.gumoxi.gumoximall.cart.vo.Cart;
import com.gumoxi.gumoximall.cart.vo.CartItem;
import com.gumoxi.gumoximall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/currentMemberCartItems")
    @ResponseBody
    public List<CartItem> currentMemberCartItems() {
        List<CartItem> cartItems = cartService.getCurrentMemberCartItems();
        return cartItems;
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gumoxi.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gumoxi.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check){
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gumoxi.com/cart.html";
    }

    /**
     * 浏览器有个cookie,user-key:表示用户身份，一个月后过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     * 浏览器保存以后，每次访问都会带上这个cookie
     *
     * 登录 session有
     * 没登录：按照cookie里面的user-key来做。
     * 第一次：如果没有临时用户，帮忙创建一个临时用户
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     *
     * RedirectAttributes ra
     *      ra.addFlashAttribute();将数据放在session里面，可以在页面中取出，但只能取一次
     *      ra.addAttribute();将数据放入在url后面
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes ra) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addToCart(skuId, num);

//        model.addAttribute("skuId", skuId);
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.gumoxi.com/addToCartSuccess.html";
    }

    /**
     * 跳转道成功也
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.getCartItem(skuId);

        model.addAttribute("item", cartItem);
        return "success";
    }
}
