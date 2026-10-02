const cropGrid =
    document.getElementById('cropGrid');

const favoriteCropIdInput =
    document.getElementById('favoriteCropId');

const btnSaveCrop =
    document.getElementById('btnSaveCrop');

const cropMsg =
    document.getElementById('cropMsg');


const btnOpenRegionModal =
    document.getElementById('btnOpenRegionModal');

const btnWithdraw =
    document.getElementById('btnWithdraw');

const btnLogout =
    document.getElementById('btnLogout');


const favoriteTradeCount =
    document.getElementById('favoriteTradeCount');

const favoriteTradeEmpty =
    document.getElementById('favoriteTradeEmpty');

const favoriteTradeList =
    document.getElementById('favoriteTradeList');


const myTradeCount =
    document.getElementById('myTradeCount');

const myTradeEmpty =
    document.getElementById('myTradeEmpty');

const myTradeList =
    document.getElementById('myTradeList');


const tradeDetailModal =
    document.getElementById('tradeDetailModal');

const tradeDetailBackdrop =
    document.getElementById('tradeDetailBackdrop');

const tradeDetailCloseBtn =
    document.getElementById('tradeDetailCloseBtn');

const tradeDetailImage =
    document.getElementById('tradeDetailImage');

const tradeDetailTitle =
    document.getElementById('tradeDetailTitle');

const tradeDetailPrice =
    document.getElementById('tradeDetailPrice');

const tradeDetailRegion =
    document.getElementById('tradeDetailRegion');

const tradeDetailDate =
    document.getElementById('tradeDetailDate');

const tradeDetailStatus =
    document.getElementById('tradeDetailStatus');

const tradeDetailStatusSelect =
    document.getElementById('tradeDetailStatusSelect');

const tradeDetailDeleteBtn =
    document.getElementById('tradeDetailDeleteBtn');

const tradeDetailQuantity =
    document.getElementById('tradeDetailQuantity');

const tradeDetailContent =
    document.getElementById('tradeDetailContent');

const tradeDetailSellerName =
    document.getElementById('tradeDetailSellerName');

const tradeDetailActions =
    document.getElementById('tradeDetailActions');

const tradeDetailFavoriteBtn =
    document.getElementById('tradeDetailFavoriteBtn');

const tradeDetailFavoriteIcon =
    document.getElementById('tradeDetailFavoriteIcon');

const tradeDetailFavoriteCount =
    document.getElementById('tradeDetailFavoriteCount');


let selectedSidoName =
    '';

let selectedSigunguName =
    '';

let currentTradeDetailMode =
    '';

let currentTradePostId =
    null;


/* =====================================================
   AJAX
   ===================================================== */

function ajaxGet(url, callback) {

    const xhr =
        new XMLHttpRequest();


    xhr.open(
        'GET',
        url,
        true
    );


    xhr.onreadystatechange = function () {

        if (xhr.readyState === 4) {

            if (xhr.status === 200) {

                const res =
                    JSON.parse(xhr.responseText);


                callback(res);

            } else {

                openAlert(
                    '오류',
                    '요청 처리 중 오류가 발생했습니다.'
                );
            }
        }
    };


    xhr.send();
}


function ajaxPost(url, data, callback) {

    const xhr =
        new XMLHttpRequest();


    xhr.open(
        'POST',
        url,
        true
    );


    xhr.setRequestHeader(
        'Content-Type',
        'application/x-www-form-urlencoded; charset=UTF-8'
    );


    xhr.onreadystatechange = function () {

        if (xhr.readyState === 4) {

            if (xhr.status === 200) {

                const res =
                    JSON.parse(xhr.responseText);


                callback(res);

            } else {

                openAlert(
                    '오류',
                    '요청 처리 중 오류가 발생했습니다.'
                );
            }
        }
    };


    const params =
        Object.entries(data)
            .map(function ([key, value]) {

                return encodeURIComponent(key)
                    + '='
                    + encodeURIComponent(value);
            })
            .join('&');


    xhr.send(params);
}


/* =====================================================
   메시지
   ===================================================== */

function showMsg(el, msg, type) {

    if (!el) {
        return;
    }


    el.textContent =
        msg;

    el.className =
        'msg-' + type;
}


