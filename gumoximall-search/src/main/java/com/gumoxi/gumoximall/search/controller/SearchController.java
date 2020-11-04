package com.gumoxi.gumoximall.search.controller;

import com.gumoxi.gumoximall.search.service.MallSearchService;
import com.gumoxi.gumoximall.search.vo.SearchParam;
import com.gumoxi.gumoximall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {
    /**
     * 自动将页面传递过来的所有请求查询参数封装成指定对象
     */
    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) {

        searchParam.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
