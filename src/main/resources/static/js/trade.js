const tradeEmpty =
    document.getElementById('tradeEmpty');

const tradeContent =
    document.getElementById('tradeContent');

const tradeList =
    document.getElementById('tradeList');

const tradePagination =
    document.getElementById('tradePagination');

const tradeRegionBtn =
    document.getElementById('tradeRegionBtn');

const tradeRegionName =
    document.getElementById('tradeRegionName');

const tradeRegionCount =
    document.getElementById('tradeRegionCount');

const currentSidoInput =
    document.getElementById('currentSidoName');

const tradeTopList =
    document.querySelector('.trade-top-list');


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

const tradeDetailQuantity =
    document.getElementById('tradeDetailQuantity');

const tradeDetailContent =
    document.getElementById('tradeDetailContent');

const tradeDetailSellerName =
    document.getElementById('tradeDetailSellerName');

const tradeDetailFavoriteBtn =
    document.getElementById('tradeDetailFavoriteBtn');

const tradeDetailFavoriteIcon =
    document.getElementById('tradeDetailFavoriteIcon');

const tradeDetailFavoriteCount =
    document.getElementById('tradeDetailFavoriteCount');


let currentSidoName =
    currentSidoInput.value;

let selectedSidoName =
    currentSidoName;


console.log(
    '[TRADE] 초기 거래 지역 :',
    currentSidoName
);


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
   AJAX
   ===================================================== */

function ajaxGet(url, callback) {

    console.log(
        '[TRADE AJAX] 요청 :',
        url
    );


    const xhr =
        new XMLHttpRequest();


    xhr.open(
        'GET',
        url,
        true
    );


    xhr.onreadystatechange = function () {

        if (xhr.readyState === 4) {

            console.log(
                '[TRADE AJAX] 응답 status :',
                xhr.status
            );


            if (xhr.status === 200) {

                const res =
                    JSON.parse(xhr.responseText);


                console.log(
                    '[TRADE AJAX] 응답 데이터 :',
                    res
                );


                callback(res);

            } else {

                console.error(
                    '[TRADE AJAX] 요청 실패 :',
                    xhr.status,
                    xhr.responseText
                );


                openAlert(
                    '오류',
                    '거래 게시글을 불러오지 못했습니다.'
                );
            }
        }
    };


    xhr.send();
}


