package com.gumoxi.gumoximall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gumoxi.gumoximall.common.utils.HttpUtils;
import com.gumoxi.gumoximall.member.dao.MemberLevelDao;
import com.gumoxi.gumoximall.member.entity.MemberLevelEntity;
import com.gumoxi.gumoximall.member.exception.PhoneExistException;
import com.gumoxi.gumoximall.member.exception.UsernameExistException;
import com.gumoxi.gumoximall.member.vo.MemberLoginVo;
import com.gumoxi.gumoximall.member.vo.MemberRegistVo;
import com.gumoxi.gumoximall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;

import com.gumoxi.gumoximall.member.dao.MemberDao;
import com.gumoxi.gumoximall.member.entity.MemberEntity;
import com.gumoxi.gumoximall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelDao memberLevelDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao dao = this.baseMapper;
        MemberEntity entity = new MemberEntity();
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        // 检查手机号和用户名是否唯一
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());
        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUsername());
        entity.setNickname(vo.getUsername());
        //密码要加密存储

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        //其他默认信息

        dao.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if(count > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        // SELECT * FROM mms_member WHERE username=? OR mobile=?
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if(memberEntity == null) {
            // 登录失败
            return null;
        } else {
            // 1、获取到数据库的password
            String passwordDb = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            // 2、密码匹配
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if(matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
        // 登录和注册合并逻辑
        String uid = socialUser.getUid();
        // 判断当前社交用户是否已经登录过系统
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(memberEntity != null) {
            // 这个用户已经注册过了
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            memberDao.updateById(memberEntity);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        } else {
            // 2、没有查到当前社交用户对应的记录，我们就需要注册一个
            MemberEntity regist = new MemberEntity();

           try{
               // 3、查找当前社交用户的社交账号信息（昵称、性别等）
               Map<String, String> query = new HashMap<>();
               query.put("access_token", socialUser.getAccess_token());
               query.put("uid", socialUser.getUid());
               HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
               if(response.getStatusLine().getStatusCode() == 200) {
                   String json = EntityUtils.toString(response.getEntity());
                   JSONObject jsonObject = JSON.parseObject(json);
                   String name = jsonObject.getString("name");
                   String gender = jsonObject.getString("gender");
                   regist.setNickname(name);
                   regist.setGender("m".equals(gender)?1:0);
               }
           }catch (Exception e) {}

            regist.setSocialUid(uid);
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            memberDao.insert(regist);
            return regist;
        }
    }

}