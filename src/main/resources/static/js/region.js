const sidoSelect = document.getElementById('sidoSelect');
const sigunguSelect = document.getElementById('sigunguSelect');
const btnAnalyze = document.getElementById('btnAnalyze');
const regionMsg = document.getElementById('regionMsg');
const selectedPreview = document.getElementById('selectedPreview');
const previewText = document.getElementById('previewText');
const btnLogout = document.getElementById('btnLogout');

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

function updateSelectedPreview() {
    const sidoName = sidoSelect.value;
    const sigunguName = sigunguSelect.value;

    if (sidoName !== '' && sigunguName !== '') {
        previewText.textContent = sidoName + ' ' + sigunguName;
        selectedPreview.style.display = 'block';
        return;
    }

    selectedPreview.style.display = 'none';
}

function resetSigunguSelect() {
    sigunguSelect.innerHTML = '<option value="">시군구를 선택해주세요</option>';
    sigunguSelect.disabled = true;
    selectedPreview.style.display = 'none';
}

sidoSelect.addEventListener('change', function () {
    const sidoName = sidoSelect.value;

    resetSigunguSelect();
    clearMsg(regionMsg);

    if (sidoName === '') {
        return;
    }

    ajaxGet(CTX + 'crop/getSigungu?sidoName=' + encodeURIComponent(sidoName), function (list) {
        list.forEach(function (item) {
            const option = document.createElement('option');
            option.value = item.sigunguName;
            option.textContent = item.sigunguName;
            sigunguSelect.appendChild(option);
        });

        sigunguSelect.disabled = false;
    });
});

sigunguSelect.addEventListener('change', function () {
    clearMsg(regionMsg);
    updateSelectedPreview();
});

btnAnalyze.addEventListener('click', function () {
    clearMsg(regionMsg);

    const sidoName = sidoSelect.value;
    const sigunguName = sigunguSelect.value;

    if (sidoName === '' || sigunguName === '') {
        showMsg(regionMsg, '지역을 선택해주세요.', 'error');
        return;
    }

    ajaxPost(CTX + 'crop/selectRegion', { sidoName, sigunguName }, function (res) {
        if (res.result === 'SELECT_OK') {
            location.href = CTX + 'crop/loading';
            return;
        }

        showMsg(regionMsg, res.msg || '지역 선택 처리 중 오류가 발생했습니다.', 'error');
    });
});

if (btnLogout) {
    btnLogout.addEventListener('click', function () {
        CommonModal.open({
            title: '로그아웃<br>하시겠습니까',
            confirmText: '네',
            cancelText: '아니요',
            onConfirm: function () {
                location.href = CTX + 'login/logout';
            }
        });
    });
}