function ajaxPost(url, data, callback) {

    console.log(
        '[TRADE AJAX] POST 요청 :',
        url
    );


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

            console.log(
                '[TRADE AJAX] POST 응답 status :',
                xhr.status
            );


            if (xhr.status === 200) {

                const res =
                    JSON.parse(xhr.responseText);


                console.log(
                    '[TRADE AJAX] POST 응답 데이터 :',
                    res
                );


                callback(res);

            } else {

                console.error(
                    '[TRADE AJAX] POST 요청 실패 :',
                    xhr.status,
                    xhr.responseText
                );


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
   가격
   ===================================================== */

function formatPrice(price) {

    if (price == null) {
        return '';
    }


    return Number(price)
            .toLocaleString('ko-KR')
        + '원';
}


/* =====================================================
   날짜
   ===================================================== */

function formatDate(createdAt) {

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
   관심 등록 TOP 5
   ===================================================== */

function renderTradeFavoriteTop5(list) {

    if (!tradeTopList) {
        return;
    }


    tradeTopList.innerHTML =
        '';


    list.forEach(function (item, index) {

        const topItem =
            document.createElement('div');


        topItem.className =
            'trade-top-item';


        const rank =
            document.createElement('span');


        rank.className =
            'trade-top-rank';

        rank.textContent =
            index + 1;


        const info =
            document.createElement('div');


        info.className =
            'trade-top-info';


        const title =
            document.createElement('strong');


        title.textContent =
            item.title;


        const price =
            document.createElement('span');


        price.textContent =
            formatPrice(item.price);


        const favorite =
            document.createElement('span');


        favorite.className =
            'trade-top-heart';

        favorite.textContent =
            '♥ ' + item.favoriteCount;


        info.appendChild(
            title
        );

        info.appendChild(
            price
        );


        topItem.appendChild(
            rank
        );

        topItem.appendChild(
            info
        );

        topItem.appendChild(
            favorite
        );


        tradeTopList.appendChild(
            topItem
        );
    });
}


function loadTradeFavoriteTop5() {

    if (!tradeTopList) {
        return;
    }


    ajaxGet(
        CTX + 'trade/getTradeFavoriteTop5',
        function (list) {

            if (!Array.isArray(list)) {
                return;
            }


            renderTradeFavoriteTop5(
                list
            );
        }
    );
}


/* =====================================================
   거래 상세
   ===================================================== */

function renderTradePostDetail(item) {

    tradeDetailImage.src =
        item.imageUrl;

    tradeDetailTitle.textContent =
        item.title;

    tradeDetailPrice.textContent =
        formatPrice(item.price);

    tradeDetailRegion.textContent =
        item.fullRegionName;

    tradeDetailDate.textContent =
        formatDate(item.createdAt);

    tradeDetailStatus.textContent =
        formatTradeStatus(item.status);

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


    tradeDetailModal.hidden =
        false;
}


function closeTradeDetailModal() {

    tradeDetailModal.hidden =
        true;
}


function loadTradePostDetail(tradePostId) {

    console.log(
        '[TRADE] 상세 조회 tradePostId :',
        tradePostId
    );


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


            renderTradePostDetail(res);
        }
    );
}


/* =====================================================
   게시글 목록
   ===================================================== */

function updateTradeItemFavorite(article, favorite) {

    const favoriteIcon =
        article.querySelector(
            '.trade-item-favorite'
        );


    if (favorite) {

        if (favoriteIcon) {
            return;
        }


        const newFavoriteIcon =
            document.createElement('span');


        newFavoriteIcon.className =
            'trade-item-favorite';

        newFavoriteIcon.textContent =
            '♥';


        article.appendChild(
            newFavoriteIcon
        );

        return;
    }


    if (favoriteIcon) {

        favoriteIcon.remove();
    }
}


function updateTradeListFavorite(tradePostId, favorite) {

    const article =
        tradeList.querySelector(
            '.trade-item[data-id="'
            + tradePostId
            + '"]'
        );


    if (!article) {
        return;
    }


    updateTradeItemFavorite(
        article,
        favorite
    );
}


function renderTradeList(list) {

    console.log(
        '[TRADE] 거래글 렌더링 건수 :',
        list.length
    );


    tradeList.innerHTML =
        '';


    list.forEach(function (item) {

        const article =
            document.createElement('article');


        article.className =
            'trade-item';

        article.dataset.id =
            item.tradePostId;


        const image =
            document.createElement('img');


        image.className =
            'trade-item-image';

        image.src =
            item.imageUrl;

        image.alt =
            '상품 이미지';


        const info =
            document.createElement('div');


        info.className =
            'trade-item-info';


        const title =
            document.createElement('h2');


        title.className =
            'trade-item-title';

        title.textContent =
            item.title;


        const price =
            document.createElement('p');


        price.className =
            'trade-item-price';

        price.textContent =
            formatPrice(item.price);


        info.appendChild(
            title
        );

        info.appendChild(
            price
        );


        if (item.quantity != null) {

            const quantity =
                document.createElement('span');


            quantity.className =
                'trade-item-quantity';

            quantity.textContent =
                '수량 ' + item.quantity;


            info.appendChild(
                quantity
            );
        }


        const date =
            document.createElement('span');


        date.className =
            'trade-item-date';

        date.textContent =
            formatDate(item.createdAt);


        info.appendChild(
            date
        );


        article.appendChild(
            image
        );

        article.appendChild(
            info
        );


        updateTradeItemFavorite(
            article,
            item.favorite
        );


        tradeList.appendChild(
            article
        );
    });
}


/* =====================================================
   페이징
   ===================================================== */

function renderPagination(page, totalPages) {

    console.log(
        '[TRADE] 페이징 렌더링 page :',
        page,
        'totalPages :',
        totalPages
    );


    tradePagination.innerHTML =
        '';


    if (totalPages <= 1) {

        tradePagination.style.display =
            'none';

        return;
    }


    tradePagination.style.display =
        'flex';


    for (let i = 1; i <= totalPages; i++) {

        const button =
            document.createElement('button');


        button.type =
            'button';

        button.className =
            'trade-page-btn';

        button.dataset.page =
            i;

        button.textContent =
            i;


        if (i === page) {

            button.classList.add(
                'active'
            );
        }


        tradePagination.appendChild(
            button
        );
    }
}


/* =====================================================
   화면 갱신
   ===================================================== */

function renderTradePage(res) {

    console.log(
        '[TRADE] 화면 갱신 totalCount :',
        res.totalCount
    );


    tradeRegionCount.textContent =
        res.totalCount + '개';


    if (res.totalCount === 0) {

        tradeList.innerHTML =
            '';

        tradePagination.innerHTML =
            '';

        tradePagination.style.display =
            'none';

        tradeContent.style.display =
            'none';

        tradeEmpty.style.display =
            'flex';


        return;
    }


    tradeEmpty.style.display =
        'none';

    tradeContent.style.display =
        'block';


    renderTradeList(
        res.tradeList
    );


    renderPagination(
        res.page,
        res.totalPages
    );
}


/* =====================================================
   거래글 AJAX 조회
   ===================================================== */

function loadTradePage(page, sidoName, callback) {

    console.log(
        '[TRADE] loadTradePage page :',
        page,
        'sidoName :',
        sidoName
    );


    let url =
        CTX
        + 'trade/getTradePostList?page='
        + encodeURIComponent(page);


    if (sidoName) {

        url +=
            '&sidoName='
            + encodeURIComponent(sidoName);
    }


    ajaxGet(
        url,
        function (res) {

            renderTradePage(
                res
            );


            if (callback) {

                callback(
                    res
                );
            }
        }
    );
}


/* =====================================================
   거래 상세 열기 / 닫기
   ===================================================== */

tradeList.addEventListener(
    'click',
    function (e) {

        const article =
            e.target.closest(
                '.trade-item'
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
            tradePostId
        );
    }
);


tradeDetailCloseBtn.addEventListener(
    'click',
    function () {

        closeTradeDetailModal();
    }
);


tradeDetailBackdrop.addEventListener(
    'click',
    function () {

        closeTradeDetailModal();
    }
);


/* =====================================================
   관심 등록 / 취소
   ===================================================== */

tradeDetailFavoriteBtn.addEventListener(
    'click',
    function () {

        const tradePostId =
            tradeDetailFavoriteBtn.dataset.id;


        if (!tradePostId) {

            console.warn(
                '[TRADE] 관심 처리 tradePostId가 없습니다.'
            );

            return;
        }


        console.log(
            '[TRADE] 관심 처리 tradePostId :',
            tradePostId
        );


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


                updateTradeListFavorite(
                    tradePostId,
                    res.favorite
                );


                loadTradeFavoriteTop5();


                console.log(
                    '[TRADE] 관심 처리 완료 favorite :',
                    res.favorite,
                    'favoriteCount :',
                    res.favoriteCount
                );
            }
        );
    }
);


