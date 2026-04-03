const loginIdInput = document.getElementById('loginId');
const passwordInput = document.getElementById('password');
const passwordConfirmInput = document.getElementById('passwordConfirm');
const btnCheckId = document.getElementById('btnCheckId');
const btnNext = document.getElementById('btnNext');

const loginIdMsg = document.getElementById('loginIdMsg');
const passwordMsg = document.getElementById('passwordMsg');
const passwordConfirmMsg = document.getElementById('passwordConfirmMsg');

let isLoginIdChecked = false;

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
    clearMsg(loginIdMsg);
    clearMsg(passwordMsg);
    clearMsg(passwordConfirmMsg);
}

function resetLoginIdCheck() {
    isLoginIdChecked = false;
}

loginIdInput.addEventListener('input', function () {
    resetLoginIdCheck();
    clearMsg(loginIdMsg);
});

passwordInput.addEventListener('input', function () {
    clearMsg(passwordMsg);
});

passwordConfirmInput.addEventListener('input', function () {
    clearMsg(passwordConfirmMsg);
});

btnCheckId.addEventListener('click', function () {
    clearMsg(loginIdMsg);

    const loginId = loginIdInput.value.trim();

    ajaxPost(CTX + 'signup/checkLoginId', { loginId }, function (res) {
        if (res.result === 'LOGINID_OK') {
            showMsg(loginIdMsg, res.msg, 'success');
            isLoginIdChecked = true;
            return;
        }

        showMsg(loginIdMsg, res.msg, 'error');
        isLoginIdChecked = false;
    });
});

btnNext.addEventListener('click', function () {
    clearAllMsg();

    if (!isLoginIdChecked) {
        showMsg(loginIdMsg, '아이디 중복확인을 해주세요.', 'error');
        return;
    }

    const loginId = loginIdInput.value.trim();
    const password = passwordInput.value.trim();
    const passwordConfirm = passwordConfirmInput.value.trim();

    ajaxPost(CTX + 'signup/saveStep2', { loginId, password, passwordConfirm }, function (res) {
        if (res.result === 'STEP2_OK') {
            location.href = CTX + 'signup/step3';
            return;
        }

        if (
            res.result === 'LOGINID_EMPTY' ||
            res.result === 'LOGINID_INVALID' ||
            res.result === 'LOGINID_EXISTS'
        ) {
            showMsg(loginIdMsg, res.msg, 'error');
            return;
        }

        if (
            res.result === 'PASSWORD_EMPTY' ||
            res.result === 'PASSWORD_INVALID'
        ) {
            showMsg(passwordMsg, res.msg, 'error');
            return;
        }

        if (
            res.result === 'PASSWORD_CONFIRM_EMPTY' ||
            res.result === 'PASSWORD_MISMATCH'
        ) {
            showMsg(passwordConfirmMsg, res.msg, 'error');
            return;
        }

        showMsg(loginIdMsg, res.msg, 'error');
    });
});