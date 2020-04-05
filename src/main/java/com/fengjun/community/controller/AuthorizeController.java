package com.fengjun.community.controller;

import com.fengjun.community.dto.AccessTokenDTO;
import com.fengjun.community.dto.GithubUser;
import com.fengjun.community.mapper.UserMapper;
import com.fengjun.community.model.User;
import com.fengjun.community.provider.GithubProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;

@Controller
public class AuthorizeController {
    @Resource
    private GithubProvider githubProvider;
    @Resource
    private UserMapper userMapper;

    @Value("${github.client.id}")
    private String clientId;
    @Value("${github.client.secret}")
    private String clientSecret;
    @Value("${github.redirect.uri}")
    private String redirectUri;

    @GetMapping(value = "/callback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletRequest request,
                           ModelMap modelMap){

        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(state);
        String accessToken = githubProvider.getAccessTokenDTO(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        System.out.println("callback 拿到user："+githubUser);

        //判断是否取得user
        if(githubUser != null){
            //将user存入session中
            request.getSession().setAttribute("user",githubUser);

            //将用户信息存入数据库
            User user = new User();
            user.setToken(UUID.randomUUID().toString());
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setGmtCreate(new Date());
            user.setGmtModified(new Date());
            userMapper.insertUser(user);
        }
        modelMap.put("msg","测试");
        return "index";
    }
}
