package com.goodjob.resume;

import com.goodjob.career.dto.CareerDTO;
import com.goodjob.career.service.CareerService;
import com.goodjob.certification.CertificateName;
import com.goodjob.certification.dto.CertificationDTO;
import com.goodjob.certification.service.CertificationService;
import com.goodjob.education.MajorName;
import com.goodjob.education.SchoolName;
import com.goodjob.education.dto.EducationDTO;
import com.goodjob.education.service.EducationService;
import com.goodjob.member.MemberDTO;
import com.goodjob.member.MemberService;
import com.goodjob.selfIntroduction.service.SelfIntroductionService;
import com.goodjob.selfIntroduction.dto.SelfIntroductionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 박채원 22.10.02 작성
 */

@Controller
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final MemberService memberService;
    private final EducationService educationService;
    private final CareerService careerService;
    private final CertificationService certificationService;
    private final SelfIntroductionService selfIntroductionService;

    private String loginId = "memberId2";                           //세션 처리 후 세션에서 가져올 값


    @GetMapping("/myInfo")
    public String registerButton(){
        return "ResumeRegisterButton";
    }

    @ResponseBody
    @GetMapping("/registerResume")
    public Long registerResume(){
        return resumeService.registerResume(loginId);
    }

    @GetMapping("resumeStep1/{resumeId}")
    public String resumeStep1(@PathVariable("resumeId") Long resumeId, Model model){
        MemberDTO memberDTO = memberService.bringMemInfo(loginId);

        model.addAttribute("resumeId", resumeId);
        model.addAttribute("memberInfo", memberDTO);
        return "ResumeStep1";
    }

    //학교 검색할 때 AJAX에서 사용하는 메소드
    @ResponseBody
    @GetMapping("/findSchool/{schoolName}")
    public List<SchoolName> findSchool(@PathVariable("schoolName") String schoolName){
        return educationService.findSchoolName(schoolName);
    }

    //전공 검색할 때 AJAX에서 사용하는 메소드
    @ResponseBody
    @GetMapping("/findMajor/{majorName}")
    public List<MajorName> findMajor(@PathVariable("majorName") String majorName){
        return educationService.findMajorName(majorName);
    }

    //자격증 검색할 때 AJAX에서 사용하는 메소드
    @ResponseBody
    @GetMapping("/findCerti/{certiName}")
    public List<CertificateName> findCerti(@PathVariable("certiName") String certiName){
        return certificationService.findCertiName(certiName);
    }

    @PostMapping("/resumeStep2/{resumeId}")
    public String resumeStep2(@PathVariable("resumeId") Long resumeId, MemberDTO memberDTO, EducationDTO educationDTO, Model model){
        resumeService.updateResumeMemberInfo(memberDTO, resumeId);
        educationService.registerSchoolInfo(educationDTO);

        if(certificationService.existOrNotResumeId(resumeId) == 1){
            model.addAttribute("resumeId", resumeId);
            model.addAttribute("certiInfo", certificationService.bringCertiInfo(resumeId));
            model.addAttribute("careerInfo", careerService.bringCareerInfo(resumeId));
            return "ResumeStep2WithContent";
        }
        model.addAttribute("resumeId", resumeId);
        return "ResumeStep2";
    }
    
    @PostMapping("/resumeStep3/{resumeId}")
    public String resumeStep3(@PathVariable("resumeId") Long resumeId, CertificationDTO certificationDTO, CareerDTO careerDTO, Model model){
        certificationService.registerCertiInfo(certificationDTO);
        careerService.registerCareerInfo(careerDTO);

        if(selfIntroductionService.existOrNotResumeId(resumeId) == 1){
            model.addAttribute("resumeId", resumeId);
            model.addAttribute("selfIntroInfo", selfIntroductionService.bringSelfIntroInfo(resumeId));
            return "ResumeStep3WithContent";
        }

        model.addAttribute("resumeId", resumeId);
        return "ResumeStep3";
    }

    @PostMapping("/submitResume")
    public String submitResume(SelfIntroductionDTO selfIntroductionDTO){
        selfIntroductionService.registerSelfInfo(selfIntroductionDTO);
        return "redirect:/myInfo";
    }

    @PostMapping("/goPreviousStep1/{resumeId}")
    public String goPreviousStep1(@PathVariable("resumeId") Long resumeId, CertificationDTO certificationDTO, CareerDTO careerDTO, Model model){
        certificationService.registerCertiInfo(certificationDTO);
        careerService.registerCareerInfo(careerDTO);

        model.addAttribute("resumeId", resumeId);
        model.addAttribute("memberInfo", memberService.bringMemInfo(loginId));
        model.addAttribute("resumeMemInfo", resumeService.bringResumeInfo(resumeId));
        model.addAttribute("schoolInfo", educationService.bringSchoolInfo(resumeId));
        return "ResumeStep1WithContent";
    }

    @PostMapping("/goPreviousStep2/{resumeId}")
    public String goPreviousStep2(@PathVariable("resumeId") Long resumeId, SelfIntroductionDTO selfIntroductionDTO, Model model){
        selfIntroductionService.registerSelfInfo(selfIntroductionDTO);

        model.addAttribute("resumeId", resumeId);
        model.addAttribute("certiInfo", certificationService.bringCertiInfo(resumeId));
        model.addAttribute("careerInfo", careerService.bringCareerInfo(resumeId));
        return "ResumeStep2WithContent";
    }
}