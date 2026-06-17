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

function restoreUser() {
    ajaxPost(CTX + 'login/restore', {}, function (res) {
        if (res.result === 'success') {
            CommonModal.open({
                title: '복구 완료',
                body: res.msg || '계정이 복구되었습니다. 다시 로그인해주세요.',
                hideCancel: true,
                confirmText: '확인',
                onConfirm: function () {
                    location.reload();
                }
            });
            return;
        }

        CommonModal.open({
            title: '복구 실패',
            body: res.msg || '계정 복구에 실패했습니다.',
            hideCancel: true,
            confirmText: '확인'
        });
    });
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

        if (res.result === 'RESTORE_CONFIRM') {
            CommonModal.open({
                title: '계정 복구',
                body: res.msg || '탈퇴한 계정입니다. 복구하시겠습니까?',
                cancelText: '아니오',
                confirmText: '네',
                confirmType: 'primary',
                cancelType: 'neutral',
                onConfirm: function () {
                    restoreUser();
                }
            });
            return;
        }

        if (res.result === 'ACCOUNT_EXPIRED') {
            CommonModal.open({
                title: '복구 불가',
                body: res.msg || '탈퇴 후 15일이 지나 복구할 수 없는 계정입니다.',
                hideCancel: true,
                confirmText: '확인'
            });
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