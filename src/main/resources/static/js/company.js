//22.10.19 ho - 마이페이지 js파일 + 다음 주소찾기 fuction
//수정
//Daum 주소 찾기API function
function sample6_execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function (data) {
            // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.

            // 각 주소의 노출 규칙에 따라 주소를 조합한다.
            // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
            var addr = ''; // 주소 변수
            var extraAddr = ''; // 참고항목 변수

            //사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                addr = data.roadAddress;
            } else { // 사용자가 지번 주소를 선택했을 경우(J)
                addr = data.jibunAddress;
            }

            // 사용자가 선택한 주소가 도로명 타입일때 참고항목을 조합한다.
            if (data.userSelectedType === 'R') {
                // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
                if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
                    extraAddr += data.bname;
                }
                // 건물명이 있고, 공동주택일 경우 추가한다.
                if (data.buildingName !== '' && data.apartment === 'Y') {
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                if (extraAddr !== '') {
                    extraAddr = ' (' + extraAddr + ')';
                }
                // 조합된 참고항목을 해당 필드에 넣는다.
                document.getElementById("sample6_extraAddress").value = extraAddr;

            } else {
                document.getElementById("sample6_extraAddress").value = '';
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('sample6_postcode').value = data.zonecode;
            document.getElementById("sample6_address").value = addr;
            // 커서를 상세주소 필드로 이동한다.
            document.getElementById("sample6_detailAddress").focus();
        }
    }).open();
}

//22.11.07 - 회원 탈퇴 전 한번 더 확인하도록 수정.
function deleteCompany2() {
    const password = $('#delete_password_confirm').val();
    $.ajax({
        type:"post",
        url:"/com/delete",
        data: {pw:password},
        success:function (confirm) {
            if(confirm == "1") {
                Swal.fire({
                    title: "정말 탈퇴하시겠습니까?",
                    text: "회원탈퇴 시 어떤방법으로도 계정이 복구되지 않습니다.",
                    icon: "warning",
                    confirmButtonText: "예",
                    cancelButtonText: "아니요",
                    closeOnConfirm: true,
                    showCancelButton: true,
                })
                    .then((result) => {
                        if (result.isConfirmed) {
                            location.replace("/com/deleteConfirm");
                        } else if (!result.isConfirmed) {
                            Swal.fire("회원탈퇴", "회원탈퇴를 취소합니다.", "error");
                        }
                    })
            }  else {
                Swal.fire("ERROR", "비밀번호를 확인해주세요.", "error");
            }
        }
    })
}

//22.11.01 ho - 아이디 찾기(이름, 이메일로 찾기)
$('#btn_findId').click(function (){
    findId();
})

function findId() {
    let name = $('#name').val();
    let email = $('#email').val();

    if (!email || email.trim() === "") {
        alert("이메일을 입력하세요.");
    } else {
        if (!name || name.trim() === "") {
            alert("이름(기업명)을 입력하세요.");
        } else {
            $.ajax({
                type: 'post',
                url: '/com/findId',
                data: {
                    'email': email,
                    'name': name
                },
                success: function (result) {
                    console.log(result);
                    if(result[0] == "1"){
                        Swal.fire("기업회원",result[1]+"님의 아이디는 ["+result[2]+"]입니다.")
                            .then(function (){
                                location.href = "/login";
                            })
                    } else if(result[0] == "2") {
                        Swal.fire("개인회원",result[1]+"님의 아이디는 ["+result[2]+"]입니다.")
                            .then(function (){
                                location.href = "/login";
                            })
                    } else {
                        Swal.fire("회원정보 없음","찾으시는 회원정보가 없습니다.","error");
                    }
                }

            })
        }
    }
}

//비밀번호 변경
$(document).ready(function(){
    $('#change_confirm').click(function (){
        const password = $('#change_password_confirm').val();

        $.ajax({
            type:"post",
            url:"/com/changePw",
            data:"pw="+password,
            success : function (result) {
                if(result == 1) {
                    Swal.fire("비밀번호 변경페이지로 이동!","","success").then(function (){
                        location.href = "/com/changePassword";
                    })
                } else {
                    Swal.fire("비밀번호를 확인해주세요!","","error");
                }
            }
        });
    });
});