function clearMsg(el) {

    if (!el) {
        return;
    }


    el.textContent =
        '';

    el.className =
        '';
}


/* =====================================================
   공통 알림
   ===================================================== */

function openAlert(title, body, onConfirm) {

    CommonModal.open({
        title: title,
        body: body,
        hideCancel: true,
        confirmText: '확인',
        onConfirm: onConfirm
    });
}


/* =====================================================
   거래 가격
   ===================================================== */

function formatTradePrice(price) {

    if (price == null) {
        return '';
    }


    return Number(price)
            .toLocaleString('ko-KR')
        + '원';
}


/* =====================================================
   거래 날짜
   ===================================================== */

function formatTradeDate(createdAt) {

    if (!createdAt) {
        return '';
    }


    return createdAt
        .substring(0, 10)
        .replace(/-/g, '.');
}


/* =====================================================
   거래 상태
   ===================================================== */

function formatTradeStatus(status) {

    if (status === 'SALE') {
        return '판매중';
    }

    if (status === 'RESERVED') {
        return '예약중';
    }

    if (status === 'SOLD') {
        return '판매완료';
    }


    return '';
}


/* =====================================================
   관심 거래글 목록
   ===================================================== */

function renderFavoriteTradeList(list) {

    favoriteTradeList.innerHTML =
        '';


    favoriteTradeCount.textContent =
        list.length + '건';


    if (list.length === 0) {

        favoriteTradeEmpty.hidden =
            false;

        return;
    }


    favoriteTradeEmpty.hidden =
        true;


    list.forEach(function (item) {

        const article =
            createTradeListItem(item);


        favoriteTradeList.appendChild(
            article
        );
    });
}


function loadFavoriteTradeList() {

    if (!favoriteTradeList
        || !favoriteTradeCount
        || !favoriteTradeEmpty) {

        return;
    }


    ajaxGet(
        CTX + 'mypage/getFavoriteTradeList',
        function (list) {

            if (!Array.isArray(list)) {

                openAlert(
                    '관심 거래글',
                    '관심 거래글을 불러오지 못했습니다.'
                );

                return;
            }


            renderFavoriteTradeList(
                list
            );
        }
    );
}


/* =====================================================
   내가 등록한 거래글 목록
   ===================================================== */

function renderMyTradeList(list) {

    myTradeList.innerHTML =
        '';


    myTradeCount.textContent =
        list.length + '건';


    if (list.length === 0) {

        myTradeEmpty.hidden =
            false;

        return;
    }


    myTradeEmpty.hidden =
        true;


    list.forEach(function (item) {

        const article =
            createTradeListItem(item);


        myTradeList.appendChild(
            article
        );
    });
}


function loadMyTradeList() {

    if (!myTradeList
        || !myTradeCount
        || !myTradeEmpty) {

        return;
    }


    ajaxGet(
        CTX + 'mypage/getMyTradeList',
        function (list) {

            if (!Array.isArray(list)) {

                openAlert(
                    '내 거래글',
                    '등록한 거래글을 불러오지 못했습니다.'
                );

                return;
            }


            renderMyTradeList(
                list
            );
        }
    );
}


/* =====================================================
   거래 목록 아이템 생성
   ===================================================== */

function createTradeListItem(item) {

    const article =
        document.createElement('article');


    article.className =
        'mypage-trade-item';

    article.dataset.id =
        item.tradePostId;


    const image =
        document.createElement('img');


    image.className =
        'mypage-trade-image';

    image.alt =
        '거래 상품 이미지';


    if (item.imageUrl) {

        image.src =
            item.imageUrl;

    } else {

        image.hidden =
            true;
    }


    const info =
        document.createElement('div');


    info.className =
        'mypage-trade-info';


    const title =
        document.createElement('strong');


    title.className =
        'mypage-trade-title';

    title.textContent =
        item.title;


    const price =
        document.createElement('span');


    price.className =
        'mypage-trade-price';

    price.textContent =
        formatTradePrice(item.price);


    const detail =
        document.createElement('div');


    detail.className =
        'mypage-trade-detail';


    if (item.quantity != null) {

        const quantity =
            document.createElement('span');


        quantity.textContent =
            '수량 ' + item.quantity + '개';


        detail.appendChild(
            quantity
        );
    }


    const date =
        document.createElement('span');


    date.textContent =
        formatTradeDate(item.createdAt);


    detail.appendChild(
        date
    );


    const status =
        document.createElement('span');


    status.className =
        'mypage-trade-status';

    status.textContent =
        formatTradeStatus(item.status);


    info.appendChild(
        title
    );

    info.appendChild(
        price
    );

    info.appendChild(
        detail
    );


    article.appendChild(
        image
    );

    article.appendChild(
        info
    );

    article.appendChild(
        status
    );


    return article;
}


