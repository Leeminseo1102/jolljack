/* =====================================================
   요소
   ===================================================== */

const dictSearchInput =
    document.getElementById('dictSearchInput');

const dictSearchBtn =
    document.getElementById('dictSearchBtn');

const dictResultCount =
    document.getElementById('dictResultCount');

const dictResultEmpty =
    document.getElementById('dictResultEmpty');

const dictResultList =
    document.getElementById('dictResultList');

const dictPagination =
    document.getElementById('dictPagination');


const dictDetailModal =
    document.getElementById('dictDetailModal');

const dictDetailBackdrop =
    document.getElementById('dictDetailBackdrop');

const dictDetailCloseBtn =
    document.getElementById('dictDetailCloseBtn');

const dictDetailWord =
    document.getElementById('dictDetailWord');

const dictDetailDescription =
    document.getElementById('dictDetailDescription');


/* =====================================================
   검색 상태
   ===================================================== */

const DICT_PAGE_SIZE =
    15;

let currentSearchWord =
    '';

let currentApiPage =
    1;

let currentViewPage =
    1;

let dictResultData =
    [];

let hasMoreApiData =
    true;

let isLoading =
    false;


/* =====================================================
   AJAX
   ===================================================== */

function ajaxGet(url, callback, errorCallback) {

    console.log(
        '[DICT AJAX] 요청 :',
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

        if (xhr.readyState !== 4) {
            return;
        }


        console.log(
            '[DICT AJAX] 응답 status :',
            xhr.status
        );


        if (xhr.status === 200) {

            let res;


            try {

                res =
                    JSON.parse(
                        xhr.responseText
                    );

            } catch (e) {

                console.error(
                    '[DICT AJAX] JSON 변환 실패 :',
                    e
                );


                if (errorCallback) {
                    errorCallback();
                }


                return;
            }


            console.log(
                '[DICT AJAX] 응답 데이터 :',
                res
            );


            callback(
                res
            );


            return;
        }


        console.error(
            '[DICT AJAX] 요청 실패 :',
            xhr.status,
            xhr.responseText
        );


        if (errorCallback) {
            errorCallback();
        }
    };


    xhr.send();
}


/* =====================================================
   상세 모달 열기
   ===================================================== */

function openDictDetail(
    wordNm,
    wordDc
) {

    dictDetailWord.textContent =
        wordNm;

    dictDetailDescription.textContent =
        wordDc
            ? wordDc
            : '등록된 설명이 없습니다.';


    dictDetailModal.hidden =
        false;
}


/* =====================================================
   상세 모달 닫기
   ===================================================== */

function closeDictDetail() {

    dictDetailModal.hidden =
        true;

    dictDetailWord.textContent =
        '';

    dictDetailDescription.textContent =
        '';
}


/* =====================================================
   검색 상태 초기화
   ===================================================== */

function resetDictSearch() {

    currentSearchWord =
        '';

    currentApiPage =
        1;

    currentViewPage =
        1;

    dictResultData =
        [];

    hasMoreApiData =
        true;

    isLoading =
        false;


    dictResultList.innerHTML =
        '';

    dictPagination.innerHTML =
        '';

    dictResultCount.textContent =
        '0';


    closeDictDetail();
}


/* =====================================================
   검색 결과 없음
   ===================================================== */

function showDictEmpty(message) {

    dictResultEmpty.textContent =
        message;

    dictResultEmpty.style.display =
        'flex';
}


function hideDictEmpty() {

    dictResultEmpty.style.display =
        'none';
}


/* =====================================================
   API 검색
   ===================================================== */