//22.10.24 ho - 회원정보 수정시 비밀번호 확인
$(document).ready(function(){
    $('#confirm').click(function (){
        const password = $("#password_confirm").val();

        $.ajax({
            type:"post",
            url:"/com/confirm",
            data: "pw=" +password,
            success : function (result) {
                if(result == 1) {
                    Swal.fire("회원정보를 수정합니다!","","success").then(function (){
                        $("#contact-form").submit();
                    })
                } else {
                    Swal.fire("비밀번호를 확인해주세요!","","error");
                }
            }

        })
    });
})

//수정하기 버튼 클릭시 disabled가 false 되며 입력할수 있게 변함. (jquery로 업데이트 10.27)
$(document).ready(function () {
    $('#btn_modify').click(function () {
        //입력창 활성화
        $('#btn_target').attr('disabled', false);
        //'정보 수정하기'버튼 숨기기
        $('#btn_modify').css('display', 'none')
        //'이전으로 돌아가기'버튼 생성
        //$('#btn_back').append('<input type="button" value="이전으로 돌아가기" onClick="btnDisabled()" id="btn_hidden" class="btn bg-gradient-dark w-100">');
        //'이전으로 돌아가기'버튼 보이기
        $('#btn_hidden').removeAttr('hidden');
        //'회원정보 수정완료'버튼 보이기
        $('#modal_open_btn').removeAttr('hidden');
        //'비밀번호 변경하기'버튼 보이기
        $('#btn_change_pw').removeAttr('hidden');
    })
})


//view 유효성 검사

//view - 사업자 등록번호 양식 일치 3-2-5
$('#comBusiNum').keyup(function (){
    let busiNum = $(this).val();
    let regExp = /^\d{3}-\d{2}-\d{5}$/;
    let busiNum_error = $('#busiNum_error');

    if(!regExp.test(busiNum)) {
        busiNum_error.css("color","#ff0000");
        busiNum_error.show().text("형식이 맞지 않습니다. 다시 확인해주세요.");
    }else {
        busiNum_error.css("color","#54b254");
        busiNum_error.show().text("올바른 형식입니다.");
    }
})

//view - 전화번호 양식 일치(2~3)-(3~4)-4
$('#phone').keyup(function (){
    let phone = $('#phone').val();
    let regExp = /^\d{2,3}-\d{3,4}-\d{4}$/;
    let phone_error = $('#phone_error');

    if(!regExp.test(phone)) {
        phone_error.css("color","#ff0000");
        phone_error.show().text("형식이 맞지 않습니다. 다시 확인해주세요.");
    }else {
        phone_error.css("color","#54b254");
        phone_error.show().text("올바른 형식입니다.");
    }
})

//22.11.07 회원정보 수정용 이메일 중복 검사(본인 이메일은 pass) + 유효성검사 추가
function emailDuplicationForUpdate() {
    let email = $('#email').val();
    let email2 = $('#email2').val();
    let emailCheck = email + "@" + email2;

   // let regExp = /^[A-Z0-9a-z._%+-]{2,64}$/;
    let regExp = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
    //let regExp = /^[a-z0-9]+@[a-z]+\.[a-z]{2,3}$/;

    let emailError = $('#email_error');
    emailError.css("color", "#ff0000");

    if (!regExp.test(emailCheck)) {
        emailError.show().text("올바른 이메일 양식이 아닙니다!")
    } else {
        emailError.show().text("");
        $.ajax({
            type: 'post',
            url: "/com/emailCheck2",
            data: {emailCheck: emailCheck},
            success: function (result) {
                console.log(result)
                if (result[0] == "null" ) {
                    emailError.css('color', '#54b254');
                    emailError.html("사용 가능한 이메일입니다.");
                } else if (result[0] == "com") {
                    emailError.css('color', '#ff0000');
                    emailError.html("이미 기업회원으로 가입한 이메일입니다.");
                } else if (result[0] == "mem") {
                    emailError.css('color', '#ff0000');
                    emailError.html("이미 개인회원으로 가입한 이메일입니다.");
                } else if (result[0] == "mine") {
                    emailError.css('color', '#54b254');
                    emailError.html("본인 계정의 이메일입니다.");
                }
            }
        })

    }

}


