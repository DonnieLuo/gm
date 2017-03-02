package com.controller;

/**
 * Created by Donnie on 2017/2/17.
 */

import com.Entity.msg.Article;
import com.Entity.msg.MpNews;
import com.Entity.msg.MpNewsMsg;
import com.google.gson.Gson;
import com.repository.LogRepository;
import com.repository.UserRepository;
import com.util.GsonUtil;
import com.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class LoginController{

    @Autowired
    private LogRepository logRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    private Gson gson = GsonUtil.getInstance();
    public static String gmTokenUrl = "http://localhost:8080/oauth/token?grant_type=password&client_id=appclient&client_secret=123456&username=USERNAME&password=PASSWORD";

    @RequestMapping("/login")
    public String login() {
        return "login";
    }
    @RequestMapping("/test")
    public String test() {
        return "test";
    }
    @RequestMapping("/notoken")
    public String test2() {
        return "test2";
    }

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public String auth(@RequestParam String username, @RequestParam String password) {

            try {
//                log.info("id:{}",userRepository.findByUsername(username).getId());
                Authentication authentication = tryToAuthenticateWithUsernameAndPassword(username, password);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (BadCredentialsException e) {
                log.info(e.toString());
            }

        log.debug("------/auth-debug");
//        return "sendLog";
        return "redirect:/oauth/token";
    }

    @RequestMapping(value = "/gmtoken", method = RequestMethod.GET)
    public @ResponseBody String getToken(HttpServletRequest request) throws Exception {
        String username = request.getParameter("user");
        String password = request.getParameter("pass");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            log.error("------ request format: /oauth/token?user=USERNAME&pass=PASSWORD");
            throw new Exception("");
        }
        String url = gmTokenUrl.replace("USERNAME", username).replace("PASSWORD", password);
        return UrlUtil.urlPost2(url, "");
    }


    private Authentication tryToAuthenticateWithUsernameAndPassword(String username, String password) {
        UsernamePasswordAuthenticationToken requestAuthentication = new UsernamePasswordAuthenticationToken(username, password);
        return tryToAuthenticate(requestAuthentication);
    }
    private Authentication tryToAuthenticate(Authentication requestAuthentication) {
        Authentication responseAuthentication = authenticationManager.authenticate(requestAuthentication);
        if (responseAuthentication == null || !responseAuthentication.isAuthenticated()) {
            throw new InternalAuthenticationServiceException("auth failed");
        }
        return responseAuthentication;
    }
    @RequestMapping("/send/news")
    public String sendWechat() throws Exception {
        String accessToken = UrlUtil.getAccessToken();
        MpNewsMsg msg = new MpNewsMsg();

        Article article = new Article(UrlUtil.upload("C:\\Users\\Donnie\\Desktop\\7c739d6.jpg",accessToken, "image" ),"【外盘日讯】 特朗普演说反应正面 ：美联储3月加息机率暴增至66.4%");
        article.setDigest("this is the digest");
        article.setContent("作者：芝商所特约评论员寇健<br><br>市场对昨天晚上特朗普总统在国会的演说表现了非常正面的反应。<br><br>芝商所联邦储备银行观测站 (FedWatch Tool) 数据显示，3月份联邦储备银行 FOMC会议加息的可能性从昨天的 35.4% 增加到今天的 66.4%。");
        article.setShow_cover_pic(1);

        MpNews mpNews = new MpNews();
        List<Article> articleList = new ArrayList<Article>();
        articleList.add(article);
        mpNews.setArticles(articleList);

        msg.setTouser("@all");
        msg.setMsgtype("mpNews");
        msg.setAgentid(0);
        msg.setMpnews(mpNews);

        String jsonContent = gson.toJson(msg);
        String sendUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;
        log.debug("--------------------------jsonMsg:{}",jsonContent);
        return UrlUtil.urlPost(sendUrl, jsonContent);
    }
    @RequestMapping(value = "/img",method = RequestMethod.GET)
    public String selectImg() {
        return "uploadImg";
    }
    @RequestMapping(value = "/img/upload", method = RequestMethod.POST)
    public String postImg(@RequestParam(value = "media") MultipartFile img) throws Exception {
        String result = uploadImg(img);
        return result;
    }
    public String uploadImg(MultipartFile img) throws Exception {
        String accessToken = UrlUtil.getAccessToken();
        String url = "https://qyapi.weixin.qq.com/cgi-bin/media/uploadimg?access_token="+accessToken;
        StringBuilder head = new StringBuilder("\r\n");
        String fileUrl = "C:\\Users\\Donnie\\Desktop\\"+img.getOriginalFilename();

        String mediaId = UrlUtil.upload(fileUrl, UrlUtil.getAccessToken(), "image");

        return mediaId;
    }

}
