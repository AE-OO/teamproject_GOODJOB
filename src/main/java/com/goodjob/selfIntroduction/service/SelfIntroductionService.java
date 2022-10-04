package com.goodjob.selfIntroduction.service;

import com.goodjob.resume.Resume;
import com.goodjob.selfIntroduction.SelfIntroduction;
import com.goodjob.selfIntroduction.dto.SelfIntroductionDTO;

/**
 * 박채원 22.10.03 작성
 */

public interface SelfIntroductionService {

    void registerSelfInfo(SelfIntroductionDTO selfIntroductionDTO);

    default SelfIntroduction dtoToEntity(SelfIntroductionDTO selfIntroductionDTO){
        Resume resume = Resume.builder().resumeId(selfIntroductionDTO.getResumeId()).build();

        SelfIntroduction selfIntroduction = SelfIntroduction.builder()
                .selfId(selfIntroductionDTO.getSelfId())
                .selfInterActivity(selfIntroductionDTO.getSelfInterActivity())
                .selfLetter(selfIntroductionDTO.getSelfLetter())
                .resume(resume)
                .build();

        return selfIntroduction;
    }

    default SelfIntroductionDTO entityToDTO(SelfIntroduction selfIntroduction, Resume resume){
        SelfIntroductionDTO selfIntroductionDTO = SelfIntroductionDTO.builder()
                .selfId(selfIntroduction.getSelfId())
                .selfInterActivity(selfIntroduction.getSelfInterActivity())
                .selfLetter(selfIntroduction.getSelfLetter())
                .resumeId(resume.getResumeId())
                .build();

        return selfIntroductionDTO;
    }
}
