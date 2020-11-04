package com.gumoxi.gumoximall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.gumoxi.gumoximall.common.constant.ProductConstant;
import com.gumoxi.gumoximall.common.to.SkuHasStockTo;
import com.gumoxi.gumoximall.common.to.SkuReductionTo;
import com.gumoxi.gumoximall.common.to.SpuBoundTo;
import com.gumoxi.gumoximall.common.to.es.SkuEsModel;
import com.gumoxi.gumoximall.common.utils.R;
import com.gumoxi.gumoximall.product.dao.SpuInfoDao;
import com.gumoxi.gumoximall.product.entity.*;
import com.gumoxi.gumoximall.product.feign.CouponFeignService;
import com.gumoxi.gumoximall.product.feign.SearchFeignService;
import com.gumoxi.gumoximall.product.feign.WareFeignService;
import com.gumoxi.gumoximall.product.service.*;
import com.gumoxi.gumoximall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService imagesService;

    @Autowired
    private ProductAttrValueService attrValueService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * //TODO 高级部分完善
     * SPU STANDARD PRODUCT UNIT  标准产品单位
     * SKU STOCK KEEPING UNIT  库存保有单位
     * @GlobalTransactional
     * @param vo
     */

    @Transactional
    @Override
    // SEATA AT 分布式事务
    public void saveSupInfo(SpuSaveVo vo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();

        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);



        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(),images);


        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);


        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }


        //5、保存当前spu对应的所有sku信息；

        List<Skus> skus = vo.getSkus();
        if(skus!=null && skus.size()>0){
            skus.forEach(item->{
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }



            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public void up(Long spuId) {
        // 1、查找supId下的所有sku,品牌的名字
        List<SkuInfoEntity> entities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = entities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // TODO 4、查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());


        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<Long>(searchAttrIds);
        List<SkuEsModel.Attr> attrs = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());


        // TODO 1、查询库存是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkuHasStock(skuIds);
            TypeReference<List<SkuHasStockTo>> typeReference = new TypeReference<List<SkuHasStockTo>>(){};
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }

        // 2、组装每个sku封装成一个SkuEsModel
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> collect = entities.stream().map(sku -> {
            SkuEsModel elasticsearchModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, elasticsearchModel);
            // skuPrice, skuImg
            elasticsearchModel.setSkuPrice(sku.getPrice());
            elasticsearchModel.setSkuImg(sku.getSkuDefaultImg());
            // hasStock hotScore

            if(finalStockMap == null) {
                elasticsearchModel.setHasStock(false);
            } else {
                elasticsearchModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            // TODO 2、热度评分，默认初始 0 比较复杂
            elasticsearchModel.setHotScore(0L);
            // TODO 3、查询品牌和分类的信息
            BrandEntity brand = brandService.getById(elasticsearchModel.getBrandId());
            elasticsearchModel.setBrandName(brand.getName());
            elasticsearchModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(elasticsearchModel.getCatalogId());
            elasticsearchModel.setCatalogName(category.getName());
            elasticsearchModel.setAttrs(attrs);
            return elasticsearchModel;
        }).collect(Collectors.toList());
        // TODO 5、发送ES对商品进行上架

        R r = searchFeignService.productStatusUp(collect);
        if(r.getCode() == 0) {
            // 上架成功
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 上架失败
            // TODO 7 、重复调用？接口幂等;重试机制
            /**
             * 1、构造请求数据，将对象转为json
             * RestTemplate template = buildTemplateFromArgs.create(argv);
             *
             * 2、发送请求进行执行（执行成功会解码响应数据）
             * executeAndDecode(template);
             *
             * 3、执行请求会有重试机制
             *
             */
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spu = getById(spuId);
        return spu;
    }

}