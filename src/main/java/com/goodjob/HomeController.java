package com.goodjob;

import com.goodjob.post.Post;
import com.goodjob.post.occupation.service.OccupationService;
import com.goodjob.post.postdto.PageRequestDTO;
import com.goodjob.post.postdto.PageResultDTO;
import com.goodjob.post.postdto.PostCardDTO;
import com.goodjob.post.service.PostService;
import com.goodjob.status.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@Log4j2
public class HomeController {

    private final OccupationService occuService;
    private final PostService postService;
    private final StatusService statusService;


    @GetMapping("/login")
    public String login() { return "login";}


    @GetMapping("/search")
    public String search(PageRequestDTO pageRequestDTO, Model model) {
        pageRequestDTO.setSize(12);
        PageResultDTO<Post, PostCardDTO> result = postService.getPagingPostList(pageRequestDTO);
        model.addAttribute("occuAll",occuService.searchOccName());
        model.addAttribute("salaryRange",postService.searchSalaryRange());
        model.addAttribute("result",result);
        return "searchPage";
    }
    @GetMapping(value = {"/"})
    public String main(PageRequestDTO pageRequestDTO, Model model,
                       @ModelAttribute("havePass") String havePass, @RequestParam(value = "param")@Nullable String check){
        pageRequestDTO.setSize(8);
        PageResultDTO<Post, PostCardDTO> result = postService.getPagingPostList(pageRequestDTO);
        model.addAttribute("result",result);

        //박채원 22.11.02 추가 (이하 4줄) - memberController의 login 메소드에서 넘긴 파라미터 값을 받기 위함
        if(havePass.isBlank()){
            havePass = "false";
        }
        model.addAttribute("havePass", havePass);
        System.out.println("1111111 "+check);
        //김도현 22.11.07추가 (회원가입 확인)
        if (check != null && check.equals("1")) {

                model.addAttribute("signupMsg", check);

        }else{
            check = "0";
            model.addAttribute("signupMsg", check);
        }
        return "mainPage";
    }

    // mainPage.html 에서 인기, 최신, 연봉, 마감직전 순으로 정렬 클릭시 ajax로
    // 데이터를 보내주는 메소드
    @PostMapping(value = {"/getPagingPostList"})
    @ResponseBody
    public ResponseEntity<PageResultDTO<Post, PostCardDTO>> getPagingPostList(@RequestBody PageRequestDTO pageRequestDTO) {
        log.info("================================="+pageRequestDTO);
        PageResultDTO<Post, PostCardDTO> result = postService.getPagingPostList(pageRequestDTO);
        log.info("=================="+result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}