function loadDictApiPage(callback) {

    if (
        isLoading
        || !hasMoreApiData
    ) {

        if (callback) {
            callback();
        }


        return;
    }


    isLoading =
        true;


    const requestPage =
        currentApiPage;


    const url =
        CTX
        + 'farmDic/search?word='
        + encodeURIComponent(
            currentSearchWord
        )
        + '&pageNo='
        + encodeURIComponent(
            requestPage
        );


    console.log(
        '[DICT] API 검색 word :',
        currentSearchWord,
        'apiPage :',
        requestPage
    );


    ajaxGet(
        url,

        function (list) {

            isLoading =
                false;


            if (!Array.isArray(list)) {

                hasMoreApiData =
                    false;


                if (callback) {
                    callback();
                }


                return;
            }


            if (list.length === 0) {

                hasMoreApiData =
                    false;


                if (callback) {
                    callback();
                }


                return;
            }


            dictResultData.push(
                ...list
            );


            currentApiPage++;


            dictResultCount.textContent =
                dictResultData.length;


            console.log(
                '[DICT] 현재 저장된 검색 결과 :',
                dictResultData.length
            );


            if (callback) {
                callback();
            }
        },

        function () {

            isLoading =
                false;

            hasMoreApiData =
                false;


            showDictEmpty(
                '농업 용어를 불러오는 중 오류가 발생했습니다.'
            );
        }
    );
}


/* =====================================================
   검색 시작
   ===================================================== */

function searchDictionary(word) {

    const searchWord =
        word.trim();


    resetDictSearch();


    if (!searchWord) {

        showDictEmpty(
            '검색할 농업 용어를 입력해주세요.'
        );


        return;
    }


    currentSearchWord =
        searchWord;


    showDictEmpty(
        '농업 용어를 검색하고 있습니다.'
    );


    loadDictApiPage(
        function () {

            if (dictResultData.length === 0) {

                showDictEmpty(
                    '검색된 농업 용어가 없습니다.'
                );


                return;
            }


            showDictPage(
                1
            );
        }
    );
}


/* =====================================================
   화면 페이지 데이터 확보
   ===================================================== */

function ensureDictPageData(
    page,
    callback
) {

    const requiredCount =
        page
        * DICT_PAGE_SIZE;


    if (
        dictResultData.length >= requiredCount
        || !hasMoreApiData
    ) {

        callback();


        return;
    }


    loadDictApiPage(
        function () {

            if (
                dictResultData.length < requiredCount
                && hasMoreApiData
            ) {

                ensureDictPageData(
                    page,
                    callback
                );


                return;
            }


            callback();
        }
    );
}


/* =====================================================
   검색 결과 렌더링
   ===================================================== */

function renderDictResultList(page) {

    const startIndex =
        (page - 1)
        * DICT_PAGE_SIZE;

    const endIndex =
        startIndex
        + DICT_PAGE_SIZE;


    const pageList =
        dictResultData.slice(
            startIndex,
            endIndex
        );


    dictResultList.innerHTML =
        '';


    if (pageList.length === 0) {

        showDictEmpty(
            '더 이상 검색 결과가 없습니다.'
        );


        return;
    }


    hideDictEmpty();


    pageList.forEach(
        function (item) {

            const button =
                document.createElement(
                    'button'
                );


            button.type =
                'button';

            button.className =
                'dict-result-item';

            button.dataset.wordNo =
                item.wordNo;

            button.dataset.wordNm =
                item.wordNm;


            const word =
                document.createElement(
                    'strong'
                );


            word.className =
                'dict-result-word';

            word.textContent =
                item.wordNm;


            const arrow =
                document.createElement(
                    'span'
                );


            arrow.className =
                'dict-result-arrow';

            arrow.textContent =
                '›';


            button.appendChild(
                word
            );

            button.appendChild(
                arrow
            );


            dictResultList.appendChild(
                button
            );
        }
    );
}


/* =====================================================
   페이지 버튼
   ===================================================== */

function createPageButton(page) {

    const button =
        document.createElement(
            'button'
        );


    button.type =
        'button';

    button.className =
        'dict-page-btn';

    button.dataset.page =
        page;

    button.textContent =
        page;


    if (page === currentViewPage) {

        button.classList.add(
            'active'
        );
    }


    return button;
}


/* =====================================================
   페이지네이션
   ===================================================== */

