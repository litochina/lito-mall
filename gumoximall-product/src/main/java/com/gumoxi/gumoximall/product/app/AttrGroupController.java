package com.gumoxi.gumoximall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.gumoxi.gumoximall.product.entity.AttrEntity;
import com.gumoxi.gumoximall.product.entity.AttrGroupEntity;
import com.gumoxi.gumoximall.product.service.AttrAttrgroupRelationService;
import com.gumoxi.gumoximall.product.service.AttrGroupService;
import com.gumoxi.gumoximall.product.service.AttrService;
import com.gumoxi.gumoximall.product.service.CategoryService;
import com.gumoxi.gumoximall.product.vo.AttrGroupRelationVo;
import com.gumoxi.gumoximall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.R;



/**
 * 属性分组
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 08:47:28
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/{catelogId}/withattr")
    //@RequiresPermissions("product:attrgroup:list")
    public R attrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        // 1、查询当前分类下的所有属性分组
        // 2、查出每个分组的所有属性
        List<AttrGroupWithAttrsVo> entities = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", entities);
    }

    /**
     * 列表
     */
    @RequestMapping("/{attrgroupId}/attr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        //PageUtils page = attrGroupService.queryPage(params);
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }


    /**
     * 列表
     */
    @RequestMapping("/{attrgroupId}/noattr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R attrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrService.getRelationNoAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catalogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catalogId);
        attrGroup.setCatelogPath(catelogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }


    /**
     * 删除
     */
    @PostMapping("/attr/relation/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos){
        attrService.deleteRelation(vos);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
