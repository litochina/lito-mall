package com.gumoxi.gumoximall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.gumoxi.gumoximall.product.entity.ProductAttrValueEntity;
import com.gumoxi.gumoximall.product.service.AttrService;
import com.gumoxi.gumoximall.product.service.ProductAttrValueService;
import com.gumoxi.gumoximall.product.vo.AttrRespVo;
import com.gumoxi.gumoximall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.R;



/**
 * 商品属性
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 08:47:28
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;


    /**
     * 商品管理-spu规格维护 回显
     */
    @GetMapping("/base/listforspu/{spuId}")
    //@RequiresPermissions("product:attr:list")
    public R baseAttrlist(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrlistforspu(spuId);

        return R.ok().put("data", entities);
    }

    /**
     * 列表
     *
     * "/sale/list/{catelogId}"
     * "/base/list/{catelogId}"
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    //@RequiresPermissions("product:attr:list")
    public R baseAttrlist(@RequestParam Map<String, Object> params, @PathVariable Long catelogId, @PathVariable String attrType){
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, attrType);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		//AttrEntity attr = attrService.getById(attrId);
        AttrRespVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 商品管理-规格维护 保存
     */
    @PostMapping("/save/{supId}")
    public R save(@PathVariable Long spuId,
                  @RequestBody  List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId, entities);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