/* =====================================================
   거래 상세
   ===================================================== */

function renderTradePostDetail(item, mode) {

    currentTradeDetailMode =
        mode;

    currentTradePostId =
        item.tradePostId;


    tradeDetailImage.src =
        item.imageUrl;

    tradeDetailTitle.textContent =
        item.title;

    tradeDetailPrice.textContent =
        formatTradePrice(item.price);

    tradeDetailRegion.textContent =
        item.fullRegionName;

    tradeDetailDate.textContent =
        formatTradeDate(item.createdAt);

    tradeDetailContent.textContent =
        item.content;

    tradeDetailSellerName.textContent =
        item.sellerName;


    tradeDetailFavoriteBtn.dataset.id =
        item.tradePostId;


    tradeDetailFavoriteCount.textContent =
        item.favoriteCount == null
            ? 0
            : item.favoriteCount;


    tradeDetailFavoriteIcon.textContent =
        item.favorite
            ? '♥'
            : '♡';


    if (item.quantity != null) {

        tradeDetailQuantity.textContent =
            '수량 ' + item.quantity + '개';

        tradeDetailQuantity.hidden =
            false;

    } else {

        tradeDetailQuantity.hidden =
            true;
    }


    if (mode === 'mine') {

        tradeDetailStatus.hidden =
            true;

        tradeDetailStatusSelect.hidden =
            false;

        tradeDetailDeleteBtn.hidden =
            false;

        tradeDetailActions.hidden =
            true;


        tradeDetailStatusSelect.value =
            item.status;

        tradeDetailStatusSelect.dataset.previousStatus =
            item.status;

    } else {

        tradeDetailStatus.hidden =
            false;

        tradeDetailStatus.textContent =
            formatTradeStatus(item.status);

        tradeDetailStatusSelect.hidden =
            true;

        tradeDetailDeleteBtn.hidden =
            true;

        tradeDetailActions.hidden =
            false;
    }


    tradeDetailModal.hidden =
        false;
}


function closeTradeDetailModal() {

    tradeDetailModal.hidden =
        true;

    currentTradeDetailMode =
        '';

    currentTradePostId =
        null;
}


function loadTradePostDetail(tradePostId, mode) {

    if (!tradePostId) {
        return;
    }


    const url =
        CTX
        + 'trade/getTradePostDetail?tradePostId='
        + encodeURIComponent(tradePostId);


    ajaxGet(
        url,
        function (res) {

            if (!res) {

                openAlert(
                    '거래글 조회',
                    '거래글을 찾을 수 없습니다.'
                );

                return;
            }


            renderTradePostDetail(
                res,
                mode
            );
        }
    );
}


/* =====================================================
   관심 거래글 클릭
   ===================================================== */

if (favoriteTradeList) {

    favoriteTradeList.addEventListener(
        'click',
        function (e) {

            const article =
                e.target.closest(
                    '.mypage-trade-item'
                );


            if (!article) {
                return;
            }


            const tradePostId =
                article.dataset.id;


            if (!tradePostId) {
                return;
            }


            loadTradePostDetail(
                tradePostId,
                'favorite'
            );
        }
    );
}


/* =====================================================
   내가 등록한 거래글 클릭
   ===================================================== */

if (myTradeList) {

    myTradeList.addEventListener(
        'click',
        function (e) {

            const article =
                e.target.closest(
                    '.mypage-trade-item'
                );


            if (!article) {
                return;
            }


            const tradePostId =
                article.dataset.id;


            if (!tradePostId) {
                return;
            }


            loadTradePostDetail(
                tradePostId,
                'mine'
            );
        }
    );
}


