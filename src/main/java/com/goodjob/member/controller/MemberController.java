package com.goodjob.member.controller;

import com.goodjob.member.Member;
import com.goodjob.member.memDTO.MemberDTO;
import com.goodjob.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * 김도현 22.9.29 작성
 * 김도현 22.10.07 수정 (login 메소드 구조 변경)
**/

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/signUp")
    public String signUpForm(HttpServletRequest request) {
        // 회원가입 시 기존 로그인 상태면 로그아웃 실행
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
        return "member/signup";
    }

    //ID 중복확인
    @ResponseBody
    @RequestMapping(value="/checkId",method = RequestMethod.GET)
    public Long checkIdDuplication(@RequestParam("id") String id) {
        Long result = memberService.countByMemLoginId(id);
        return result;
    }
    //회원가입
    @RequestMapping(value="/signUp",method = RequestMethod.POST)
    public String signUp(@ModelAttribute(name = "memberDTO") MemberDTO memberDTO) {
        //ho - 22.10.17 getMemPw -> getPw (로그인 폼 input name 통일. DTO 필드 loginId,pw 로 통일)
        memberDTO.setPw(passwordEncoder.encode(memberDTO.getPw()));
        Member mem =  memberDTO.toEntity();
        memberService.register(mem);
        return "redirect:/";

    }
    @GetMapping("/login")
    public String loginFrom() {
        return "login";
    }


    @RequestMapping(value="/login",method = RequestMethod.POST)
    public String login( @ModelAttribute(name = "memberDTO") MemberDTO memberDTO, HttpServletRequest request) {
        //ho - 22.10.17 getMemLoginId -> getLoginId (로그인 폼 input name 통일. DTO 필드 loginId,pw 로 통일)
        Optional<Member> mem = memberService.loginIdCheck(memberDTO.getLoginId());

        if (mem.isPresent()) {  //id가 db에 있으면
            Member member = mem.get();
            //ho - 22.10.17 getMemLoginId -> getLoginId (로그인 폼 input name 통일. DTO 필드 loginId,pw 로 통일)
            //70,73,77라인 변경
            if (member.getMemLoginId().equals(memberDTO.getLoginId())) {  //id 가 있으면
                String encodePw = member.getMemPw();
                //암호화된 비밀번호와 로그인 시 입력받은 비밀번호 match 확인
                if (passwordEncoder.matches(memberDTO.getPw(), encodePw)) {
                    //세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
                    HttpSession session = request.getSession();
                    //세션에 로그인 회원 정보 저장
                    session.setAttribute("sessionId", memberDTO.getLoginId());
                    session.setAttribute("Type", "member");

                    return "redirect:/";
                } else {
                    return "redirect:login?error";  //pw가 틀린 경우
                }
            } else {
                return "member/signup";
            }
        } else {
            return "redirect:login?error"; //id가 없는 경우
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/";
    }

}
