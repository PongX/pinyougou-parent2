package com.pinyougou.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//认证类，这个类的主要作用是在登陆后得到用户名，可以根据用户名查询角色或执行一些逻辑
public class UserDetailServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("经过认证类:"+username);

        List<GrantedAuthority> authorities=new ArrayList();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(username,"",authorities);
    }

}