/* =====================================================
   거래 상세 닫기
   ===================================================== */

if (tradeDetailCloseBtn) {

    tradeDetailCloseBtn.addEventListener(
        'click',
        function () {

            closeTradeDetailModal();
        }
    );
}


if (tradeDetailBackdrop) {

    tradeDetailBackdrop.addEventListener(
        'click',
        function () {

            closeTradeDetailModal();
        }
    );
}


/* =====================================================
   관심 등록 / 취소
   ===================================================== */

if (tradeDetailFavoriteBtn) {

    tradeDetailFavoriteBtn.addEventListener(
        'click',
        function () {

            const tradePostId =
                tradeDetailFavoriteBtn.dataset.id;


            if (!tradePostId) {
                return;
            }


            ajaxPost(
                CTX + 'trade/toggleTradeFavorite',
                {
                    tradePostId: tradePostId
                },
                function (res) {

                    tradeDetailFavoriteIcon.textContent =
                        res.favorite
                            ? '♥'
                            : '♡';


                    tradeDetailFavoriteCount.textContent =
                        res.favoriteCount == null
                            ? 0
                            : res.favoriteCount;


                    loadFavoriteTradeList();
                }
            );
        }
    );
}


/* =====================================================
   내가 등록한 거래글 상태 변경
   ===================================================== */

if (tradeDetailStatusSelect) {

    tradeDetailStatusSelect.addEventListener(
        'change',
        function () {

            if (currentTradeDetailMode !== 'mine'
                || !currentTradePostId) {

                return;
            }


            const status =
                tradeDetailStatusSelect.value;

            const previousStatus =
                tradeDetailStatusSelect.dataset.previousStatus;


            ajaxPost(
                CTX + 'mypage/updateMyTradeStatus',
                {
                    tradePostId: currentTradePostId,
                    status: status
                },
                function (res) {

                    if (res.result === 'success') {

                        tradeDetailStatusSelect.dataset.previousStatus =
                            status;


                        loadMyTradeList();


                        openAlert(
                            '상태 변경',
                            res.msg || '거래 상태가 변경되었습니다.'
                        );

                        return;
                    }


                    tradeDetailStatusSelect.value =
                        previousStatus;


                    if (res.result === 'login') {

                        openAlert(
                            '로그인 필요',
                            res.msg || '로그인이 필요합니다.',
                            function () {

                                location.href =
                                    CTX + 'login/login';
                            }
                        );

                        return;
                    }


                    openAlert(
                        '상태 변경 실패',
                        res.msg || '거래 상태 변경에 실패했습니다.'
                    );
                }
            );
        }
    );
}


/* =====================================================
   내가 등록한 거래글 삭제
   ===================================================== */

if (tradeDetailDeleteBtn) {

    tradeDetailDeleteBtn.addEventListener(
        'click',
        function () {

            if (currentTradeDetailMode !== 'mine'
                || !currentTradePostId) {

                return;
            }


            CommonModal.open({
                title: '거래글 삭제',
                body: '정말 이 거래글을 삭제하시겠습니까?',
                cancelText: '취소',
                confirmText: '삭제',
                cancelType: 'neutral',
                confirmType: 'danger',

                onConfirm: function () {

                    ajaxPost(
                        CTX + 'mypage/deleteMyTradePost',
                        {
                            tradePostId: currentTradePostId
                        },
                        function (res) {

                            if (res.result === 'success') {

                                closeTradeDetailModal();


                                loadMyTradeList();

                                loadFavoriteTradeList();


                                openAlert(
                                    '삭제 완료',
                                    res.msg || '거래글이 삭제되었습니다.'
                                );

                                return;
                            }


                            if (res.result === 'login') {

                                openAlert(
                                    '로그인 필요',
                                    res.msg || '로그인이 필요합니다.',
                                    function () {

                                        location.href =
                                            CTX + 'login/login';
                                    }
                                );

                                return;
                            }


                            openAlert(
                                '삭제 실패',
                                res.msg || '거래글 삭제에 실패했습니다.'
                            );
                        }
                    );
                }
            });
        }
    );
}


/* =====================================================
   작물 선택
   ===================================================== */

