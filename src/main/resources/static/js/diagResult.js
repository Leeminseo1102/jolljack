const candidateButtonWrap = document.getElementById('candidateButtonWrap');

const selectedDiseaseNameEl = document.getElementById('selectedDiseaseName');
const selectedPercentEl = document.getElementById('selectedPercent');

const diagnosisReasonEl = document.getElementById('diagnosisReason');
const solutionTextEl = document.getElementById('solutionText');
const preventionTextEl = document.getElementById('preventionText');

const btnDone = document.getElementById('btnDone');

let diagnosisData = null;
let candidateList = [];

/* =====================================================
   공통 AJAX
   ===================================================== */

function ajaxPost(url, data, callback) {
    const xhr = new XMLHttpRequest();

    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                callback(JSON.parse(xhr.responseText));
            } else {
                callback({
                    result: 'FAIL',
                    msg: '요청 처리 중 오류가 발생했습니다.'
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

function openErrorModal(title, body, moveUrl) {
    CommonModal.open({
        title: title,
        body: body,
        confirmText: '확인',
        hideCancel: true,
        onConfirm: function () {
            if (moveUrl) {
                location.href = moveUrl;
            }
        }
    });
}

function isReportMode() {
    return typeof REPORT_MODE !== 'undefined' && REPORT_MODE === true;
}

function escapeHtml(str) {
    return String(str || '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

/* =====================================================
   diagnosisId 가져오기
   일반 진입 : sessionStorage diagnosisId
   마이페이지 진입 : model diagnosisId
   ===================================================== */

function getSavedDiagnosisId() {
    if (isReportMode()
        && typeof DIAGNOSIS_ID !== 'undefined'
        && DIAGNOSIS_ID !== null) {
        return DIAGNOSIS_ID;
    }

    return sessionStorage.getItem('diagnosisId');
}

/* =====================================================
   결과 조회
   ===================================================== */

function loadDiagnosisData() {
    const diagnosisId = getSavedDiagnosisId();

    if (!diagnosisId) {
        const moveUrl = isReportMode()
            ? CTX + 'mypage/view'
            : CTX + 'diag/ImgTrans';

        openErrorModal(
            '진단 결과가 없습니다',
            '진단 결과 정보를 찾을 수 없습니다.',
            moveUrl
        );
        return;
    }

    ajaxPost(CTX + 'diag/resultData', { diagnosisId: diagnosisId }, function (res) {
        if (res.result !== 'SUCCESS') {
            const moveUrl = isReportMode()
                ? CTX + 'mypage/view'
                : CTX + 'diag/ImgTrans';

            openErrorModal(
                '진단 결과 조회 실패',
                res.msg || '진단 결과 조회 중 오류가 발생했습니다.',
                moveUrl
            );
            return;
        }

        diagnosisData = res.data;
        renderDiagnosisData();
    });
}

/* =====================================================
   렌더링
   ===================================================== */

function renderDiagnosisData() {
    candidateList = parseCandidateList(
        diagnosisData.predictedDiseaseName,
        diagnosisData.symptomSummary
    );

    if (!candidateList.length) {
        candidateList.push({
            rank: 1,
            name: diagnosisData.predictedDiseaseName || '진단명',
            percent: null
        });
    }

    renderCandidateButtons();
    selectCandidate(0);
}

function renderCandidateButtons() {
    candidateButtonWrap.innerHTML = candidateList.map(function (item, index) {
        const activeClass = index === 0 ? ' active' : '';

        return ''
            + '<button type="button" class="diag-candidate-btn' + activeClass + '" data-index="' + index + '">'
            + '    <span class="diag-candidate-name">' + escapeHtml(item.name) + '</span>'
            + '    <span class="diag-candidate-percent">' + formatPercent(item.percent) + '</span>'
            + '</button>';
    }).join('');

    document.querySelectorAll('.diag-candidate-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            selectCandidate(Number(btn.dataset.index));
        });
    });
}

function selectCandidate(index) {
    const selected = candidateList[index];

    if (!selected) {
        return;
    }

    document.querySelectorAll('.diag-candidate-btn').forEach(function (btn) {
        btn.classList.remove('active');
    });

    const activeBtn = document.querySelector('.diag-candidate-btn[data-index="' + index + '"]');

    if (activeBtn) {
        activeBtn.classList.add('active');
    }

    selectedDiseaseNameEl.textContent = selected.name;
    selectedPercentEl.textContent = formatPercent(selected.percent);

    const rank = selected.rank;

    const reasonText = extractRankText(diagnosisData.diseaseDescription, rank);
    const solutionText = extractRankText(diagnosisData.solutionText, rank);
    const preventionText = extractRankText(diagnosisData.preventionText, rank);

    diagnosisReasonEl.textContent = reasonText || diagnosisData.diseaseDescription || '진단 이유가 없습니다.';
    solutionTextEl.textContent = solutionText || diagnosisData.solutionText || '해결 방안이 없습니다.';
    preventionTextEl.textContent = preventionText || diagnosisData.preventionText || '예방 방법이 없습니다.';
}

/* =====================================================
   후보명 / 퍼센트 파싱
   predictedDiseaseName 예:
   1순위 고추 탄저병, 2순위 고추 역병, 3순위 흰가루병

   symptomSummary 예:
   후보별 유사도 점수는 1순위 고추 탄저병 87점, ...
   ===================================================== */

function parseCandidateList(predictedDiseaseName, symptomSummary) {
    const nameText = predictedDiseaseName || '';
    const summaryText = symptomSummary || '';

    const result = [];

    for (let rank = 1; rank <= 3; rank++) {
        const name = extractRankName(nameText, rank);

        if (!name) {
            continue;
        }

        result.push({
            rank: rank,
            name: name,
            percent: extractRankPercent(summaryText, rank)
        });
    }

    return result;
}

function extractRankName(text, rank) {
    if (!text) {
        return '';
    }

    const source = String(text);

    const currentRank = rank + '순위';
    const nextRank = (rank + 1) + '순위';

    const startIndex = source.indexOf(currentRank);

    if (startIndex === -1) {
        return '';
    }

    let endIndex = source.indexOf(nextRank, startIndex + currentRank.length);

    if (endIndex === -1) {
        endIndex = source.length;
    }

    return source.substring(startIndex + currentRank.length, endIndex)
        .replaceAll(',', '')
        .replaceAll('.', '')
        .replaceAll(':', '')
        .trim();
}

function extractRankPercent(text, rank) {
    if (!text) {
        return null;
    }

    const source = String(text);

    const regex = new RegExp(rank + '순위\\s*[^,\\.\\n]*?\\s*(\\d{1,3})점');
    const match = source.match(regex);

    if (!match) {
        return null;
    }

    const percent = Number(match[1]);

    if (Number.isNaN(percent)) {
        return null;
    }

    return percent;
}

/* =====================================================
   1순위 / 2순위 / 3순위 내용 자르기
   diseaseDescription / solutionText / preventionText 공통
   ===================================================== */

function extractRankText(text, rank) {
    if (!text) {
        return '';
    }

    const source = String(text).trim();

    const currentRank = rank + '순위';
    const nextRank = (rank + 1) + '순위';

    const startIndex = source.indexOf(currentRank);

    if (startIndex === -1) {
        return '';
    }

    let endIndex = source.indexOf(nextRank, startIndex + currentRank.length);

    if (endIndex === -1) {
        endIndex = source.length;
    }

    return source.substring(startIndex, endIndex)
        .replaceAll(/\s+/g, ' ')
        .trim();
}

function formatPercent(value) {
    if (value === null || value === undefined || Number.isNaN(value)) {
        return '--%';
    }

    return value + '%';
}

/* =====================================================
   버튼
   ===================================================== */

function bindDoneButton() {
    if (!btnDone) {
        return;
    }

    btnDone.addEventListener('click', function () {
        if (isReportMode()) {
            location.href = CTX + 'mypage/view';
            return;
        }

        location.href = CTX + 'main/view';
    });
}

/* =====================================================
   시작
   ===================================================== */

document.addEventListener('DOMContentLoaded', function () {
    bindDoneButton();
    loadDiagnosisData();
});