//view - 이메일 중복 검사 22.11.06
//view - 이메일 양식 검사 후 중복 검사 로 변경.
function emailDuplication(){

    let email = $('#email').val();
    let email2 = $('#email2').val();
    let emailCheck = email + "@" + email2;
    //let regExp = /^[A-Z0-9a-z._%+-]{2,64}$/;
    let regExp = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/;
    //let regExp = /^[a-z0-9]+@[a-z]+\.[a-z]{2,3}$/;
    let emailError = $('#email_error');
    emailError.css("color", "#ff0000");

    if (!regExp.test(emailCheck)) {
        emailError.show().text("올바른 이메일 양식이 아닙니다!")
    } else {
        emailError.show().text("");
        $.ajax({
            type: "post",
            url: "/com/emailCheck",
            data: {emailCheck: emailCheck},
            success: function (result) {
                console.log(result)
                if (result[0] == "null") {
                    emailError.css('color', '#54b254');
                    emailError.html("사용 가능한 이메일입니다.");
                } else if (result[0] == "com") {
                    emailError.css('color', '#ff0000');
                    emailError.html("이미 기업회원으로 가입한 이메일입니다.");
                } else if (result[0] == "mem") {
                    emailError.css('color', '#ff0000');
                    emailError.html("이미 개인회원으로 가입한 이메일입니다.");
                }
            }
        });
    }

}

//view - 비밀번호1,2 일치 여부 보여주는 function
function passwordConfirm() {
    var password = document.getElementById('comPw1');
    var passwordConfirm = document.getElementById('comPw2');
    var confirmMsg = document.getElementById('confirmMsg')  //확인 메시지

    var passwordError = document.getElementById('password_error');

    var correctColor = "#54b254"; //맞았을 때 출력되는 색깔.
    var wrongColor = "#ff0000";   //틀렸을 때 출력되는 색깔.

    var regExp = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{3,25}$/g; //최소 8자 최대 25자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자
//                ^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,}$

    if(!regExp.test(password.value)){
        passwordError.style.color = wrongColor;
        confirmMsg.style.color = wrongColor;
        confirmMsg.innerHTML = "비밀번호는 3~25자로 최소 하나의 영문자, 숫자, 특수문자가 들어가야 합니다."
    } else {
        if(password.value == passwordConfirm.value) {
            passwordError.style.color = correctColor;
            passwordError.innerHTML = ""
            confirmMsg.style.color = correctColor;
            confirmMsg.innerHTML = "비밀번호 일치";
        } else {
            passwordError.style.color = correctColor;
            passwordError.innerHTML = ""
            confirmMsg.style.color = wrongColor;
            confirmMsg.innerHTML = "비밀번호 불일치";
        }

    }

}

//view - 아이디 중복검사
//onkeyup이 여기서는 "change keyup"이다!!!
$('#id_input').on("change keyup", function(){

    var comLoginId = $('#id_input').val();			// .id_input에 입력되는 값
    var data = {comLoginId : comLoginId};			// '컨트롤에 넘길 데이터 이름' : '데이터(.id_input에 입력되는 값)'
    var idError = $('.id_error');
    idError.css("color", "#ff0000");

    var submit = false;

    $.ajax({
        type : "post",
        url : "/com/check",
        data : data,
        success : function(result){
            if(result != 'fail'){  //중복되는 아이디가 없을 때
                let regExp = /^[a-z0-9_-]{3,15}$/; //아이디 정규식(영문 소문자, 숫자만 허용. 길이제한 3~15)
                if(comLoginId === null || comLoginId ==="") {  //아이디 입력을 안 했을 때
                    idError.show().text("필수 입력값입니다. 아이디를 입력해주세요.");
                } else {
                    if(!regExp.test(comLoginId)) {  //아이디 정규식이 맞지 않을 때
                        idError.show().text("아이디는 3~15자의 영문 소문자와 숫자,특수기호(_),(-)만 사용 가능합니다.")
                    } else { //아이디 중복도 없고 정규식도 올바를 때
                        idError.css("color","#54b254");
                        idError.show().text("사용할 수 있는 아이디입니다!");
                        submit = true;
                    }
                }
            } else {  //중복되는 아이디가 있을 경우
                idError.show().text("이미 존재하는 아이디입니다.");
            }
            return submit;
        }
    });
});