function renderDictPagination() {

    dictPagination.innerHTML =
        '';


    const loadedPages =
        Math.ceil(
            dictResultData.length
            / DICT_PAGE_SIZE
        );


    if (
        loadedPages <= 1
        && !hasMoreApiData
    ) {
        return;
    }


    const prevButton =
        document.createElement(
            'button'
        );


    prevButton.type =
        'button';

    prevButton.className =
        'dict-page-btn dict-page-control';

    prevButton.dataset.action =
        'prev';

    prevButton.textContent =
        '‹';

    prevButton.disabled =
        currentViewPage === 1;


    dictPagination.appendChild(
        prevButton
    );


    let startPage =
        Math.max(
            1,
            currentViewPage - 2
        );

    let endPage =
        Math.min(
            loadedPages,
            startPage + 4
        );


    startPage =
        Math.max(
            1,
            endPage - 4
        );


    for (
        let page = startPage;
        page <= endPage;
        page++
    ) {

        dictPagination.appendChild(
            createPageButton(
                page
            )
        );
    }


    const nextButton =
        document.createElement(
            'button'
        );


    nextButton.type =
        'button';

    nextButton.className =
        'dict-page-btn dict-page-control';

    nextButton.dataset.action =
        'next';

    nextButton.textContent =
        '›';

    nextButton.disabled =
        currentViewPage >= loadedPages
        && !hasMoreApiData;


    dictPagination.appendChild(
        nextButton
    );
}


/* =====================================================
   페이지 이동
   ===================================================== */

function showDictPage(page) {

    if (page < 1) {
        return;
    }


    ensureDictPageData(
        page,

        function () {

            const startIndex =
                (page - 1)
                * DICT_PAGE_SIZE;


            if (
                startIndex
                >= dictResultData.length
            ) {

                renderDictPagination();


                return;
            }


            currentViewPage =
                page;


            renderDictResultList(
                currentViewPage
            );

            renderDictPagination();
        }
    );
}


/* =====================================================
   상세 조회
   ===================================================== */

function loadDictDetail(
    wordNo,
    wordNm
) {

    if (!wordNo) {
        return;
    }


    const url =
        CTX
        + 'farmDic/detail?wordNo='
        + encodeURIComponent(
            wordNo
        );


    console.log(
        '[DICT] 상세 조회 wordNo :',
        wordNo
    );


    ajaxGet(
        url,

        function (res) {

            if (!res) {

                openDictDetail(
                    wordNm,
                    ''
                );


                return;
            }


            openDictDetail(
                wordNm,
                res.wordDc
            );
        },

        function () {

            console.error(
                '[DICT] 상세 정보를 불러오지 못했습니다.'
            );


            openDictDetail(
                wordNm,
                '상세 정보를 불러오지 못했습니다.'
            );
        }
    );
}


/* =====================================================
   검색 버튼
   ===================================================== */

dictSearchBtn.addEventListener(
    'click',

    function () {

        searchDictionary(
            dictSearchInput.value
        );
    }
);


/* =====================================================
   Enter 검색
   ===================================================== */

dictSearchInput.addEventListener(
    'keydown',

    function (e) {

        if (e.key !== 'Enter') {
            return;
        }


        searchDictionary(
            dictSearchInput.value
        );
    }
);


/* =====================================================
   검색 결과 클릭
   ===================================================== */

dictResultList.addEventListener(
    'click',

    function (e) {

        const item =
            e.target.closest(
                '.dict-result-item'
            );


        if (!item) {
            return;
        }


        loadDictDetail(
            item.dataset.wordNo,
            item.dataset.wordNm
        );
    }
);


/* =====================================================
   페이지 클릭
   ===================================================== */

dictPagination.addEventListener(
    'click',

    function (e) {

        const button =
            e.target.closest(
                '.dict-page-btn'
            );


        if (
            !button
            || button.disabled
        ) {
            return;
        }


        const action =
            button.dataset.action;


        if (action === 'prev') {

            showDictPage(
                currentViewPage - 1
            );


            return;
        }


        if (action === 'next') {

            showDictPage(
                currentViewPage + 1
            );


            return;
        }


        const page =
            Number(
                button.dataset.page
            );


        if (!page) {
            return;
        }


        showDictPage(
            page
        );
    }
);


/* =====================================================
   상세 모달 닫기
   ===================================================== */

dictDetailCloseBtn.addEventListener(
    'click',

    function () {

        closeDictDetail();
    }
);


dictDetailBackdrop.addEventListener(
    'click',

    function () {

        closeDictDetail();
    }
);


/* =====================================================
   ESC 상세 모달 닫기
   ===================================================== */

document.addEventListener(
    'keydown',

    function (e) {

        if (
            e.key !== 'Escape'
            || dictDetailModal.hidden
        ) {
            return;
        }


        closeDictDetail();
    }
);