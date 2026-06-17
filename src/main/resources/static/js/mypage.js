const cropGrid = document.getElementById('cropGrid');
const favoriteCropIdInput = document.getElementById('favoriteCropId');
const btnSaveCrop = document.getElementById('btnSaveCrop');
const cropMsg = document.getElementById('cropMsg');

const btnOpenRegionModal = document.getElementById('btnOpenRegionModal');
const btnWithdraw = document.getElementById('btnWithdraw');
const btnLogout = document.getElementById('btnLogout');

let selectedSidoName = '';
let selectedSigunguName = '';

function ajaxGet(url, callback) {
    const xhr = new XMLHttpRequest();

    xhr.open('GET', url, true);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const res = JSON.parse(xhr.responseText);
                callback(res);
            } else {
                openAlert('오류', '요청 처리 중 오류가 발생했습니다.');
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
                openAlert('오류', '요청 처리 중 오류가 발생했습니다.');
            }
        }
    };

    const params = Object.entries(data)
        .map(([key, value]) => encodeURIComponent(key) + '=' + encodeURIComponent(value))
        .join('&');

    xhr.send(params);
}

function showMsg(el, msg, type) {
    if (!el) {
        return;
    }

    el.textContent = msg;
    el.className = 'msg-' + type;
}

function clearMsg(el) {
    if (!el) {
        return;
    }

    el.textContent = '';
    el.className = '';
}

function openAlert(title, body, onConfirm) {
    CommonModal.open({
        title: title,
        body: body,
        hideCancel: true,
        confirmText: '확인',
        onConfirm: onConfirm
    });
}

/* ===== 작물 선택 ===== */
if (cropGrid) {
    cropGrid.addEventListener('click', function (e) {
        const target = e.target.closest('.crop-item');

        if (!target) {
            return;
        }

        const selected = cropGrid.querySelector('.crop-item.selected');

        if (selected) {
            selected.classList.remove('selected');
        }

        target.classList.add('selected');
        favoriteCropIdInput.value = target.dataset.id;

        clearMsg(cropMsg);
    });
}

if (btnSaveCrop) {
    btnSaveCrop.addEventListener('click', function () {
        const favoriteCropId = favoriteCropIdInput.value;

        if (favoriteCropId === '') {
            showMsg(cropMsg, '선호 작물을 선택해주세요.', 'error');
            return;
        }

        ajaxPost(CTX + 'mypage/updateFavoriteCrop', {
            favoriteCropId: favoriteCropId
        }, function (res) {
            if (res.result === 'success') {
                openAlert('수정 완료', res.msg || '선호 작물이 수정되었습니다.', function () {
                    location.reload();
                });
                return;
            }

            if (res.result === 'login') {
                openAlert('로그인 필요', res.msg || '로그인이 필요합니다.', function () {
                    location.href = CTX + 'login/form';
                });
                return;
            }

            showMsg(cropMsg, res.msg || '선호 작물 수정에 실패했습니다.', 'error');
        });
    });
}

/* ===== 지역 수정 ===== */
function getRegionModalElements() {
    const modalBody = document.getElementById('commonModalBody');

    return {
        modalSidoSelect: modalBody ? modalBody.querySelector('#modalSidoSelect') : null,
        modalSigunguSelect: modalBody ? modalBody.querySelector('#modalSigunguSelect') : null,
        regionMsg: modalBody ? modalBody.querySelector('#regionMsg') : null
    };
}

function bindRegionModalEvents() {
    const els = getRegionModalElements();

    const modalSidoSelect = els.modalSidoSelect;
    const modalSigunguSelect = els.modalSigunguSelect;
    const regionMsg = els.regionMsg;

    if (!modalSidoSelect || !modalSigunguSelect) {
        return;
    }

    modalSidoSelect.addEventListener('change', function () {
        const sidoName = modalSidoSelect.value;

        selectedSidoName = sidoName;
        selectedSigunguName = '';

        modalSigunguSelect.innerHTML = '<option value="">시군구를 선택해주세요</option>';
        modalSigunguSelect.disabled = true;
        clearMsg(regionMsg);

        if (sidoName === '') {
            return;
        }

        ajaxGet(CTX + 'mypage/getSigungu?sidoName=' + encodeURIComponent(sidoName), function (list) {
            list.forEach(function (item) {
                const option = document.createElement('option');
                option.value = item.sigunguName;
                option.textContent = item.sigunguName;
                modalSigunguSelect.appendChild(option);
            });

            modalSigunguSelect.disabled = false;
        });
    });

    modalSigunguSelect.addEventListener('change', function () {
        selectedSigunguName = modalSigunguSelect.value;
        clearMsg(regionMsg);
    });
}

