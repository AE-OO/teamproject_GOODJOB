package com.goodjob.post.service;
import com.goodjob.company.Company;
import com.goodjob.post.Post;
import com.goodjob.post.occupation.Occupation;
import com.goodjob.post.postdto.*;
import com.goodjob.post.salary.PostSalary;
import com.goodjob.post.util.EntityDtoMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface PostService extends EntityDtoMapper {
    PageResultDTO<Post, PostCardDTO> getPagingPostList(PageRequestDTO pageRequestDTO);

    PageResultDTO<Post, PostComMyPageDTO> getPagingPostListInComMyPage(PageRequestDTO pageRequestDTO);


    CompanyInfoDTO getComInfo(String sessionId);

    List<Occupation> getListOccupation();

    List<PostSalary> getListSalary();

    Long savePost(PostInsertDTO postInsertDTO) throws IOException;


    PostDetailsDTO readPost(Long postId) throws IOException;

    PostInsertDTO getPostById(Long postId);

    void deletePost(Long postId);

    List<String> searchSalaryRange();

    // 오성훈 추가
    Optional<Post> findOne(Long postId);
    boolean existsPostByPostIdAndPostComId(Long postId, Company company);

}
