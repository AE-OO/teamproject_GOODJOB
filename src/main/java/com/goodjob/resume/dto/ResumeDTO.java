package com.goodjob.resume.dto;

import lombok.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.sql.Date;

/**
 * 박채원 22.10.05 작성
 * 박채원 22.10.13 수정 - validation 추가
 */

@Data
@Builder
public class ResumeDTO {
    private String memName;
    private String memGender;
    private Date memBirthDate;
    private String memFirstAddress;
    private String memLastAddress;
    private String memFirstEmail;
    private String memLastEmail;
    private String memFirstPhoneNum;
    private boolean deleted;

    @NotNull(message = "전화번호를 입력해 주세요")
    @Pattern(regexp = "/\\d{4}/", message = "숫자 4자리로 입력해 주세요")
    private String memMiddlePhoneNum;

    @NotNull(message = "전화번호를 입력해 주세요")
    @Pattern(regexp = "/\\d{4}/", message = "숫자 4자리로 입력해 주세요")
    private String memLastPhoneNum;
    private Long memId;
}