/* =====================================================
   페이지 이동
   ===================================================== */

tradePagination.addEventListener(
    'click',
    function (e) {

        const button =
            e.target.closest(
                '.trade-page-btn'
            );


        if (!button) {
            return;
        }


        if (button.classList.contains('active')) {
            return;
        }


        const page =
            button.dataset.page;


        console.log(
            '[TRADE] 페이지 클릭 :',
            page,
            '현재 지역 :',
            currentSidoName
        );


        loadTradePage(
            page,
            currentSidoName
        );
    }
);


/* =====================================================
   지역 모달 요소
   ===================================================== */

function getTradeSidoSelect() {

    const modalBody =
        document.getElementById(
            'commonModalBody'
        );


    if (!modalBody) {

        console.error(
            '[TRADE] commonModalBody를 찾을 수 없습니다.'
        );

        return null;
    }


    return modalBody.querySelector(
        '#tradeSidoSelect'
    );
}


/* =====================================================
   지역 변경
   ===================================================== */

tradeRegionBtn.addEventListener(
    'click',
    function () {

        console.log(
            '[TRADE] 지역 변경 버튼 클릭'
        );


        const template =
            document.getElementById(
                'tradeRegionModalTemplate'
            );


        if (!template) {

            console.error(
                '[TRADE] tradeRegionModalTemplate을 찾을 수 없습니다.'
            );

            return;
        }


        selectedSidoName =
            currentSidoName;


        console.log(
            '[TRADE] 모달 열기 현재 지역 :',
            currentSidoName
        );


        CommonModal.open({

            title: '거래 지역 선택',

            body: template.innerHTML,

            confirmText: '선택',

            onConfirm: function () {

                console.log(
                    '[TRADE] 지역 선택 확인 :',
                    selectedSidoName
                );


                if (!selectedSidoName) {

                    console.warn(
                        '[TRADE] 선택된 시도가 없음'
                    );


                    openAlert(
                        '지역 선택',
                        '거래 지역을 선택해주세요.'
                    );


                    return;
                }


                loadTradePage(
                    1,
                    selectedSidoName,
                    function () {

                        currentSidoName =
                            selectedSidoName;


                        currentSidoInput.value =
                            selectedSidoName;


                        tradeRegionName.textContent =
                            selectedSidoName;


                        console.log(
                            '[TRADE] 거래 지역 변경 완료 :',
                            currentSidoName
                        );
                    }
                );
            }
        });


        const sidoSelect =
            getTradeSidoSelect();


        if (!sidoSelect) {

            console.error(
                '[TRADE] tradeSidoSelect를 찾을 수 없습니다.'
            );

            return;
        }


        if (currentSidoName) {

            sidoSelect.value =
                currentSidoName;
        }


        console.log(
            '[TRADE] 모달 select 초기값 :',
            sidoSelect.value
        );


        sidoSelect.addEventListener(
            'change',
            function () {

                selectedSidoName =
                    sidoSelect.value;


                console.log(
                    '[TRADE] 지역 선택 변경 :',
                    selectedSidoName
                );
            }
        );
    }
);


/* =====================================================
   초기 조회
   ===================================================== */

loadTradeFavoriteTop5();