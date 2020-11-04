package com.gumoxi.gumoximall.search.service;

import com.gumoxi.gumoximall.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException;
}
