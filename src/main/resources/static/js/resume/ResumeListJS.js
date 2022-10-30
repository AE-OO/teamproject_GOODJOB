/**
 * 박채원 22.10.23 작성
 */

$(document).ready(function () {
    var pageNum = 0;
    getJSONResumeList();
    getApplyList(pageNum);
    getResumeData();  //동작 안됨

    //이력서 등록버튼 누르면 이력서 번호부터 등록하고 시작
    $("#registerResume").click(function () {
        var size = $("h5[name='resumeTitle']").length;

        if (size >= 5) {
            alert("이미 이력서를 5개 작성하였습니다.");
        } else {
            $.ajax({
                type: "get",
                url: "/resume/registerResume",
                dataType: "text",
                success: function (data) {
                    var openNewWindow = window.open("about:blank");
                    openNewWindow.location.href = "/resume/resumeStep1/" + data;
                    getJSONResumeList($("input[id=sessionInput]").val());
                }
            });
        }
    })
})

function getResumeData() {
    var count = $("input[id=resumeCheckBox]").length;
    console.log(count);
    $(".resumeCount").text(count);
}

function getJSONResumeList() {
    $.getJSON('/member/getResumeList', function (arr) {
        var list = '';

        $.each(arr, function (idx, resume) {
            list += '<div class="row">';
            list += '<div class="col-2 text-center" style="margin-top: 8px">';
            list += '<input class="form-check-input me-1" type="checkbox" id="resumeCheckBox" value="' + resume.resumeId + '" name="resumeCheckBox" style="background-color: #e4e1e4">'
            list += '</div>';
            list += '<div class="col-8" style="margin-top: 8px">';
            list += '<h5 class="text-bold" name="resumeTitle" onclick="changeTitleForm(this)">' + resume.resumeTitle + '</h5>';
            list += '<h6 style="color: #bbb8bb">이력서 등록날짜 | ' + resume.resumeUpdateDate + '</h6>';
            list += '</div>';
            list += '<div class="col-2">';
            list += '<input id="resumeId" type="hidden" value="' + resume.resumeId + '">'
            list += '<button type="button" class="btn btn-sm btn-outline-secondary" style="margin-bottom: 8px" onclick="updateResume(this)">이력서수정</button>';
            list += '<br>';
            list += '<button type="button" class="btn btn-sm btn-outline-danger" style="margin-bottom: 8px" onclick="deleteResume(this)">이력서삭제</button>';
            list += '</div>';
            list += '</div>';
            list += '<hr style="margin-bottom: 25px">';
        })

        $(".resumeList").html(list);
    })
}

//이력서 개별 삭제
function deleteResume(data) {
    var resumeIdList = [];
    var resumeId = $(data).parent().find("input[id=resumeId]").val();
    resumeIdList.push(resumeId);

    if (confirm("이력서를 삭제하겠습니까?") == true) {
        $.ajax({
            url: "/resume/deleteResume",
            type: "get",
            data: {"resumeId": JSON.stringify(resumeIdList)},
            success: function (result) {
                if (result === 'success') {
                    alert("이력서가 삭제되었습니다.");
                    getJSONResumeList($("input[id=sessionInput]").val());
                }
            }
        });
    } else {
        getJSONResumeList($("input[id=sessionInput]").val());
    }
}

//이력서 수정
function updateResume(data) {
    var resumeId = $(data).parent().find("input[id=resumeId]").val();
    var openNewWindow = window.open("about:blank");
    openNewWindow.location.href = "/resume/goPreviousStep1/" + resumeId;
}

//이력서 제목 수정폼
function changeTitleForm(data) {
    var titleInput = '';
    titleInput += '<input class="form-control col-md-8" type="text" name="changeTitle" placeholder="ex) OO회사 이력서, OO직종 이력서">';
    titleInput += '<button class="btn btn-sm btn-outline-secondary" type="button" onclick="changeTitle(this)">수정</button>';

    $(data).replaceWith(titleInput);
}

//이력서 제목 수정
function changeTitle(data) {
    var resumeId = $(data).parent().parent().find("input[id=resumeId]").val();
    var title = $("input[name=changeTitle]").val();

    if(title == ''){
        title = '제목 없음';
    }

    $.ajax({
        url: "/resume/changeTitle/" + resumeId,
        type: "get",
        data: {"title": title},
        success: function () {
            getJSONResumeList();
        }
    })

    //put으로 하는거 왜 안되는지 모름
    // $.ajax({
    //     url: "/resume/changeTitle/" + resumeId,
    //     type: "put",
    //     data: JSON.stringify(resume),
    //     contentType: "application/json; charset=utf-8",
    //     success: function (){
    //         getJSONResumeList($("input[id=sessionInput]").val());
    //     }
    // })
}

//체크된 이력서들을 삭제함
function deleteCheckedResume() {
    var checkedList = [];
    var size = $("input:checkbox[name=resumeCheckBox]:checked").length;

    for (i = 0; i < size; i++) {
        checkedList.push($("input:checkbox[name=resumeCheckBox]:checked").eq(i).val());
    }
    ;

    if (confirm("이력서를 삭제하겠습니까?") == true) {
        $.ajax({
            url: "/resume/deleteResume",
            type: "get",
            data: {"resumeId": JSON.stringify(checkedList)},
            success: function (result) {
                if (result === 'success') {
                    alert("이력서가 삭제되었습니다.");
                    getJSONResumeList();
                }
            }
        });
    }
    ;
}

//지원현황 리스트
function getApplyList(pageNum) {
    $.getJSON('/status/getApplyList/' + pageNum, function (result) {
        var list = '';

        $.each(result.dtoList, function (applyIdx, apply) {
            list += '    <tr>\n' +
                '      <th scope="row">' + (applyIdx + 1) + '</th>\n' +
                '      <td><a href="#">' + apply.postName.substr(0, 10) + "..." + '</a></td>\n' +
                '      <td>' + apply.companyName + '</td>\n' +
                '      <td><a href="/resume/goPreviousStep1/'+ apply.statResumeId +'" target="_blank">' + apply.resumeTitle.substr(0, 6) + "..." + '</a></td>\n' +
                '      <td>' + apply.statApplyDate + '</td>\n' +
                '      <td>' + apply.statPass + '</td>\n' +
                '    </tr>\n';
        })

        $(".applyTable").html(list);

        var pageBtn = '';

        //이렇게 적으면 버튼하나를 누를 때마다 result.page가 1씩 증가하는데 왜 그런겨
        // $.each(result.pageList, function (pageIdx, page){
        //     pageBtn += '<li class="page-item"><a class="page-link" onclick="getApplyList('+ result.page +')">'+ page +'</a></li>';
        //     console.log(result.page);
        // })  

        pageBtn += '<li class="page-item" th:if="${'+ result.prev +'}">';
        pageBtn += '<a class="page-link" onclick="getApplyList(' + (result.start - 1) + ')" tabindex="-1"><<</a>';
        pageBtn += '</li>';
        for(i = 0; i < result.totalPage; i++){
            pageBtn += '<a class="page-link" onclick="getApplyList('+ i +')"><li class="page-item">'+ (i + 1) +'</li></a>';
        }
        pageBtn += '<li class="page-item" th:if="${'+ result.next +'}">';
        pageBtn += '<a class="page-link" onclick="getApplyList(' + (result.end) + ')">>></a>';
        pageBtn += '</li>';

        $(".pagination").html(pageBtn);
    })
}