if (btnOpenRegionModal) {
    btnOpenRegionModal.addEventListener('click', function () {
        const template = document.getElementById('regionModalTemplate');

        selectedSidoName = '';
        selectedSigunguName = '';

        CommonModal.open({
            title: '지역 수정',
            body: template.innerHTML,
            cancelText: '취소',
            confirmText: '수정',
            cancelType: 'neutral',
            confirmType: 'primary',
            onConfirm: function () {
                if (selectedSidoName === '' || selectedSigunguName === '') {
                    openAlert('지역 수정', '지역을 선택해주세요.');
                    return;
                }

                ajaxPost(CTX + 'mypage/updateRegion', {
                    sidoName: selectedSidoName,
                    sigunguName: selectedSigunguName
                }, function (res) {
                    if (res.result === 'success') {
                        openAlert('수정 완료', res.msg || '지역이 수정되었습니다.', function () {
                            location.reload();
                        });
                        return;
                    }

                    if (res.result === 'login') {
                        openAlert('로그인 필요', res.msg || '로그인이 필요합니다.', function () {
                            location.href = CTX + 'login/form';
                        });
                        return;
                    }

                    openAlert('수정 실패', res.msg || '지역 수정에 실패했습니다.');
                });
            }
        });

        setTimeout(function () {
            bindRegionModalEvents();
        }, 0);
    });
}

/* ===== 회원탈퇴 ===== */
if (btnWithdraw) {
    btnWithdraw.addEventListener('click', function () {
        CommonModal.open({
            title: '회원탈퇴',
            body: '정말 회원탈퇴를 진행하시겠습니까?<br>탈퇴 후 15일 이내에는 로그인 시 복구할 수 있습니다.',
            cancelText: '취소',
            confirmText: '탈퇴',
            cancelType: 'neutral',
            confirmType: 'danger',
            onConfirm: function () {
                ajaxPost(CTX + 'mypage/withdraw', {}, function (res) {
                    if (res.result === 'success') {
                        openAlert('탈퇴 완료', res.msg || '회원탈퇴가 처리되었습니다.', function () {
                            location.href = CTX + 'main/view';
                        });
                        return;
                    }

                    if (res.result === 'login') {
                        openAlert('로그인 필요', res.msg || '로그인이 필요합니다.', function () {
                            location.href = CTX + 'login/form';
                        });
                        return;
                    }

                    openAlert('탈퇴 실패', res.msg || '회원탈퇴 처리에 실패했습니다.');
                });
            }
        });
    });
}

/* ===== 리포트 클릭: 상세 기능은 나중에 연결 ===== */
document.querySelectorAll('.analysis-report-item').forEach(function (item) {
    item.addEventListener('click', function () {
        const analysisId = item.dataset.id;

        if (!analysisId) {
            openAlert('오류', '분석 번호를 찾을 수 없습니다.');
            return;
        }

        location.href = CTX + 'mypage/analysis/result/' + analysisId;
    });
});

document.querySelectorAll('.diagnosis-report-item').forEach(function (item) {
    item.addEventListener('click', function () {
        const diagnosisId = item.dataset.id;

        if (!diagnosisId) {
            openAlert('오류', '진단 번호를 찾을 수 없습니다.');
            return;
        }

        location.href = CTX + 'mypage/diagnosis/result/' + diagnosisId;
    });
});

/* ===== 로그아웃 ===== */
if (btnLogout) {
    btnLogout.addEventListener('click', function () {
        CommonModal.open({
            title: '로그아웃',
            body: '로그아웃하시겠습니까?',
            cancelText: '취소',
            confirmText: '로그아웃',
            cancelType: 'neutral',
            confirmType: 'primary',
            onConfirm: function () {
                location.href = CTX + 'login/logout';
            }
        });
    });
}