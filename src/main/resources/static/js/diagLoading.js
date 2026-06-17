const SCENE_W = 480;
const SCENE_H = 280;
const OBJ_W = 56;
const OBJ_H = 56;
const GRASS_H = 62;
const TRAVEL = 5000;

const sunEl = document.getElementById('sun');
const cloudEl = document.getElementById('cloud');
const plantImg = document.getElementById('plantImg');
const dotsEl = document.getElementById('dots');

const START_CX = SCENE_W + (OBJ_W / 2);
const END_CX = -(OBJ_W / 2);
const BASE_CY = SCENE_H - GRASS_H - 20;
const MID_CX = (START_CX + END_CX) / 2;
const RX = (START_CX - END_CX) / 2;
const MAX_RY = BASE_CY - OBJ_H;
const RY = Math.min(BASE_CY * 0.85, MAX_RY);

let dotCount = 0;
let timerInterval = null;
let plantScale = 1.0;
let plantDir = -1;
let lastPlantTime = null;
let objIndex = 0;
let isProcessing = false;

function ajaxPost(url, data, callback) {
    const xhr = new XMLHttpRequest();

    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const res = JSON.parse(xhr.responseText);
                callback(res);
            } else {
                callback({
                    result: 'FAIL',
                    msg: '병충해 진단 처리 중 오류가 발생했습니다.'
                });
            }
        }
    };

    const params = Object.entries(data)
        .map(function ([key, value]) {
            return encodeURIComponent(key) + '=' + encodeURIComponent(value);
        })
        .join('&');

    xhr.send(params);
}

function startDots() {
    timerInterval = setInterval(function () {
        dotCount = (dotCount + 1) % 4;
        dotsEl.textContent = '.'.repeat(dotCount);
    }, 500);
}

function stopDots() {
    clearInterval(timerInterval);
}

function animatePlant(ts) {
    if (!lastPlantTime) {
        lastPlantTime = ts;
    }

    const delta = (ts - lastPlantTime) / 1000;
    lastPlantTime = ts;

    plantScale += plantDir * delta * 0.38;

    if (plantScale <= 0.55) {
        plantScale = 0.55;
        plantDir = 1;
    }

    if (plantScale >= 1.0) {
        plantScale = 1.0;
        plantDir = -1;
    }

    plantImg.style.transform = 'scale(' + plantScale + ')';

    requestAnimationFrame(animatePlant);
}

function easeInOutSine(t) {
    return -(Math.cos(Math.PI * t) - 1) / 2;
}

function flyObject() {
    const isSun = objIndex % 2 === 0;

    sunEl.style.display = isSun ? 'block' : 'none';
    cloudEl.style.display = isSun ? 'none' : 'block';

    const activeEl = isSun ? sunEl : cloudEl;

    let startTime = null;

    function step(ts) {
        if (!startTime) {
            startTime = ts;
        }

        const progress = Math.min((ts - startTime) / TRAVEL, 1);
        const ease = easeInOutSine(progress);
        const angle = Math.PI * ease;
        const cx = MID_CX + (RX * Math.cos(angle));
        const cy = BASE_CY - (RY * Math.sin(angle));

        activeEl.style.left = (cx - (OBJ_W / 2)) + 'px';
        activeEl.style.top = (cy - (OBJ_H / 2)) + 'px';

        if (progress < 1) {
            requestAnimationFrame(step);
            return;
        }

        objIndex += 1;
        setTimeout(flyObject, 600);
    }

    requestAnimationFrame(step);
}

function saveDiagnosisId(res) {
    sessionStorage.setItem('diagnosisId', res.diagnosisId);
}

function moveWithModal(title, body, moveUrl) {
    stopDots();

    CommonModal.open({
        title: title,
        body: body,
        confirmText: '확인',
        hideCancel: true,
        onConfirm: function () {
            location.href = moveUrl;
        }
    });
}

function handleLoadingResponse(res) {
    if (res.result === 'LOADING_OK') {
        saveDiagnosisId(res);
        location.href = CTX + 'diag/result';
        return;
    }

    if (res.result === 'LOGIN_REQUIRED') {
        moveWithModal('로그인이 필요합니다', res.msg, CTX + 'login/form');
        return;
    }

    if (res.result === 'UPLOAD_NOT_FOUND') {
        moveWithModal('이미지 정보를 확인해주세요', res.msg, CTX + 'diag/ImgTrans');
        return;
    }

    if (res.result === 'DIAG_SAVE_FAIL' || res.result === 'DIAG_ID_NOT_FOUND') {
        moveWithModal('진단 결과 저장 실패', res.msg, CTX + 'diag/ImgTrans');
        return;
    }

    moveWithModal('진단을 진행할 수 없습니다', res.msg || '병충해 진단 처리 중 오류가 발생했습니다.', CTX + 'diag/ImgTrans');
}

function runLoadingProc() {
    if (isProcessing) {
        return;
    }

    isProcessing = true;

    ajaxPost(CTX + 'diag/loadingProc', {}, function (res) {
        handleLoadingResponse(res);
    });
}

document.addEventListener('DOMContentLoaded', function () {
    startDots();
    requestAnimationFrame(animatePlant);
    flyObject();
    runLoadingProc();
});