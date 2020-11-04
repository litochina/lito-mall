package com.gumoxi.gumoximall.ware.vo;

import lombok.Data;

import java.util.List;
@Data
public class PurchaseDoneVo {
    private Long purchaseId;
    private List<PurchaseItemDoneVo> items;
}
