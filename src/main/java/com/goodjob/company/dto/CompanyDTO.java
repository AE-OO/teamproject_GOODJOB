/**
 *  HO - 2022.10.05
 *  기업 회원가입을 위한 DTO
 *  18라인 @Setter 추가
 *  +뷰 단에서도 입력조건 주고 여기에서도 검증조건 추가
 *  +@Size,@NotEmpty,@Email 등 검증조건 추가 (spring-boot-starter-validation) 관련
 *  +@NotEmpty ->@NotBlank로 변경 (null, "", " " 모두 허용 안 하기 위해서)
 *
 *  * Gradle에 implementation 'org.springframework.boot:spring-boot-starter-validation' 의존성
 *
 *  + 2022.10.06
 *  comAddress1,2,3,4 추가
 *  - 뒤로가기나 새로고침시 기존 comAddress가 하나인 경우에 모둔 우편번호창, 상세주소창 등에 모든 주소가 재입력 되는 문제 발생.
 *  - 창마다 name을 따로 주어 받아오게 해 문제해결.
 *
 *  +2022.10.17
 *  로그인 폼 기업/개인 통일 위해 로그인, 비밀번호 창 name 통일. 필드도 loginId,pw 통일(46,51, 93, 96라인 변경)
 *
 *  +2022.10.24
 *  기업분류, 지역분류 타입 변경. (회원정보에서 코드와 이름으로 받아오기 위해서)
 *
 *  +22.11.06 기업 회원 지역 테이블 삭제 -> String타입 comRegCode '지역분류코드'필드, comRegName 필드 삭제.
 */
package com.goodjob.company.dto;

import com.goodjob.company.Comdiv;
import com.goodjob.company.Company;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDTO {

    //DTO 클래스에서는 조인컬럼이라도 필드 다 String이나 Long으로 받아야 함.
    @Size(min = 3, max = 15, message = "ID는 3~15자 사이여야 합니다.")
    @NotBlank(message = "사용자 ID는 필수항목입니다.")
    @Pattern(regexp = "[a-z0-9_-]{3,15}"
            ,message = "아이디는 3~15자의 영문 소문자와 숫자,특수기호(_),(-)만 사용 가능합니다.")
    //ho - 22.10.17 comLoginID -> loginId (로그인 폼 input name 통일. DTO 필드 loginId,pw 로 통일)
    private String loginId;

    @Size(min = 3, max = 25, message = "Password는 3~25자 사이여야 합니다.")
    @NotBlank(message = "비밀번호는 필수항목입니다.")
    @Pattern(regexp="(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{3,25}",
            message = "비밀번호는 3~25자로 최소 1개의 영문자, 숫자, 특수문자가 들어가야 합니다.")
    //ho - 22.10.17 comPw1 -> pw (로그인 폼 input name 통일. DTO 필드 loginId,pw 로 통일)
    private String pw;

    //비밀번호가 일치하지 않으면 넘어가지 않도록 하기 위해 엔티티와는 별개로 DTO에만 comPw2 추가
    @NotBlank(message = "비밀번호 확인은 필수항목입니다.")
    private String comPw2;

    @NotBlank
    private String comComdivCode;

    private String comComdivName;
    @NotBlank
    @Pattern(regexp="\\d{2,3}-\\d{3,4}-\\d{4}", message = "전화번호 형식이 맞지 않습니다. 다시 확인해주세요.")
    private String comPhone;
    @NotBlank
    private String comName;
    @NotBlank
    @Pattern(regexp="\\d{3}-\\d{2}-\\d{5}", message = "사업자 등록번호 형식이 맞지 않습니다. 다시 확인해주세요.")
    private String comBusiNum; // 사업등록번호
    @NotBlank
    private String comInfo;
    @NotBlank
    private String comTerms;
    @NotBlank
    private String comAddress1;  // 회사주소(우편번호)
    @NotBlank
    private String comAddress2;  // 회사주소(주소)
    private String comAddress3;  // 회사주소(상세주소)

    private String comAddress4;  // 회사주소(참고항목)
    @NotBlank
    @Pattern(regexp="[0-9a-zA-Z]([-_.]?[0-9a-zA-Z]){2,25}"
            , message = "이메일 형식이 맞지 않습니다. 다시 확인해주세요.")
    private String comEmail1;
    @NotBlank(message = "이메일을 선택해주세요.")
    private String comEmail2;

    //22.10.29 - 아이디 찾기 때 사용하는 엔티티 변환 메서드. 기존 toEntity 메서드 if문 때문에 에러 발생.
    public Company toEntityForFindId(){
        return Company.builder()
                .comPw(pw)
                .comName(comName)
                .comEmail(comEmail1)
                .build();
    }

    public Company toEntity() {
        //엔티티 바꿀 때는 builder이용해서 필요한 객체 만들자.

        Comdiv comdiv = Comdiv.builder()
                .comdivCode(comComdivCode)
                .build();

        //ho - 22.10.17 getMemPw -> getPw (로그인 폼 input name 통일. DTO 필드 loginId,pw 로 통일) 93,96라인 변경
        Company com = Company.builder()
                .comLoginId(loginId)
                .comComdivCode(comdiv)
                .comPw(pw)
                .comPhone(comPhone)
                .comEmail(comEmail1+"@"+comEmail2)
                .comName(comName)
                .comBusiNum(comBusiNum)
                .comInfo(comInfo)
                .comTerms(comTerms)
                .comAddress(comAddress1+"@"+comAddress2+"@"+comAddress3+"@"+comAddress4)
                .build();
        return com;
    }
}