package com.gumoxi.gumoximall.search.service;

import com.gumoxi.gumoximall.search.vo.SearchParam;
import com.gumoxi.gumoximall.search.vo.SearchResult;

public interface MallSearchService {

    /**
     *
     * @param searchParam 检索参数
     * @return 检索结果，里边包含所有页面需要的信息
     */
    public SearchResult search(SearchParam searchParam);
}
