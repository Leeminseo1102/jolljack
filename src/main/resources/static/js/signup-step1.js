const nameInput = document.getElementById('name');
const emailInput = document.getElementById('email');
const inputCode = document.getElementById('inputCode');
const codeArea = document.getElementById('codeArea');
const codeTimer = document.getElementById('codeTimer');
const btnSendCode = document.getElementById('btnSendCode');
const btnVerify = document.getElementById('btnVerify');
const btnNext = document.getElementById('btnNext');

const nameMsg = document.getElementById('nameMsg');
const emailMsg = document.getElementById('emailMsg');
const codeMsg = document.getElementById('codeMsg');

let timerInterval = null;
let isVerified = false;

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
    btnNext.disabled = true;
    inputCode.disabled = false;
    btnVerify.disabled = false;
}

function startTimer() {
    clearInterval(timerInterval);

    let remaining = 3 * 60;

    codeTimer.textContent = '03:00';
    codeTimer.className = 'msg-info';

    timerInterval = setInterval(function () {
        remaining -= 1;

        const min = String(Math.floor(remaining / 60)).padStart(2, '0');
        const sec = String(remaining % 60).padStart(2, '0');

        codeTimer.textContent = min + ':' + sec;

        if (remaining <= 0) {
            clearInterval(timerInterval);
            codeTimer.textContent = '인증 시간이 만료되었습니다.';
            codeTimer.className = 'msg-error';
            resetVerifyState();
        }
    }, 1000);
}

btnSendCode.addEventListener('click', function () {
    clearAllMsg();

    const name = nameInput.value.trim();
    const email = emailInput.value.trim();

    ajaxPost(CTX + 'signup/sendEmailCode', { name, email }, function (res) {
        if (res.result === 'NAME_EMPTY') {
            showMsg(nameMsg, res.msg, 'error');
            return;
        }

        if (
            res.result === 'EMAIL_EMPTY' ||
            res.result === 'EMAIL_INVALID' ||
            res.result === 'EMAIL_EXISTS'
        ) {
            showMsg(emailMsg, res.msg, 'error');
            return;
        }

        if (res.result === 'SEND_OK') {
            showMsg(emailMsg, res.msg, 'success');

            codeArea.style.display = 'block';
            inputCode.value = '';
            clearMsg(codeMsg);

            resetVerifyState();
            startTimer();

            btnSendCode.textContent = '재전송';
        }
    });
});

btnVerify.addEventListener('click', function () {
    clearMsg(codeMsg);

    const code = inputCode.value.trim();

    ajaxPost(CTX + 'signup/verifyEmailCode', { inputCode: code }, function (res) {
        if (res.result === 'CODE_OK') {
            showMsg(codeMsg, res.msg, 'success');

            clearInterval(timerInterval);
            codeTimer.textContent = '';
            codeTimer.className = '';

            isVerified = true;
            inputCode.disabled = true;
            btnVerify.disabled = true;
            btnNext.disabled = false;

            return;
        }

        showMsg(codeMsg, res.msg, 'error');
    });
});

btnNext.addEventListener('click', function () {
    if (!isVerified) {
        return;
    }

    location.href = CTX + 'signup/step2';
});