if (cropGrid) {

    cropGrid.addEventListener(
        'click',
        function (e) {

            const target =
                e.target.closest(
                    '.crop-item'
                );


            if (!target) {
                return;
            }


            const selected =
                cropGrid.querySelector(
                    '.crop-item.selected'
                );


            if (selected) {

                selected.classList.remove(
                    'selected'
                );
            }


            target.classList.add(
                'selected'
            );


            favoriteCropIdInput.value =
                target.dataset.id;


            clearMsg(
                cropMsg
            );
        }
    );
}


if (btnSaveCrop) {

    btnSaveCrop.addEventListener(
        'click',
        function () {

            const favoriteCropId =
                favoriteCropIdInput.value;


            if (favoriteCropId === '') {

                showMsg(
                    cropMsg,
                    '선호 작물을 선택해주세요.',
                    'error'
                );

                return;
            }


            ajaxPost(
                CTX + 'mypage/updateFavoriteCrop',
                {
                    favoriteCropId: favoriteCropId
                },
                function (res) {

                    if (res.result === 'success') {

                        openAlert(
                            '수정 완료',
                            res.msg || '선호 작물이 수정되었습니다.',
                            function () {

                                location.reload();
                            }
                        );

                        return;
                    }


                    if (res.result === 'login') {

                        openAlert(
                            '로그인 필요',
                            res.msg || '로그인이 필요합니다.',
                            function () {

                                location.href =
                                    CTX + 'login/form';
                            }
                        );

                        return;
                    }


                    showMsg(
                        cropMsg,
                        res.msg || '선호 작물 수정에 실패했습니다.',
                        'error'
                    );
                }
            );
        }
    );
}


/* =====================================================
   지역 수정
   ===================================================== */

function getRegionModalElements() {

    const modalBody =
        document.getElementById(
            'commonModalBody'
        );


    return {

        modalSidoSelect:
            modalBody
                ? modalBody.querySelector(
                    '#modalSidoSelect'
                )
                : null,

        modalSigunguSelect:
            modalBody
                ? modalBody.querySelector(
                    '#modalSigunguSelect'
                )
                : null,

        regionMsg:
            modalBody
                ? modalBody.querySelector(
                    '#regionMsg'
                )
                : null
    };
}


function bindRegionModalEvents() {

    const els =
        getRegionModalElements();


    const modalSidoSelect =
        els.modalSidoSelect;

    const modalSigunguSelect =
        els.modalSigunguSelect;

    const regionMsg =
        els.regionMsg;


    if (!modalSidoSelect
        || !modalSigunguSelect) {

        return;
    }


    modalSidoSelect.addEventListener(
        'change',
        function () {

            const sidoName =
                modalSidoSelect.value;


            selectedSidoName =
                sidoName;

            selectedSigunguName =
                '';


            modalSigunguSelect.innerHTML =
                '<option value="">시군구를 선택해주세요</option>';


            modalSigunguSelect.disabled =
                true;


            clearMsg(
                regionMsg
            );


            if (sidoName === '') {
                return;
            }


            ajaxGet(
                CTX
                + 'mypage/getSigungu?sidoName='
                + encodeURIComponent(sidoName),

                function (list) {

                    list.forEach(
                        function (item) {

                            const option =
                                document.createElement(
                                    'option'
                                );


                            option.value =
                                item.sigunguName;

                            option.textContent =
                                item.sigunguName;


                            modalSigunguSelect.appendChild(
                                option
                            );
                        }
                    );


                    modalSigunguSelect.disabled =
                        false;
                }
            );
        }
    );


    modalSigunguSelect.addEventListener(
        'change',
        function () {

            selectedSigunguName =
                modalSigunguSelect.value;


            clearMsg(
                regionMsg
            );
        }
    );
}


