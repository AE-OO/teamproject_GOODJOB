package com.goodjob.post.controller;

import com.goodjob.bookmark.service.BookMarkService;
import com.goodjob.company.Company;
import com.goodjob.company.service.CompanyService;
import com.goodjob.post.Post;
import com.goodjob.post.error.SessionCompanyAccountNotFound;
import com.goodjob.post.error.SessionNotFoundException;
import com.goodjob.post.fileupload.FileService;
import com.goodjob.post.postdto.*;
import com.goodjob.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

@Log4j2
@Controller
@RequestMapping(value = {"/post"})
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final BookMarkService bookMarkService;
    private final FileService fileService;
    private final CompanyService companyService;

    @GetMapping("/savePost")
    public String postSaveForm(String redirectedFrom, HttpServletRequest httpServletRequest, Model model) {

        String sessionId = getSessionInfo(httpServletRequest, "sessionId");
        if (sessionId != null) {
            model.addAttribute("comInfo", postService.getComInfo(sessionId)); // CompanyInfoDTO(회사 주소+이름+사업번호+구분)
        } else {
            throw new SessionNotFoundException();
        }
        model.addAttribute("occList", postService.getListOccupation()); // 직업 리스트
        model.addAttribute("salaryList", postService.getListSalary()); // 연봉대 리스트
        model.addAttribute("redirectedFrom", redirectedFrom);
        return "/post/postInsertForm";
    }

    @PostMapping(value = "/savePost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> postSave(@Valid @ModelAttribute PostInsertDTO postInsertDTO, HttpServletRequest httpServletRequest) throws IOException {
        String sessionId = getSessionInfo(httpServletRequest,"sessionId");
        if(sessionId != null){
            postInsertDTO.setComLoginId(sessionId);
        } else {
            throw new SessionNotFoundException();
        }
        Long savedPostId = postService.savePost(postInsertDTO);
        if (savedPostId != null) {
            SuccessfulPostVo sp = new SuccessfulPostVo();
            sp.setId(savedPostId);
            return new ResponseEntity<>(sp, HttpStatus.OK);
        } else {
            return null;
        }
    }

    @GetMapping(value = {"/updatePost/{postId}"})
    public String postUpdate(@PathVariable(name = "postId") Long postId, String redirectedFrom, HttpServletRequest httpServletRequest, Model model) {
        String sessionId = getSessionInfo(httpServletRequest, "sessionId");
        if (sessionId != null) {
            model.addAttribute("comInfo", postService.getComInfo(sessionId)); // CompanyInfoDTO(회사 주소+이름+사업번호+구분)
        } else {
            throw new SessionNotFoundException();
        }
        PostInsertDTO postInsertDTO = postService.getPostById(postId);
        model.addAttribute("comInfo", postService.getComInfo(sessionId));
        model.addAttribute("dto", postInsertDTO);
        model.addAttribute("occList", postService.getListOccupation()); // 직업 리스트
        model.addAttribute("salaryList", postService.getListSalary()); // 연봉대 리스트
        return "/post/postUpdateForm";
    }


    // 기업 마이 페이지 공고관리 처리 메소드
    @GetMapping(value = {"/comMyPagePost"})
    public String comMyPagePost(PageRequestDTO pageRequestDTO, HttpServletRequest httpServletRequest, Model model) {
        String sessionType = getSessionInfo(httpServletRequest, "Type");
        if (sessionType != null) {
            pageRequestDTO.setAuthType(getSessionInfo(httpServletRequest, "Type"));
            pageRequestDTO.setAuth(getSessionInfo(httpServletRequest, "sessionId"));
        } else {
            throw new SessionNotFoundException();
        }
        PageResultDTO<Post, PostComMyPageDTO> result = postService.getPagingPostListInComMyPage(pageRequestDTO);
        model.addAttribute("occList", postService.getListOccupation()); // 직업 리스트
        model.addAttribute("salaryList", postService.getListSalary()); // 연봉대 리스트
        model.addAttribute("result", result);
        model.addAttribute("sessionLoginId", getSessionInfo(httpServletRequest, "sessionId"));
        return "/post/comMyPagePost";
    }

    // postId 값으로 개별 공고를 삭제하는 메소드
    @DeleteMapping(value = {"/deletePost/{postId}"})
    @ResponseBody
    @Transactional
    public ResponseEntity<String> deletePost(@PathVariable(name = "postId") Long postId) {
        postService.deletePost(postId);
        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }

    // postId 값으로 개별 공고를 조회하는 페이지로 이동하는 메소드
    @GetMapping(value = {"/readPost/{postId}"})
    @Transactional
    public String readPost(@PathVariable(name = "postId") Long postId, PageRequestDTO pageRequestDTO, Model model, @SessionAttribute(required = false, name = "sessionId") String sessionId, HttpServletRequest httpServletRequest) throws IOException {
        // 오성훈 추가
        boolean existsBookmarkByMember = bookMarkService.existsPostByMember(sessionId, postId);
        model.addAttribute("existsBookmarkByMember", existsBookmarkByMember);
        boolean existsCompanyOfPostIn = existsCompanyOfPostIn(postId);
        model.addAttribute("existsPostInCompany",existsCompanyOfPostIn);
        boolean existsPostByComLoginId = postService.existsPostByPostIdAndPostComId(postId,companyService.loginIdCheck(sessionId).orElse(null));
        model.addAttribute("existsPostByComLoginId",existsPostByComLoginId);

        PostDetailsDTO postDetailsDTO = postService.readPost(postId);
        model.addAttribute("dto", postDetailsDTO);
        return "/post/postDetailViewWithMap";
    }

    @GetMapping(value = {"/file/{fileName}"})
    public ResponseEntity<?> getFile(@PathVariable String fileName) throws IOException {
        return fileService.getFile(fileName);
    }

    // HttpServletRequest 에서 두번째 파라미터 값에 따라 Session 정보를 리턴해주는 메소드.
    // @param typeOrSessionId
    // "type" -> "user" or "company"
    // "sessionId" -> 유저나 기업회원 로그인 ID 값
    private String getSessionInfo(HttpServletRequest httpServletRequest, String typeOrSessionId) {
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null && httpSession.getAttribute("Type").toString().equals("company")) {
            // 세션 타입 체크
            if (typeOrSessionId.equals("Type")) {
                System.out.println((String) httpSession.getAttribute("Type"));
                System.out.println(httpSession.getAttribute("Type").toString());
                return httpSession.getAttribute("Type").toString();
            } else if (typeOrSessionId.equals("sessionId")) {
                System.out.println((String) httpSession.getAttribute("sessionId"));
                System.out.println(httpSession.getAttribute("sessionId").toString());
                return httpSession.getAttribute("sessionId").toString();
            } else {
                throw new SessionCompanyAccountNotFound();
            }
        } else {
            return null;
        }
    }

    // 22.11.09 오성훈 임시추가. 삭제된 공고인지 확인하는 메소드
    private boolean existsCompanyOfPostIn(Long postId) {
        Optional<Post> findOne = postService.findOne(postId);
        if (findOne.isPresent()) {
            Company postComId = findOne.get().getPostComId();
            if (postComId == null) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
