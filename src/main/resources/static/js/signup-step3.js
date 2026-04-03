const sidoSelect = document.getElementById('sidoSelect');
const sigunguSelect = document.getElementById('sigunguSelect');
const cropGrid = document.getElementById('cropGrid');
const favoriteCropId = document.getElementById('favoriteCropId');
const btnComplete = document.getElementById('btnComplete');
const regionMsg = document.getElementById('regionMsg');

function ajaxGet(url, callback) {
    const xhr = new XMLHttpRequest();

    xhr.open('GET', url, true);

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

    xhr.send();
}

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

function resetSigunguSelect() {
    sigunguSelect.innerHTML = '<option value="">시군구를 선택해주세요</option>';
    sigunguSelect.disabled = true;
}

sidoSelect.addEventListener('change', function () {
    const sidoName = sidoSelect.value;

    resetSigunguSelect();
    clearMsg(regionMsg);

    if (sidoName === '') {
        return;
    }

    ajaxGet(CTX + 'signup/getSigungu?sidoName=' + encodeURIComponent(sidoName), function (list) {
        list.forEach(function (item) {
            const option = document.createElement('option');
            option.value = item.sigunguName;
            option.textContent = item.sigunguName;
            sigunguSelect.appendChild(option);
        });

        sigunguSelect.disabled = false;
    });
});

cropGrid.addEventListener('click', function (e) {
    const target = e.target.closest('.crop-item');

    if (!target) {
        return;
    }

    const selected = cropGrid.querySelector('.crop-item.selected');

    if (selected) {
        selected.classList.remove('selected');
    }

    if (selected === target) {
        favoriteCropId.value = '';
        return;
    }

    target.classList.add('selected');
    favoriteCropId.value = target.dataset.id;
});

btnComplete.addEventListener('click', function () {
    clearMsg(regionMsg);

    const sidoName = sidoSelect.value;
    const sigunguName = sigunguSelect.value;
    const cropId = favoriteCropId.value;

    if (sidoName === '' || sigunguName === '') {
        showMsg(regionMsg, '지역을 선택해주세요.', 'error');
        return;
    }

    const data = {
        sidoName: sidoName,
        sigunguName: sigunguName,
        favoriteCropId: cropId
    };

    ajaxPost(CTX + 'signup/complete', data, function (res) {
        if (res.result === 'SIGNUP_OK') {
            alert(res.msg);
            location.href = CTX + 'login/form';
            return;
        }

        if (res.result === 'REGION_EMPTY') {
            showMsg(regionMsg, res.msg, 'error');
            return;
        }

        if (res.result === 'SESSION_INVALID') {
            alert(res.msg + '\n처음부터 다시 진행해주세요.');
            location.href = CTX + 'signup/step1';
            return;
        }

        showMsg(regionMsg, res.msg, 'error');
    });
});