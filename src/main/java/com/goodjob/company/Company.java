/**
 * HO - 2022.10.05
 * 26라인 밑에 comMemdivCode(회원분류코드) 필요 없어짐에 따라 삭제
 * 59라인 회사주소 넣기 위해 comAddress 컬럼 추가
 * 37라인 comLoginId - unique 제약조건 추가(아이디 중복 제한)
 */
package com.goodjob.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comId;

    @OneToOne
    @JoinColumn(name = "comRegCode")
    private Region comRegCode; // 지역분류코드

    @OneToOne
    @JoinColumn(name = "comComdivCode")
    private Comdiv comComdivCode; // 회사분류코드

    @Column(unique = true)  //로그인 아이디 중복 허용 안 하기 위해서 엔티티에서 제한.
    private String comLoginId;

    @Column
    private String comPw;

    @Column
    private String comPhone;

    @Column
    private String comEmail;

    @Column
    private String comName;

    @Column
    private String comBusiNum; // 사업등록번호

    @Column(length = 5000)
    private String comInfo;

    @Column(length = 1)
    private String comTerms;

    //22.10.05 ho - 회사 주소 넣기 위해 컬럼 추가
    @Column
    private String comAddress;
}