//회원가입폼 유효성 검사
function btnRexExp() {

    //아이디 정규식 확인용 변수
    var comLoginId = $('#id_input').val();			// .id_input에 입력되는 값
    let regExp1 = /^[a-z0-9_-]{3,15}$/g; //아이디 정규식(영문 소문자, 숫자만 허용. 길이제한 3~15)

    //비밀번호 정규식 확인용 변수
    var password = $('#comPw1').val();
    var passwordConfirm = $('#comPw2').val();


    //사업자 등록번호 확인용 변수
    let busiNum = $('#comBusiNum').val();
    let regExp4 = /^\d{3}-\d{2}-\d{5}$/;

    //사업자 등록번호 정규식 확인
    let result4 = "false";
    if(!regExp4.test(busiNum) || (busiNum == null || busiNum == "")) {
        $('#comBusiNum').focus();
    }else {
        result4 = "true";
    }

    //전화번호 확인용 변수
    let phone = $('#phone').val();
    let regExp5 = /^\d{2,3}-\d{3,4}-\d{4}$/;

    //전화번호 정규식 확인
    let result5 = "false";
    if(!regExp5.test(phone) || (phone == null || phone == "")) {
        $('#phone').focus();
    }else {
        result5 = "true";
    }

    //이메일 확인용 변수
    let email = $('#email').val();
    let email2 = $('#email2').val();
    let emailCheck = email + "@" + email2;
    // let regExp = /^[A-Z0-9a-z._%+-]{2,64}$/;
    let regExp3 = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
    //let regExp3 = /^[A-Z0-9a-z._%+-]{2,64}$/;

    //이메일 정규식 확인
    let result3 = "false";

    if(email2 == null || email2 == "") {
        $('#email2').focus();
    } else {
        if(!regExp3.test(emailCheck)) {
            $('#email').focus();
        } else {
            result3 = "true";
        }
    }

    //이메일 중복 확인
    let result7 = "false";

    $.ajax({
        type: "post",
        url: "/com/emailCheck",
        async: false,
        data: {emailCheck: emailCheck},
        success: function (result) {
            if (result[0] == "null") {
                result7 = "true";
            } else if(result[0] == "com") {

            } else if(result[0] == "mem") {
            }
            return result7;
        }
    });
    if (result7 == "false") {
        $('#email').focus();
    }

    //비밀번호 정규식 확인
    var result2 = "false";
    let regExp2 = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{3,25}$/g; //최소 8자 최대 25자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자
    if (!regExp2.test(password) || (password===null || password ==="")){
        $('#comPw1').focus();
        //Swal.fire("비밀번호 양식을 확인해주세요.","","error");
    } else {
        if(password != passwordConfirm) {
            $('#comPw2').focus();
        } else {
            result2 = "true";
        }
    }

    // 아이디 중복 확인
    var result6 = "false";
    var data = {comLoginId : comLoginId};			// '컨트롤에 넘길 데이터 이름' : '데이터(.id_input에 입력되는 값)'
    $.ajax({
        type : "post",
        url : "/com/check",
        async:false,  //ajax는 비동기 방식이기 때문에 결과값을 전역변수에 저장하기 위해 추가!
        data : data,
        success : function(result){
            if(result != 'fail'){  //중복되는 아이디가 없을 때
                let regExp = /^[a-z0-9_-]{3,15}$/; //아이디 정규식(영문 소문자, 숫자만 허용. 길이제한 3~15)
                if(comLoginId === null || comLoginId ==="") {  //아이디 입력을 안 했을 때
                } else {
                    if(!regExp.test(comLoginId)) {  //아이디 정규식이 맞지 않을 때
                    } else { //아이디 중복도 없고 정규식도 올바를 때
                        result6 = "true";
                    }
                }
            } else {  //중복되는 아이디가 있을 경우
            }
            return result6;
        }
    });

    if (result6 == "false") {
          $('#id_input').focus();
    }

    //아이디 정규식 확인
    var result1 = "false";
    if (comLoginId === null || comLoginId === "") {  //아이디 입력을 안 했을 때
        //Swal.fire("아이디를 입력해주세요!","","error");
        $('#id_input').focus();
    } else {
        if (!regExp1.test(comLoginId)) {  //아이디 정규식이 맞지 않을 때
            //Swal.fire("아이디 양식을 확인해주세요!","","error");
            $('#id_input').focus();
        } else { //아이디 중복도 없고 정규식도 올바를 때
            result1 = "true";
        }
    }


    //기업소개 null값 방지
    let comInfo = $('#comInfo').val();
    let result12 = "false";
    if (comInfo === null || comInfo === "") {  //기업소개 입력을 안 했을 때
        $('#comInfo').focus();
    } else {
        result12 = "true";
    }

    //기업명 null값 방지
    let comName = $('#comName').val();
    let result10 = "false";
    if (comName === null || comName === "") {  //기업명 입력을 안 했을 때
        $('#comName').focus();
    } else {
        result10 = "true";
    }

    if (result1 == "true" && result2 == "true" && result3 == "true" && result4 == "true" &&
        result5 == "true" && result6 == "true" && result7 == "true" && result10 == "true" && result12 == "true") {
        $('#contact-form').submit();
    } else {
    }
}

