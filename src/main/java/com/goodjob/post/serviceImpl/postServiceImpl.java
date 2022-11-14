package com.goodjob.post.serviceImpl;

import com.goodjob.company.Company;
import com.goodjob.company.repository.CompanyRepository;
import com.goodjob.post.Address;
import com.goodjob.post.Post;
import com.goodjob.post.fileupload.FileService;
import com.goodjob.post.fileupload.UploadFile;
import com.goodjob.post.postdto.*;
import com.goodjob.post.repository.PostRepository;
import com.goodjob.post.QPost;
import com.goodjob.post.occupation.Occupation;
import com.goodjob.post.occupation.repository.OccupationRepository;
import com.goodjob.post.salary.PostSalary;
import com.goodjob.post.salary.SalaryRepository;
import com.goodjob.post.service.PostService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class postServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final OccupationRepository occupationRepository;
    private final CompanyRepository companyRepository;
    private final SalaryRepository salaryRepository;
    private final FileService fileService;

    @Override
    public PageResultDTO<Post, PostCardDTO> getPagingPostList(PageRequestDTO pageRequestDTO){
        Pageable pageable = pageRequestDTO.getPageable(decideSort(pageRequestDTO));
        BooleanBuilder booleanBuilder = getSearch(pageRequestDTO);
        Page<Post> result = postRepository.findAll(booleanBuilder,pageable);
        Function<Post, PostCardDTO> fn = (this::entityToDtoInMain);
        return new PageResultDTO<>(result,fn);
    }
    @Override
    public PageResultDTO<Post, PostComMyPageDTO> getPagingPostListInComMyPage(PageRequestDTO pageRequestDTO){
        Pageable pageable = pageRequestDTO.getPageable(decideSort(pageRequestDTO));
        BooleanBuilder booleanBuilder = getSearch(pageRequestDTO);
        Page<Post> result = postRepository.findAll(booleanBuilder,pageable);
        Function<Post, PostComMyPageDTO> fn = (this::entityToDtoInComMyPage);
        return new PageResultDTO<>(result,fn);
    }

    // 세션 정보를 사용하여 company에서 필요한 정보를 모아 DTO 생성
    @Override
    public CompanyInfoDTO getComInfo(String sessionId){

        Optional<Company> optionalCompany = companyRepository.findByComLoginId(sessionId); // 옵셔널 company 를 가져온다.

        if(optionalCompany.isPresent()){
            Company company = optionalCompany.get();
            String[] str = company.getComAddress().split("@");
            return new CompanyInfoDTO(str[0], str[1], str[2], str[3], company.getComName(),company.getComBusiNum(),company.getComComdivCode().getComdivName());
        }
        return null;
    }
    @Override
    public List<Occupation> getListOccupation(){
        return occupationRepository.findAll();
    }

    @Override
    public List<PostSalary> getListSalary(){
        return salaryRepository.findAll();
    }

    @Override
    public Long savePost(PostInsertDTO postInsertDTO) throws IOException {
        Address address = new Address(HtmlUtils.htmlEscape(postInsertDTO.getPostcode()), HtmlUtils.htmlEscape(postInsertDTO.getPostAddress()), HtmlUtils.htmlEscape(postInsertDTO.getPostDetailAddress()),HtmlUtils.htmlEscape(postInsertDTO.getEtc()+""));
        Optional<Occupation> occupation = occupationRepository.findById(postInsertDTO.getPostOccCode());
        Optional<Company> company = companyRepository.findByComLoginId(postInsertDTO.getComLoginId());
        List<UploadFile> uploadFiles =  fileService.storeFiles(postInsertDTO.getPostImg());
        Optional<PostSalary> salary = salaryRepository.findById(postInsertDTO.getPostSalaryId());
        if(occupation.isPresent() && company.isPresent() && salary.isPresent()){
            Post post = postRepository.save(dtoToEntityForInsert(postInsertDTO,occupation.get(),company.get(),salary.get(),uploadFiles,address));
            return post.getPostId();
        }
        return null;
    }
    @Override
    public PostDetailsDTO readPost(Long postId) throws IOException {
        Optional<Post> result = postRepository.findById(postId);

        List<String> fileList = fileService.getFiles(result.get().getPostImg());
        if(fileList.isEmpty()){
            fileList.add("no_image.png");
        }
        // 게시글 조회 후 조회수를 +1 한다.
        postRepository.increasePostCount(postId);
        return result.map((Post post) -> entityToDtoForRead(post,fileList)).orElse(null);
    }
    @Override
    public PostInsertDTO getPostById(Long postId){
        Optional<Post> optionalPost = postRepository.findById(postId);
        return optionalPost.map(this::entityToDtoForUpdate).orElse(null);
    }
    @Override
    public void deletePost(Long postId){
        postRepository.deleteById(postId);
    }

    // pageRequestDTO 의 sort 값에 따라 공고 리스트의 정렬에 필요한 Sort를 리턴해주는 메솓즈
    // getPagingPostList()와  getPagingPostListInComMyPage() 에 사용한다.
    private Sort decideSort(PageRequestDTO pageRequestDTO){
        switch (pageRequestDTO.getSort()) {
            case "new":
                return Sort.by("postId").descending();
            case "count":
                return Sort.by("postReadCount").descending();
            case "salary":
                return Sort.by("postSalary").descending();
            case "end":
                return Sort.by("postEndDate").ascending();
        }
        return  null;
    }

    private BooleanBuilder getSearch(PageRequestDTO pageRequestDTO){
        QPost qPost = QPost.post;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        // 세션의 authType = "company" 이라면 로그인한 기업회원의 글만
        // 가져오는 조건 추가.
        if(pageRequestDTO.getAuthType()!=null){
            if(pageRequestDTO.getAuthType().equals("company")){
                booleanBuilder.and(qPost.postComId.comLoginId.eq(pageRequestDTO.getAuth()));
            }
        }
        // 공고를 등록한 기업 계정이 삭제 됬을 경우에는 검색 대상이 되지 않는 조건을 추가한다.
        // 로그인 아이디가 null 이면
        booleanBuilder.and(qPost.postComId.isNotNull());

        // 모집상태(모집중, 모집시작 전, 모집 종료, 전체) 조건을 추가하는 코드(기본은 모집중)
        booleanBuilder.and(getOutOfDateState(pageRequestDTO,qPost));
        // pageRequestDTO에 type이 있고 keyword가 있으면 post 테이블 postTitle 컬럼과 compnay 테이블 comName 컬럼에서 해당 값과 일치하는 조건을 추가한다.
        BooleanBuilder booleanBuilderWithTitleComName = getConditionsWithSearchTypeKeyword(pageRequestDTO,qPost);
        if(booleanBuilderWithTitleComName!=null){
            booleanBuilder.and(booleanBuilderWithTitleComName);
        }
        // pageRequestDTO filterOccupation 값이 있으면 occupation 테이블 occName 컬럼에서 해당 값과 일치하는 조건을 추가한다.
        if (!(pageRequestDTO.getFilterOccupation()==null||pageRequestDTO.getFilterOccupation().isEmpty())){
            booleanBuilder.and(qPost.postOccCode.occName.eq(pageRequestDTO.getFilterOccupation()));
        }
        // pageRequestDTO filterAddress 값이 있으면 post 테이블 address1 컬럼에서 해당 값과 일치하는 조건을 추가한다.
        BooleanBuilder booleanBuilderWithAddress = getConditionsWithAddress(pageRequestDTO,qPost);
        if(booleanBuilderWithAddress!=null){
            booleanBuilder.and(booleanBuilderWithAddress);
        }
        // pageRequestDTO filterSalary 값이 있으면 postSalary 테이블 salaryRange 컬럼에서 해당 값과 일치하는 조건을 추가한다.
        if (!(pageRequestDTO.getFilterSalary()==null||pageRequestDTO.getFilterSalary().isEmpty())){
            booleanBuilder.and(qPost.postSalary.salaryRange.eq(pageRequestDTO.getFilterSalary()));
        }

//        System.out.println("불리언 빌더 최종 쿼리 조건 : "+booleanBuilder);
        return booleanBuilder;
    }

    private BooleanBuilder getConditionsWithAddress(PageRequestDTO pageRequestDTO, QPost qPost){
        if (!(pageRequestDTO.getFilterAddress()==null||pageRequestDTO.getFilterAddress().isEmpty())){
            BooleanBuilder bd = new BooleanBuilder();
            String addressKeyword = pageRequestDTO.getFilterAddress();
            List<String> addressKeywordList = tokenizerStringToList(addressKeyword," ");
            List<String> addressDepth1 = new ArrayList<>(Arrays.asList("서울","경기","인천","강원","대전","세종","부산","울산","대구","광주","제주"));
            List<String> addressDepth1_do = new ArrayList<>(Arrays.asList("경상남","경상북","충청남","충청북","전라남","전라북"));
            addressKeywordList = addressKeywordList.stream().map(e->{
                bd.or(qPost.address.address1.contains(e));
                for(String str : addressDepth1){
                    if(e.startsWith(str)){
                        e = str;
                    };
                }
                for(String str : addressDepth1_do){
                    if(e.startsWith(str)){
                       e =String.valueOf(str.charAt(0))+String.valueOf(str.charAt(2));
                    };
                }
                return e;
            }).collect(Collectors.toList());
            addressKeywordList.forEach(e -> {
                bd.or(qPost.address.address1.contains(e));
            });
            bd.or(qPost.address.address1.contains(addressKeyword));
            return bd;
        }
        return null;
    }
    // 공고 리스트를 얻을 시 조건을 추가한다.( 검색어 포함 )
    private BooleanBuilder getConditionsWithSearchTypeKeyword(PageRequestDTO pageRequestDTO,QPost qPost){
        String type = pageRequestDTO.getType();
        String keyword = pageRequestDTO.getKeyword();
        BooleanBuilder bd = new BooleanBuilder();
       if(!(keyword == null || keyword.isEmpty())) {
           List<String> keywordList = tokenizerStringToList(pageRequestDTO.getKeyword()," ");
           switch (type) {
                case "title":
                    // 검색 조건으로 type = "title" 인 경우, 공고 제목에 keyword 를 가지는 글만
                    // 가져오는 조건 추가
                    bd.or(qPost.postTitle.contains(keyword));
                    break;
                case "company":
                    // 검색 조건으로 type = "company" 인 경우, 기업명으로 keyword 를 가지는 글만
                    // 가져오는 조건 추가
                    bd.or(qPost.postComId.comName.contains(keyword));
                    break;
                case "occupation":
                    // 검색 조건으로 type = "occupation" 인 경우, 직종명으로 keyword 를 가지는 글만
                    // 가져오는 조건 추가
                    bd.or(qPost.postOccCode.occName.contains(keyword));
                    break;
                case "region":
                    // 검색 조건으로 type = "region" 인 경우, 지역명으로 keyword 를 가지는 글만
                    // 가져오는 조건 추가
                    bd.or(qPost.address.address1.contains(keyword));
                    break;
                case "titleCompanyName":
                    // 검색 조건으로 type = "titleCompanyName" 인 경우, 공고 제목 또는 회사명으로
                    // keyword 를 가지는 글만 가져오는 조건 추가
                    keywordList.forEach(e -> {
                        bd.or(qPost.postTitle.contains(e).or(qPost.postComName.contains(e)));
                    });
                    bd.or(qPost.postTitle.contains(keyword)).or(qPost.postComName.contains(keyword));
                    break;
            }
            return bd;
        }
       return null;
    }

    // String을 ArrayList로 변환해주는 메소드
    private List<String> tokenizerStringToList(String keyword,String delim){
        List<String> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(keyword,delim);
        while(st.hasMoreTokens()){
            list.add(st.nextToken());
        }
        return list;
    }

    // 공고 리스트를 PageRequestDTO의 sort 값에 따라 정렬을 다르게하는 조건을 리턴해주는 메소드.
    // getSearch() 메소드에 사용된다.
    private BooleanBuilder getOutOfDateState(PageRequestDTO pageRequestDTO,QPost qPost){
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        LocalDate curDateEnd = LocalDate.now();
        LocalDate curDateStart = LocalDate.now().plusDays(1);
        // "모집 시작일이 현재 보다 앞이다."라는 조건 즉, 모집 중.
        BooleanExpression startDateBeforeNow = qPost.postStartDate.before(java.sql.Date.valueOf(curDateStart));
        // "모집 종료일이 현재 보다 뒤다"라는 조건. 즉, 모집 중.
        BooleanExpression endDateAfterNow = qPost.postEndDate.after(java.sql.Date.valueOf(curDateEnd));
        // "모집 시작일이 현재 보다 뒤다"라는 조건. 즉, 모집 시작 전.
        BooleanExpression startDateAfterNow = qPost.postStartDate.after(java.sql.Date.valueOf(curDateStart));
        // "모집 종료일이 현재 보다 앞다"라는 조건. 즉, 모집 종료.
        BooleanExpression endDateBeforeNow = qPost.postEndDate.before(java.sql.Date.valueOf(curDateEnd));
        switch (pageRequestDTO.getOutOfDateState()) {
            case "active":
                return booleanBuilder.and(startDateBeforeNow).and(endDateAfterNow);
            case "beforeStart":
                return booleanBuilder.and(startDateAfterNow);
            case "afterEnd":
                return booleanBuilder.and(endDateBeforeNow);
            case "beforeAfter":
                return booleanBuilder.and(startDateAfterNow.or(endDateBeforeNow));
            case "all":
                break;
        }
        return null;
    }

    @Override
    public List<String> searchSalaryRange(){
        return  postRepository.salaryRange();
    }

    @Override
    public Optional<Post> findOne(Long postId) {
        return postRepository.findById(postId);
    }

    @Override
    public boolean existsPostByPostIdAndPostComId(Long postId, Company company) {
        return postRepository.existsPostByPostIdAndPostComId(postId, company);
    }
}
