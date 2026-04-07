const passwordInput = document.getElementById('password');
const passwordConfirmInput = document.getElementById('passwordConfirm');
const btnNext = document.getElementById('btnNext');

const passwordMsg = document.getElementById('passwordMsg');
const passwordConfirmMsg = document.getElementById('passwordConfirmMsg');

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
    clearMsg(passwordMsg);
    clearMsg(passwordConfirmMsg);
}

passwordInput.addEventListener('input', function () {
    clearMsg(passwordMsg);
});

passwordConfirmInput.addEventListener('input', function () {
    clearMsg(passwordConfirmMsg);
});

btnNext.addEventListener('click', function () {
    clearAllMsg();

    const password = passwordInput.value.trim();
    const passwordConfirm = passwordConfirmInput.value.trim();

    ajaxPost(CTX + 'find/password/reset', { password, passwordConfirm }, function (res) {

        if (
            res.result === 'PASSWORD_EMPTY' ||
            res.result === 'PASSWORD_INVALID' ||
            res.result === 'PASSWORD_SAME_AS_OLD'
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

        if (res.result === 'RESET_PW_OK') {
            CommonModal.open({
                title: '비밀번호 재설정 완료',
                body: '비밀번호가 정상적으로 변경되었습니다.',
                confirmText: '로그인으로',
                hideCancel: true,
                onConfirm: function () {
                    location.href = CTX + 'login/form';
                }
            });
            return;
        }

        CommonModal.open({
            title: '오류',
            body: res.msg || '비밀번호 재설정 중 오류가 발생했습니다.',
            confirmText: '확인',
            hideCancel: true
        });
    });
});