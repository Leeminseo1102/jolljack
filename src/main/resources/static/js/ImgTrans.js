const btnLogout = document.getElementById('btnLogout');

const uploadBox = document.getElementById('uploadBox');
const imageInput = document.getElementById('imageInput');
const uploadPlaceholder = document.getElementById('uploadPlaceholder');
const previewArea = document.getElementById('previewArea');
const previewImage = document.getElementById('previewImage');

const imageMsg = document.getElementById('imageMsg');
const btnResetImage = document.getElementById('btnResetImage');
const btnDiagnosis = document.getElementById('btnDiagnosis');

const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

const ALLOWED_IMAGE_TYPES = [
    'image/jpeg',
    'image/png',
    'image/webp'
];

let selectedImageFile = null;
let isUploading = false;

/* =====================================================
   공통
   ===================================================== */

function showMsg(el, msg, type) {
    el.textContent = msg;
    el.className = 'msg-' + type;
}

function clearMsg(el) {
    el.textContent = '';
    el.className = '';
}


/* =====================================================
   이미지 검증
   ===================================================== */

function validateImageFile(file) {
    if (!file) {
        return {
            valid: false,
            msg: '업로드할 이미지를 선택해주세요.'
        };
    }

    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
        return {
            valid: false,
            msg: 'jpg, png, webp 형식의 이미지만 업로드할 수 있습니다.'
        };
    }

    if (file.size > MAX_IMAGE_SIZE) {
        return {
            valid: false,
            msg: '이미지 파일은 최대 5MB까지 업로드할 수 있습니다.'
        };
    }

    return {
        valid: true,
        msg: ''
    };
}

/* =====================================================
   미리보기
   ===================================================== */

function setSelectedImage(file) {
    clearMsg(imageMsg);

    const validResult = validateImageFile(file);

    if (!validResult.valid) {
        resetImage();
        showMsg(imageMsg, validResult.msg, 'error');
        return;
    }

    selectedImageFile = file;

    const reader = new FileReader();

    reader.onload = function (e) {
        previewImage.src = e.target.result;

        uploadPlaceholder.style.display = 'none';
        previewArea.style.display = 'block';

        uploadBox.classList.add('has-image');

        btnResetImage.disabled = false;
        btnDiagnosis.disabled = false;
    };

    reader.onerror = function () {
        resetImage();
        showMsg(imageMsg, '이미지 미리보기를 불러오지 못했습니다.', 'error');
    };

    reader.readAsDataURL(file);
}

function resetImage() {
    selectedImageFile = null;

    imageInput.value = '';
    previewImage.src = '';

    uploadPlaceholder.style.display = 'flex';
    previewArea.style.display = 'none';

    uploadBox.classList.remove('has-image');
    uploadBox.classList.remove('drag-over');

    btnResetImage.disabled = true;
    btnDiagnosis.disabled = true;
}

/* =====================================================
   이미지 업로드 AJAX
   기존 ajax 흐름 유지 + 이미지라서 FormData 사용
   Content-Type 직접 설정 금지
   ===================================================== */

function ajaxImagePost(url, file, callback) {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();

    formData.append('image', file);

    xhr.open('POST', url, true);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            isUploading = false;
            btnDiagnosis.disabled = false;
            btnResetImage.disabled = false;
            btnDiagnosis.textContent = '진단 시작';

            if (xhr.status === 200) {
                const res = JSON.parse(xhr.responseText);
                callback(res);
                return;
            }

            callback({
                result: 'FAIL',
                msg: '이미지 진단 처리 중 오류가 발생했습니다.'
            });
        }
    };

    xhr.onerror = function () {
        isUploading = false;
        btnDiagnosis.disabled = false;
        btnResetImage.disabled = false;
        btnDiagnosis.textContent = '진단 시작';

        callback({
            result: 'FAIL',
            msg: '서버와 통신 중 오류가 발생했습니다.'
        });
    };

    xhr.send(formData);
}

/* =====================================================
   응답 처리
   ===================================================== */

function handleUploadResponse(res) {
    if (res.result === 'UPLOAD_OK') {
        location.href = CTX + 'diag/loading';
        return;
    }

    if (res.result === 'LOGIN_REQUIRED') {
        CommonModal.open({
            title: '로그인이 필요합니다',
            body: res.msg || '병충해 진단은 로그인 후 이용할 수 있습니다.',
            confirmText: '로그인으로',
            hideCancel: true,
            onConfirm: function () {
                location.href = CTX + 'login/form';
            }
        });
        return;
    }

    CommonModal.open({
        title: '업로드 실패',
        body: res.msg || '이미지 업로드 중 오류가 발생했습니다.',
        confirmText: '확인',
        hideCancel: true
    });
}

/* =====================================================
   이벤트
   ===================================================== */

imageInput.addEventListener('change', function () {
    clearMsg(imageMsg);

    const file = imageInput.files[0];

    if (!file) {
        resetImage();
        return;
    }

    setSelectedImage(file);
});

uploadBox.addEventListener('dragover', function (e) {
    e.preventDefault();

    if (isUploading) {
        return;
    }

    uploadBox.classList.add('drag-over');
});

uploadBox.addEventListener('dragleave', function (e) {
    e.preventDefault();
    uploadBox.classList.remove('drag-over');
});

uploadBox.addEventListener('drop', function (e) {
    e.preventDefault();
    uploadBox.classList.remove('drag-over');

    if (isUploading) {
        return;
    }

    const file = e.dataTransfer.files[0];

    if (!file) {
        return;
    }

    imageInput.files = e.dataTransfer.files;
    setSelectedImage(file);
});

btnResetImage.addEventListener('click', function () {
    if (isUploading) {
        return;
    }

    clearMsg(imageMsg);
    resetImage();
});

btnDiagnosis.addEventListener('click', function () {
    clearMsg(imageMsg);

    if (isUploading) {
        return;
    }

    const validResult = validateImageFile(selectedImageFile);

    if (!validResult.valid) {
        showMsg(imageMsg, validResult.msg, 'error');
        return;
    }

    isUploading = true;
    btnDiagnosis.disabled = true;
    btnResetImage.disabled = true;
    btnDiagnosis.textContent = '진단 중...';

    ajaxImagePost(CTX + 'diag/uploadProc', selectedImageFile, function (res) {
        handleUploadResponse(res);
    });
});

/* =====================================================
   시작
   ===================================================== */

document.addEventListener('DOMContentLoaded', function () {
    if (typeof bindLogout === 'function') {
        bindLogout();
    }
});