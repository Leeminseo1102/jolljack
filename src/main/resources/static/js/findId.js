const nameInput = document.getElementById('name');
const emailInput = document.getElementById('email');
const inputCode = document.getElementById('inputCode');
const codeArea = document.getElementById('codeArea');
const codeTimer = document.getElementById('codeTimer');
const btnSendCode = document.getElementById('btnSendCode');
const btnVerify = document.getElementById('btnVerify');
const btnFindId = document.getElementById('btnFindId');
const btnConfirm = document.getElementById('btnConfirm');

const nameMsg = document.getElementById('nameMsg');
const emailMsg = document.getElementById('emailMsg');
const codeMsg = document.getElementById('codeMsg');

let timerInterval = null;
let isVerified = false;
let isFindSuccess = false;

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
                alert('요청 처리 중 오류가 발생했습니다.');
            }
        }
    };

    const params = Object.entries(data)
        .map(([key, value]) => encodeURIComponent(key) + '=' + encodeURIComponent(value))
        .join('&');

    xhr.send(params);
}

function showMsg(el, msg, type) {
    el.textContent = msg;
    el.className = 'msg-' + type;
}

function clearMsg(el) {
    el.textContent = '';
    el.className = '';
}

function clearAllMsg() {
    clearMsg(nameMsg);
    clearMsg(emailMsg);
    clearMsg(codeMsg);
}

function resetVerifyState() {
    isVerified = false;
    isFindSuccess = false;
    btnFindId.disabled = true;
    inputCode.disabled = false;
    btnVerify.disabled = false;
}

function startTimer(expireTime) {
    clearInterval(timerInterval);

    codeTimer.className = 'msg-info';

    timerInterval = setInterval(function () {

        const remaining = Math.floor((expireTime - Date.now()) / 1000);

        if (remaining <= 0) {
            clearInterval(timerInterval);
            codeTimer.textContent = '인증 시간이 만료되었습니다.';
            codeTimer.className = 'msg-error';
            resetVerifyState();
            return;
        }

        const min = String(Math.floor(remaining / 60)).padStart(2, '0');
        const sec = String(remaining % 60).padStart(2, '0');

        codeTimer.textContent = min + ':' + sec;

    }, 1000);
}

nameInput.addEventListener('input', function () {
    clearMsg(nameMsg);
});

emailInput.addEventListener('input', function () {
    clearMsg(emailMsg);
});

inputCode.addEventListener('input', function () {
    clearMsg(codeMsg);
});

btnSendCode.addEventListener('click', function () {
    clearAllMsg();

    const name = nameInput.value.trim();
    const email = emailInput.value.trim();

    ajaxPost(CTX + 'find/id/sendEmailCode', { name, email }, function (res) {

        if (res.result === 'NAME_EMPTY') {
            showMsg(nameMsg, res.msg, 'error');
            nameInput.focus();
            return;
        }

        if (res.result === 'EMAIL_EMPTY' || res.result === 'EMAIL_INVALID') {
            showMsg(emailMsg, res.msg, 'error');
            emailInput.focus();
            return;
        }

        if (res.result === 'FIND_ID_FAIL') {
            isFindSuccess = false;

            CommonModal.open({
                title: '회원 정보를 찾을 수 없습니다',
                body: '입력하신 정보와 일치하는 회원 정보가 없습니다.',
                confirmText: '확인',
                hideCancel: true
            });
            return;
        }

        if (res.result === 'SEND_OK') {
            showMsg(emailMsg, res.msg, 'success');

            codeArea.style.display = 'block';
            inputCode.value = '';
            clearMsg(codeMsg);

            resetVerifyState();
            startTimer(res.expireTime);

            btnSendCode.textContent = '재전송';
        }
    });
});

btnVerify.addEventListener('click', function () {
    clearMsg(codeMsg);

    const inputCodeValue = inputCode.value.trim();

    ajaxPost(CTX + 'find/id/verifyEmailCode', { inputCode: inputCodeValue }, function (res) {
        if (res.result === 'CODE_OK') {
            showMsg(codeMsg, res.msg, 'success');

            clearInterval(timerInterval);
            codeTimer.textContent = '';
            codeTimer.className = '';

            isVerified = true;
            inputCode.disabled = true;
            btnVerify.disabled = true;
            btnFindId.disabled = false;
            return;
        }

        showMsg(codeMsg, res.msg, 'error');
    });
});

btnFindId.addEventListener('click', function () {
    if (!isVerified) {
        return;
    }

    ajaxPost(CTX + 'find/id/result', {}, function (res) {

        if (res.result === 'FIND_ID_OK') {
            isFindSuccess = true;

            const name = nameInput.value.trim();

            CommonModal.open({
                title: name + '님의 아이디는<br>' + res.loginId + '입니다',
                confirmText: '로그인으로',
                hideCancel: true,
                onConfirm: function () {
                    location.href = CTX + 'login/form';
                }
            });
            return;
        }

        isFindSuccess = false;

        if (res.result === 'FIND_ID_FAIL') {
            CommonModal.open({
                title: '회원 정보를 찾을 수 없습니다',
                body: '입력하신 정보와 일치하는 회원 정보가 없습니다.',
                confirmText: '확인',
                hideCancel: true
            });
            return;
        }

        if (res.result === 'SESSION_INVALID' || res.result === 'VERIFY_REQUIRED') {
            CommonModal.open({
                title: '다시 진행해주세요',
                body: res.msg,
                confirmText: '확인',
                hideCancel: true
            });
            return;
        }

        CommonModal.open({
            title: '오류',
            body: res.msg || '아이디 찾기 중 오류가 발생했습니다.',
            confirmText: '확인',
            hideCancel: true
        });
    });
});

btnConfirm.addEventListener('click', function () {
    if (isFindSuccess) {
        location.href = CTX + 'login/form';
        return;
    }

    location.href = CTX + 'find/id';
});