/**
 * 22.10.28 마이페이지 컨트롤러 분리
 */
package com.goodjob.company.controller;

import com.goodjob.company.Company;
import com.goodjob.company.dto.CompanyDTO;
import com.goodjob.company.service.CompanyService;
import com.goodjob.member.service.MemberService;
import com.goodjob.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/com")
@RequiredArgsConstructor
@Log4j2
public class CompanyMyPageController {

    private final CompanyService companyService;

    private final PasswordEncoder passwordEncoder;

    private final MemberService memberService;

    //22.11.07 - 회원정보 수정용 이메일 중복확인.
    @ResponseBody
    @RequestMapping(value = "emailCheck2", method = RequestMethod.POST)
    public List<String> emailCheck2(@RequestParam("emailCheck") String email,HttpSession session){

        Company com = getSessionLoginId(session);
        String dbEmail = com.getComEmail();

        List<String> result2 = new ArrayList();

        if(dbEmail.equals(email)) {
            result2.add("mine");
        } else {
            String result = memberService.checkEmail(email);
            if(result == "com") {
                result2.add("com");
            } else if(result == "mem") {
                result2.add("mem");
            } else {
                result2.add("null");
            }
        }

        return  result2;
    }

    @GetMapping("/myPageApplier/{postId}")
    public String myPageApplierList(@PathVariable("postId") Long postId, Model model){
        model.addAttribute("postId", postId);
        model.addAttribute("postTitle", companyService.getPostTitle(postId));
        return "/company/companyMyPageResumeApplyList";
    }

    @GetMapping("/myPageInterviewee/{postId}")
    public String myPageIntervieweeList(@PathVariable("postId") Long postId, Model model){
        model.addAttribute("postId", postId);
        model.addAttribute("postTitle", companyService.getPostTitle(postId));
        return "/company/companyMyPageIntervieweeList";
    }

    //ho - 22.10.17 마이페이지 세션 넘기기+
    @GetMapping("/myPage")
    public String companyMyPage(HttpSession session, Model model){

        Company com = getSessionLoginId(session);
        CompanyDTO companyInfo = companyService.entityToDTO2(com);
        model.addAttribute("companyInfo",companyInfo);

//        log.info(companyInfo.getComRegCode().getRegName());
        return "/company/companyMyPage";
    }

    //ho - 22.10.19 기업정보 수정하기(업데이트 버전)
    @PostMapping("/update")
    public String companyInfoUpdate(CompanyDTO companyInfo) {
        companyService.companyInfoUpdate(companyInfo);
        return "redirect:/com/myPage";
    }

    //ho - 22.10.20 기업정보 수정하기 전 비밀번호 확인
    @ResponseBody
    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public int passwordConfirm(CompanyDTO companyInfo, HttpSession session, @RequestParam("pw") String password) throws Exception {
        //log.info("===========로그인 아이디 받아오나? : "+companyInfo.getLoginId());
        return matchPassword(session, password);
    }

    //ho - 2022.10.25 회원 탈퇴 ajax
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String deleteCompany(HttpSession session, @RequestParam("pw") String password) {

        Company com = getSessionLoginId(session);
        Long comId = com.getComId();

        if(password == null || !passwordEncoder.matches(password,com.getComPw())){
            return "0";
        } else {
//            companyService.delete(comId);
//            session.invalidate();  //세션 만료시키기.
            return "1";
        }
    }

    //22.11.07 회원탈퇴 전 한번더 묻기.
    @GetMapping(value = "/deleteConfirm")
    public String deleteCompanyConfirm(HttpSession session) {
        Company com = getSessionLoginId(session);
        Long comId = com.getComId();
        companyService.delete(comId);
        session.invalidate();  //세션 만료시키기.

        return "redirect:/";
    }

    //ho - 22.10.28 - 비밀번호 변경 ajax
    @ResponseBody
    @RequestMapping(value = "/changePw", method = RequestMethod.POST)
    public int passwordChangeConfirm(CompanyDTO companyInfo, HttpSession session, @RequestParam("pw") String password) throws Exception {
        //log.info("===========로그인 아이디 받아오나? : "+companyInfo.getLoginId());
        return matchPassword(session, password);
    }


    //ho - 22.10.28 회원 비밀번호 변경 페이지
    @GetMapping("/changePassword")
    public String changePasswordForm(){
        return "/company/changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(@Valid CompanyDTO companyDTO, BindingResult result, HttpSession session){
        if(!companyDTO.getPw().equals(companyDTO.getComPw2())){
            result.rejectValue("comPw2","passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "/company/changePassword";
        }

        String comLoginId = getSessionLoginId(session).getComLoginId();

        companyService.changePw(companyDTO,comLoginId);

        return "redirect:/com/myPage";
    }

    //ho 22.10.28 session에서 loginId 받아오는거 반복 되서 메서드로 만듦.
    private Company getSessionLoginId(HttpSession session) {
        String sessionId = (String) session.getAttribute("sessionId");
        Optional<Company> company = companyService.loginIdCheck(sessionId);
        Company com = company.get();
        return com;
    }

    //ho 22.10.28 비밀번호 확인 로직 반복 되서 메서드로 만듦.
    private int matchPassword(HttpSession session, @RequestParam("pw") String password) {

        String comPw = getSessionLoginId(session).getComPw();

        if(password == null || !passwordEncoder.matches(password,comPw)) {
            return 0;
        } else {
            return 1;
        }
    }
}
