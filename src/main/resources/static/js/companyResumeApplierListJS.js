$(document).ready(function () {
    var pageNum = 0;
    var postId = $("#postId").val();
    getApplierList(postId, pageNum);
})

//지원자 리스트 출력
function getApplierList(postId, pageNum) {
    $.getJSON('/status/getApplierList/' + postId + '/' + pageNum, function (result){
        var list = '';
        $(".postTitle").text("공고명 [ " + $("#postTitle").val() + " ] 의 서류 지원자입니다.");

        if(result.dtoList.length > 0){
            $.each(result.dtoList, function (applyIdx, applier) {
                list += '    <tr>\n' +
                    '      <th scope="row">' + (applyIdx + 1) + '</th>\n' +
                    '      <td>' + applier.applierName + '</td>\n' +
                    '      <td>' + applier.postTitle.substr(0, 10) + "..." + '</td>\n' +
                    '      <td>' + applier.postOccupation + '</td>\n' +
                    '      <td>' + applier.statApplyDate + '</td>\n';

                //공고 마감 날짜 + 30일이랑 오늘 날짜 비교해서 이전 날짜이면 이력서 열람 비활성화
                if(addDay(applier.postEndDate, 30) >= new Date()){
                    list += '    <td><a href="/resume/resumeRead/'+ applier.applierId +'/'+ applier.statResumeId +'" target="_blank"><button class="btn btn-sm bg-gradient-dark">이력서 열람</button></a></td>\n';
                }else{
                    list += '    <td><button class="btn btn-sm bg-gradient-dark" disabled>이력서 열람</button></td>\n';

                }

                //결과가 나왔으면 그 결과를 띄우고 아니면 합격/불합격 버튼을 띄움
                if(applier.statPass === '서류합격' || applier.statPass === '최종합격'){
                    list += '      <td style="color: #0a53be;">' + applier.statPass + '</td>\n';
                }else if(applier.statPass === '서류불합격' || applier.statPass === '면접불합격'){
                    list += '      <td style="color: red;">' + applier.statPass + '</td>\n';
                }else if(applier.statPass === '서류불합격' || applier.statPass === '면접불합격'){
                    list += '      <td style="color: red;">' + applier.statPass + '</td>\n';
                } else{
                    list += '<td class="passBtn' + applier.statId + '"><button class="btn btn-sm bg-gradient-dark" onclick="clickPass('+ applier.statId +')">합격</button>' +
                        '<button class="btn btn-sm btn-danger" onclick="clickUnPass('+ applier.statId +')">불합격</button>' +
                        '</td>';
                }

                list += '    </tr>\n';
            })

            $(".applierTable").html(list);

            //페이징처리
            var pageBtn = '';

            pageBtn += '<li class="page-item" th:if="${'+ result.prev +'}">';
            pageBtn += '<a class="page-link" onclick="getApplierList(' + (result.start - 1) + ')" tabindex="-1"><<</a>';
            pageBtn += '</li>';
            for(i = 0; i < result.totalPage; i++){
                pageBtn += '<a class="page-link" onclick="getApplierList('+ i +')"><li class="page-item">'+ (i + 1) +'</li></a>';
            }
            pageBtn += '<li class="page-item" th:if="${'+ result.next +'}">';
            pageBtn += '<a class="page-link" onclick="getApplierList(' + (result.end) + ')">>></a>';
            pageBtn += '</li>';

            $(".pagination").html(pageBtn);
        }else{
            //리스트에 띄울 내용이 없는 경우
            list += '<h6 class="text-center">지원자가 없습니다.</h6>';
            $(".noApplier").html(list);
        }

    })
}

function clickPass(statId) {
    Swal.fire({
        title: '"서류 합격 처리하시겠습니까?"',
        icon: 'warning',

        showCancelButton: true,
        confirmButtonColor: '#344767',
        cancelButtonColor: '#d33',
        confirmButtonText: '확인',
        cancelButtonText: '취소',

        reverseButtons: true,

    }).then(result => {
        if (result.isConfirmed) {
            $.ajax({
                url: "/status/changePass/" + statId,
                type: "get",
                data: {applyResult : "서류합격"},
                success: function () {
                    Swal.fire({
                        title: '서류 합격 처리하였습니다',
                        icon: 'info'
                    });
                    $(".passBtn" + statId).replaceWith('<td style="color: #0a53be;">서류합격</td>');
                }
            });
        }else {
            getJSONResumeList();
        }
    });
    // if (confirm("서류 합격 처리하시겠습니까?")) {
    //     $.ajax({
    //         url: "/status/changePass/" + statId,
    //         type: "get",
    //         data: {applyResult : "서류합격"},
    //         success: function () {
    //             Swal.fire({
    //                 title: '서류 합격 처리하였습니다',
    //                 icon: 'info'
    //             });
    //             $(".passBtn" + statId).replaceWith('<td style="color: #0a53be;">서류합격</td>');
    //         }
    //     })
    // }
}

function clickUnPass(statId) {
    Swal.fire({
        title: '"서류 불합격 처리하시겠습니까?"',
        icon: 'warning',

        showCancelButton: true,
        confirmButtonColor: '#344767',
        cancelButtonColor: '#d33',
        confirmButtonText: '확인',
        cancelButtonText: '취소',

        reverseButtons: true,

    }).then(result => {
        if (result.isConfirmed) {
            $.ajax({
                url: "/status/changeUnPass/" + statId,
                type: "get",
                data: {applyResult: "서류불합격"},
                success: function () {
                    Swal.fire({
                        title: '서류 불합격 처리하였습니다',
                        icon: 'info'
                    });
                    $(".passBtn" + statId).replaceWith('<td style="color: red;">서류불합격</td>');
                }
            });
        }
    });
}

function addDay(date, days) {
    var result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}
