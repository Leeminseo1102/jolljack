const tradeRegisterForm = document.getElementById('tradeRegisterForm');

const tradeImage = document.getElementById('tradeImage');
const tradeImagePreview = document.getElementById('tradeImagePreview');
const tradeImagePlaceholder = document.getElementById('tradeImagePlaceholder');
const tradeImageMsg = document.getElementById('tradeImageMsg');

const tradeTitle = document.getElementById('tradeTitle');
const tradePrice = document.getElementById('tradePrice');
const tradeQuantity = document.getElementById('tradeQuantity');
const tradeContent = document.getElementById('tradeContent');

const tradeSidoSelect = document.getElementById('tradeSidoSelect');
const tradeSigunguSelect = document.getElementById('tradeSigunguSelect');
const tradeRegionMsg = document.getElementById('tradeRegionMsg');

const tradeRegisterBtn = document.getElementById('tradeRegisterBtn');

const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

const ALLOWED_IMAGE_TYPES = [
    'image/jpeg',
    'image/png',
    'image/webp'
];

let previewUrl = null;


/* ===== 공통 ===== */

function ajaxGet(url, callback) {
    const xhr = new XMLHttpRequest();

    xhr.open('GET', url, true);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const res = JSON.parse(xhr.responseText);
                callback(res);
            } else {
                openAlert('오류', '요청 처리 중 오류가 발생했습니다.');
            }
        }
    };

    xhr.send();
}

function openAlert(title, body, onConfirm) {
    CommonModal.open({
        title: title,
        body: body,
        hideCancel: true,
        confirmText: '확인',
        onConfirm: onConfirm
    });
}

function showMsg(el, msg) {
    if (!el) {
        return;
    }

    el.textContent = msg;
}

function clearMsg(el) {
    if (!el) {
        return;
    }

    el.textContent = '';
}

function showFieldError(element, message) {
    if (!element) {
        return;
    }

    element.classList.add('is-invalid');

    element.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
    });

    openAlert('상품 등록', message, function () {
        element.focus();
    });
}

function clearFieldError(element) {
    if (!element) {
        return;
    }

    element.classList.remove('is-invalid');
}


/* ===== 이미지 ===== */

function resetImagePreview() {
    if (previewUrl !== null) {
        URL.revokeObjectURL(previewUrl);
        previewUrl = null;
    }

    tradeImagePreview.src = '';
    tradeImagePreview.hidden = true;

    tradeImagePlaceholder.style.display = '';
}

function resetImage() {
    tradeImage.value = '';

    resetImagePreview();
}

function validateImage(file) {
    if (!file) {
        return {
            valid: false,
            msg: '대표 이미지를 등록해주세요.'
        };
    }

    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
        return {
            valid: false,
            msg: 'JPG, PNG, WEBP 이미지만 등록할 수 있습니다.'
        };
    }

    if (file.size > MAX_IMAGE_SIZE) {
        return {
            valid: false,
            msg: '이미지는 5MB 이하만 등록할 수 있습니다.'
        };
    }

    return {
        valid: true,
        msg: ''
    };
}

function showImageError(message) {
    const imageUpload = document.querySelector('.trade-image-upload');

    if (imageUpload) {
        imageUpload.classList.add('is-invalid');

        imageUpload.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
        });
    }

    showMsg(tradeImageMsg, message);

    openAlert('상품 등록', message);
}

function clearImageError() {
    const imageUpload = document.querySelector('.trade-image-upload');

    if (imageUpload) {
        imageUpload.classList.remove('is-invalid');
    }

    clearMsg(tradeImageMsg);
}

tradeImage.addEventListener('change', function () {
    const file = tradeImage.files[0];

    clearImageError();

    if (!file) {
        resetImagePreview();
        return;
    }

    const result = validateImage(file);

    if (!result.valid) {
        resetImage();
        showImageError(result.msg);
        return;
    }

    if (previewUrl !== null) {
        URL.revokeObjectURL(previewUrl);
    }

    previewUrl = URL.createObjectURL(file);

    tradeImagePreview.src = previewUrl;
    tradeImagePreview.hidden = false;

    tradeImagePlaceholder.style.display = 'none';

    console.log('[TRADE REGISTER] 이미지 선택 :', file.name);
});


