package com.goodjob;

import com.goodjob.admin.interceptor.AdminLoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 10.5 인터셉터 설정 클래스. By.OH
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private String connectPath = "/Users/oh/Desktop/goodjobimg/**";
    private String resourcePath = "file:///Users/oh/Desktop/goodjobimg/";
//    private String connectPath = "/Users/kesia/Desktop/goodjobimg/**";
//    private String resourcePath = "file:///Users/kesia/Desktop/goodjobimg/";

//    private String connectPath = "C:\\Users\\pc\\Desktop\\Coding\\project\\GOODJOB\\goodjobimg/**";
//    private String resourcePath = "file:///C:\\Users\\pc\\Desktop\\Coding\\project\\GOODJOB\\goodjobimg";

//    private String connectPath = "C:\\Users\\woon1\\Desktop\\goodjobimg\\**";
//    private String resourcePath = "file:///C:\\Users\\woon1\\Desktop\\goodjobimg";

    //인터셉터 추가 메소드
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new AdminLoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/admin/**") // 인터셉터를 적용할 URI
                .excludePathPatterns("/css/**", "/*.ico", "/error", "/admin/login","/assets/**","/*.css","/*.js"); // 제외 URI

        registry.addInterceptor(new LoginInterceptor())
                .order(2)
                .addPathPatterns("/member/**")
                .excludePathPatterns("/css/**","/js/**","/login","/*.css","/*.js","/assets/**",
                        "/member/login","/member/signUp","/member/checkId","/member/signupEmail","/member/checkEmail"
                        ,"/member/sendPw");

        registry.addInterceptor(new LoginInterceptor())
                .order(3)
                .addPathPatterns("/com/**")  //세션 없으면 로그인 페이지로
                .excludePathPatterns("/login","/*.css","/*.js","/assets/**","/css/**")  //세션 없어도 돌아가도록
                .excludePathPatterns("/com/emailCheck","/com/login","/com/findId","/com/check","/com/signup","/com/update")
                .excludePathPatterns("/com/emailCheck2");

        registry.addInterceptor(new LoginInterceptor())
                .order(4)
                .addPathPatterns("/resume/**","/status/**") // 세션필요한
                .excludePathPatterns("/css/**","/js/**","/login","/*.css","/*.js","/assets/**");  //세션 없는

        registry.addInterceptor(new LoginInterceptor())
                .order(5)
                .addPathPatterns("/post/**") // 세션필요한
                .excludePathPatterns("/post/file/**","/post/readPost/**");  //세션 없는
    }
    // 썸머노트(텍스트 에디터)에 대한 요청 응답 핸들러
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(connectPath)
                .addResourceLocations(resourcePath);
    }
}
