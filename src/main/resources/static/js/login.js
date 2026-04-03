const loginIdInput = document.getElementById('loginId');
const passwordInput = document.getElementById('password');
const btnLogin = document.getElementById('btnLogin');

const loginIdMsg = document.getElementById('loginIdMsg');
const passwordMsg = document.getElementById('passwordMsg');
const loginMsg = document.getElementById('loginMsg');

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
    clearMsg(loginMsg);
}

loginIdInput.addEventListener('input', function () {
    clearMsg(loginIdMsg);
    clearMsg(loginMsg);
});

passwordInput.addEventListener('input', function () {
    clearMsg(passwordMsg);
    clearMsg(loginMsg);
});

loginIdInput.addEventListener('keydown', function (e) {
    if (e.key === 'Enter') {
        btnLogin.click();
    }
});

passwordInput.addEventListener('keydown', function (e) {
    if (e.key === 'Enter') {
        btnLogin.click();
    }
});

btnLogin.addEventListener('click', function () {
    clearAllMsg();

    const loginId = loginIdInput.value.trim();
    const password = passwordInput.value.trim();

    ajaxPost(CTX + 'login/proc', { loginId, password }, function (res) {
        if (res.result === 'LOGIN_OK') {
            location.href = CTX + 'main/view';
            return;
        }

        if (res.result === 'LOGINID_EMPTY') {
            showMsg(loginIdMsg, res.msg, 'error');
            loginIdInput.focus();
            return;
        }

        if (res.result === 'PASSWORD_EMPTY') {
            showMsg(passwordMsg, res.msg, 'error');
            passwordInput.focus();
            return;
        }

        showMsg(loginMsg, res.msg, 'error');
    });
});