//비밀번호 변경 폼 유효성 검사
function btn_passwordChange() {

    //비밀번호 정규식 확인용 변수
    var password = $('#comPw1').val();
    var passwordConfirm = $('#comPw2').val();

    //비밀번호 정규식 확인
    let regExp2 = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{3,25}$/g; //최소 8자 최대 25자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자
    if (!regExp2.test(password)) {
        Swal.fire("비밀번호 사용 불가!","비밀번호는 3~25자로 최소 하나의 영문자, 숫자, 특수문자가 들어가야 합니다.","error");
        return false;
    } else {
        if(password == passwordConfirm){
            $('#form_change_pw').submit();
        } else {
            Swal.fire("비밀번호 불일치!","","error");
        }
    }

}

//회원정보 수정 유효성 검사
function btn_change_info(){
    /** 순서 중요 - 이메일,전화번호,사업자 거꾸로 확인하여 맨 위에 양식 안 맞는 것부터 focus()*/

    //기업소개 확인
    let comInfo = $('#comInfo').val();
    let result12 = "false";
    if (comInfo === null || comInfo === "") {  //기업소개 입력을 안 했을 때
        $('#comInfo').focus();
    } else {
        result12 = "true";
    }

    //이메일 정규식 확인용 변수
    let email = $('#email').val();
    let email2 = $('#email2').val();
    let emailCheck = email + "@" + email2;
    let regExp3 = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;

    //이메일 정규식 확인
    let result3 = "false";
    if(!regExp3.test(emailCheck)){
        $('#email').focus();
    } else {
        result3 = "true";
    }

    //이메일 중복 확인
    let result7 = "false";

    $.ajax({
        type: "post",
        url: "/com/emailCheck2",
        async: false,
        data: {emailCheck: emailCheck},
        success: function (result) {
            console.log(result)
            if (result[0] == "null") {
                result7 = "true";
            } else if(result[0] == "com") {

            } else if(result[0] == "mem") {
            } else if(result[0] == "mine"){
                result7 = "true";
            }

            return result7;
        }
    });

    if (result7 == "false") {
        $('#email').focus();
    }

    //전화번호 확인용 변수
    let phone = $('#phone').val();
    let regExp5 = /^\d{2,3}-\d{3,4}-\d{4}$/;

    //전화번호 정규식 확인
    let result5 = "false";
    if(!regExp5.test(phone)) {
        $('#phone_alert').show();
        $('#phone').focus();
    }else {
        $('#phone_alert').hide();
        result5 = "true";
    }

    //사업자 등록번호 확인용 변수
    let busiNum = $('#comBusiNum').val();
    let regExp4 = /^\d{3}-\d{2}-\d{5}$/;

    //사업자 등록번호 정규식 확인
    let result4 = "false";
    if(!regExp4.test(busiNum)) {
        $('#busiNum_alert').show();
        $('#comBusiNum').focus();
    }else {
        $('#busiNum_alert').hide();
        result4 = "true";
    }

    //기업명 확인
    let comName = $('#comName').val();
    let result10 = "false";
    if (comName === null || comName === "") {  //기업명 입력을 안 했을 때
        $('#comName').focus();
    } else {
        result10 = "true";
    }

    if(result3 != "false" && result4 != "false" && result5 != "false" && result7!="false" && result10!="false" && result12!="false") {
        $('#modal_open_btn').attr("data-toggle", "modal");
        $("#confirmPasswordModal").modal();
    } else {
        $('#modal_open_btn').removeAttr("data-toggle");
    }
}