/* ===== 지역 선택 ===== */

function resetSigunguSelect() {
    tradeSigunguSelect.innerHTML =
        '<option value="">시군구를 선택해주세요</option>';

    tradeSigunguSelect.disabled = true;

    clearFieldError(tradeSigunguSelect);
}

tradeSidoSelect.addEventListener('change', function () {
    const sidoName = tradeSidoSelect.value;

    console.log('[TRADE REGISTER] 시도 선택 :', sidoName);

    clearFieldError(tradeSidoSelect);
    clearMsg(tradeRegionMsg);

    resetSigunguSelect();

    if (sidoName === '') {
        return;
    }

    ajaxGet(
        CTX + 'trade/getSigunguList?sidoName=' + encodeURIComponent(sidoName),
        function (list) {

            console.log('[TRADE REGISTER] 시군구 조회 결과 :', list);

            if (!Array.isArray(list) || list.length === 0) {
                showMsg(
                    tradeRegionMsg,
                    '조회 가능한 시군구가 없습니다.'
                );

                return;
            }

            list.forEach(function (item) {
                const option = document.createElement('option');

                option.value = item.regionId;
                option.textContent = item.sigunguName;

                tradeSigunguSelect.appendChild(option);
            });

            tradeSigunguSelect.disabled = false;
        }
    );
});

tradeSigunguSelect.addEventListener('change', function () {
    clearFieldError(tradeSigunguSelect);
    clearMsg(tradeRegionMsg);
});


/* ===== 입력 오류 해제 ===== */

tradeTitle.addEventListener('input', function () {
    clearFieldError(tradeTitle);
});

tradePrice.addEventListener('input', function () {
    clearFieldError(tradePrice);
});

tradeQuantity.addEventListener('input', function () {
    clearFieldError(tradeQuantity);
});

tradeContent.addEventListener('input', function () {
    clearFieldError(tradeContent);
});


/* ===== 상품 등록 ===== */

tradeRegisterForm.addEventListener('submit', function (e) {
    e.preventDefault();

    const file = tradeImage.files[0];

    const title = tradeTitle.value.trim();
    const priceValue = tradePrice.value.trim();
    const quantityValue = tradeQuantity.value.trim();
    const content = tradeContent.value.trim();

    const sidoName = tradeSidoSelect.value;
    const regionId = tradeSigunguSelect.value;

    const price = Number(priceValue);
    const quantity = Number(quantityValue);

    const imageResult = validateImage(file);

    if (!imageResult.valid) {
        showImageError(imageResult.msg);
        return;
    }

    if (title === '') {
        showFieldError(
            tradeTitle,
            '상품 제목을 입력해주세요.'
        );

        return;
    }

    if (priceValue === '' || Number.isNaN(price) || price < 0) {
        showFieldError(
            tradePrice,
            '올바른 가격을 입력해주세요.'
        );

        return;
    }

    if (quantityValue === '' || Number.isNaN(quantity) || quantity < 1) {
        showFieldError(
            tradeQuantity,
            '수량을 1개 이상 입력해주세요.'
        );

        return;
    }

    if (sidoName === '') {
        showFieldError(
            tradeSidoSelect,
            '시도를 선택해주세요.'
        );

        return;
    }

    if (regionId === '') {
        showFieldError(
            tradeSigunguSelect,
            '시군구를 선택해주세요.'
        );

        return;
    }

    if (content === '') {
        showFieldError(
            tradeContent,
            '상품 설명을 입력해주세요.'
        );

        return;
    }

    tradeRegisterBtn.disabled = true;
    tradeRegisterBtn.textContent = '등록 중...';

    console.log('[TRADE REGISTER] 상품 등록 요청');
    console.log('[TRADE REGISTER] regionId :', regionId);

    tradeRegisterForm.submit();
});