if (btnOpenRegionModal) {

    btnOpenRegionModal.addEventListener(
        'click',
        function () {

            const template =
                document.getElementById(
                    'regionModalTemplate'
                );


            selectedSidoName =
                '';

            selectedSigunguName =
                '';


            CommonModal.open({
                title: '지역 수정',
                body: template.innerHTML,
                cancelText: '취소',
                confirmText: '수정',
                cancelType: 'neutral',
                confirmType: 'primary',

                onConfirm: function () {

                    if (selectedSidoName === ''
                        || selectedSigunguName === '') {

                        openAlert(
                            '지역 수정',
                            '지역을 선택해주세요.'
                        );

                        return;
                    }


                    ajaxPost(
                        CTX + 'mypage/updateRegion',
                        {
                            sidoName: selectedSidoName,
                            sigunguName: selectedSigunguName
                        },
                        function (res) {

                            if (res.result === 'success') {

                                openAlert(
                                    '수정 완료',
                                    res.msg || '지역이 수정되었습니다.',
                                    function () {

                                        location.reload();
                                    }
                                );

                                return;
                            }


                            if (res.result === 'login') {

                                openAlert(
                                    '로그인 필요',
                                    res.msg || '로그인이 필요합니다.',
                                    function () {

                                        location.href =
                                            CTX + 'login/form';
                                    }
                                );

                                return;
                            }


                            openAlert(
                                '수정 실패',
                                res.msg || '지역 수정에 실패했습니다.'
                            );
                        }
                    );
                }
            });


            setTimeout(
                function () {

                    bindRegionModalEvents();
                },
                0
            );
        }
    );
}


/* =====================================================
   회원탈퇴
   ===================================================== */

if (btnWithdraw) {

    btnWithdraw.addEventListener(
        'click',
        function () {

            CommonModal.open({
                title: '회원탈퇴',

                body:
                    '정말 회원탈퇴를 진행하시겠습니까?'
                    + '<br>'
                    + '탈퇴 후 15일 이내에는 로그인 시 복구할 수 있습니다.',

                cancelText: '취소',
                confirmText: '탈퇴',
                cancelType: 'neutral',
                confirmType: 'danger',

                onConfirm: function () {

                    ajaxPost(
                        CTX + 'mypage/withdraw',
                        {},
                        function (res) {

                            if (res.result === 'success') {

                                openAlert(
                                    '탈퇴 완료',
                                    res.msg || '회원탈퇴가 처리되었습니다.',
                                    function () {

                                        location.href =
                                            CTX + 'main/view';
                                    }
                                );

                                return;
                            }


                            if (res.result === 'login') {

                                openAlert(
                                    '로그인 필요',
                                    res.msg || '로그인이 필요합니다.',
                                    function () {

                                        location.href =
                                            CTX + 'login/form';
                                    }
                                );

                                return;
                            }


                            openAlert(
                                '탈퇴 실패',
                                res.msg || '회원탈퇴 처리에 실패했습니다.'
                            );
                        }
                    );
                }
            });
        }
    );
}


/* =====================================================
   재배 분석 리포트
   ===================================================== */

document.querySelectorAll(
    '.analysis-report-item'
).forEach(function (item) {

    item.addEventListener(
        'click',
        function () {

            const analysisId =
                item.dataset.id;


            if (!analysisId) {

                openAlert(
                    '오류',
                    '분석 번호를 찾을 수 없습니다.'
                );

                return;
            }


            location.href =
                CTX
                + 'mypage/analysis/result/'
                + analysisId;
        }
    );
});


/* =====================================================
   병충해 진단 리포트
   ===================================================== */

document.querySelectorAll(
    '.diagnosis-report-item'
).forEach(function (item) {

    item.addEventListener(
        'click',
        function () {

            const diagnosisId =
                item.dataset.id;


            if (!diagnosisId) {

                openAlert(
                    '오류',
                    '진단 번호를 찾을 수 없습니다.'
                );

                return;
            }


            location.href =
                CTX
                + 'mypage/diagnosis/result/'
                + diagnosisId;
        }
    );
});


/* =====================================================
   로그아웃
   ===================================================== */

if (btnLogout) {

    btnLogout.addEventListener(
        'click',
        function () {

            CommonModal.open({
                title: '로그아웃',
                body: '로그아웃하시겠습니까?',
                cancelText: '취소',
                confirmText: '로그아웃',
                cancelType: 'neutral',
                confirmType: 'primary',

                onConfirm: function () {

                    location.href =
                        CTX + 'login/logout';
                }
            });
        }
    );
}


/* =====================================================
   초기 조회
   ===================================================== */

loadFavoriteTradeList();

loadMyTradeList();