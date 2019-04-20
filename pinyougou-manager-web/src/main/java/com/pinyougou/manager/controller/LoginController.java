package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/login")
@RestController//相当于controller注解和response注解的合体，response注解可以直接返回json数据回去
public class LoginController {

    @RequestMapping("/name")
    public Map name(){
        //通过安全框架获得当前的用户名：
        String name= SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();
        map.put("loginName", name);
        return map ;
    }
}
