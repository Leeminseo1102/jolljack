const btnLogout = document.getElementById('btnLogout');
const btnBackResult = document.getElementById('btnMain');

const riskRegionNameEl = document.getElementById('riskRegionName');
const riskCropNameEl = document.getElementById('riskCropName');
const riskListEl = document.getElementById('riskList');
const riskLegendEl = document.getElementById('riskLegend');
const riskChartCanvas = document.getElementById('riskChart');

let riskChart = null;

/* =====================================================
   공통
   ===================================================== */

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

function bindLogout() {
    if (!btnLogout) {
        return;
    }

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

function bindBackButton() {
    if (!btnBackResult) {
        return;
    }

    btnBackResult.addEventListener('click', function () {
        const isReportMode = typeof REPORT_MODE !== 'undefined' && REPORT_MODE === true;

        CommonModal.open({
            title: isReportMode
                ? '마이페이지로<br>이동하시겠습니까'
                : '메인페이지로<br>이동하시겠습니까',
            confirmText: '예',
            cancelText: '아니오',
            onConfirm: function () {
                if (isReportMode) {
                    location.href = CTX + 'mypage/view';
                    return;
                }

                location.href = CTX + 'main/view';
            }
        });
    });
}

/* =====================================================
   analysisId
   ===================================================== */

function getSavedAnalysisId() {
    if (typeof REPORT_MODE !== 'undefined'
        && REPORT_MODE === true
        && typeof ANALYSIS_ID !== 'undefined'
        && ANALYSIS_ID !== null) {
        return ANALYSIS_ID;
    }

    const raw = sessionStorage.getItem('cropAnalysisResult');

    if (!raw) {
        return null;
    }

    try {
        const parsed = JSON.parse(raw);
        return parsed.analysisId || null;
    } catch (e) {
        return null;
    }
}

/* =====================================================
   결과 조회
   ===================================================== */

function loadRiskData() {
    const analysisId = getSavedAnalysisId();

    if (!analysisId) {
        const moveUrl = (typeof REPORT_MODE !== 'undefined' && REPORT_MODE === true)
            ? CTX + 'mypage/view'
            : CTX + 'crop/region';

        openErrorModal(
            '분석 결과가 없습니다',
            '분석 결과 정보를 찾을 수 없습니다.',
            moveUrl
        );
        return;
    }

    ajaxPost(CTX + 'crop/resultData', { analysisId: analysisId }, function (res) {
        if (res.result !== 'RESULT_OK') {
            const moveUrl = (typeof REPORT_MODE !== 'undefined' && REPORT_MODE === true)
                ? CTX + 'mypage/view'
                : CTX + 'crop/region';

            openErrorModal(
                '리스크 조회 실패',
                res.msg || '리스크 조회 중 오류가 발생했습니다.',
                moveUrl
            );
            return;
        }

        renderRiskData(res);
    });
}

/* =====================================================
   렌더링
   ===================================================== */

function renderRiskData(data) {
    riskRegionNameEl.textContent = data.fullRegionName || '-';
    riskCropNameEl.textContent = data.cropName || '작물명';

    const parsedRiskList = parseRiskSummary(data.riskSummary || '');

    if (!parsedRiskList.length) {
        riskListEl.innerHTML = '<li class="risk-empty">리스크 데이터를 표시할 수 없습니다.</li>';
        riskLegendEl.innerHTML = '';
        destroyChart();
        return;
    }

    renderRiskList(parsedRiskList);
    renderRiskLegend(parsedRiskList);
    renderRiskChart(parsedRiskList);
}

function renderRiskList(items) {
    riskListEl.innerHTML = items.map(function (item, index) {
        return ''
            + '<li class="risk-item">'
            + '  <div class="risk-item-left">'
            + '      <span class="risk-item-no">' + (index + 1) + '</span>'
            + '      <span class="risk-item-text">' + escapeHtml(item.label) + '</span>'
            + '  </div>'
            + '  <span class="risk-item-score">' + item.value + '</span>'
            + '</li>';
    }).join('');
}

function renderRiskLegend(items) {
    riskLegendEl.innerHTML = items.map(function (item) {
        return ''
            + '<div class="risk-legend-item">'
            + '  <span class="risk-legend-color" style="background:' + item.color + ';"></span>'
            + '  <span class="risk-legend-label">' + escapeHtml(item.label) + '</span>'
            + '  <span class="risk-legend-value">' + item.value + '%</span>'
            + '</div>';
    }).join('');
}

/* =====================================================
   Chart.js
   ===================================================== */

function renderRiskChart(items) {
    destroyChart();

    const centerTextPlugin = {
        id: 'centerTextPlugin',
        afterDraw: function (chart) {
            const meta = chart.getDatasetMeta(0);

            if (!meta || !meta.data || !meta.data.length) {
                return;
            }

            const x = meta.data[0].x;
            const y = meta.data[0].y;
            const ctx = chart.ctx;

            ctx.save();
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';

            ctx.fillStyle = '#666666';
            ctx.font = '700 35px Noto Sans KR, Arial, sans-serif';
            ctx.fillText('문제 유형', x, y );

            ctx.fillStyle = '#111111';
            ctx.font = '900 28px Noto Sans KR, Arial, sans-serif';

            ctx.restore();
        }
    };

    riskChart = new Chart(riskChartCanvas, {
        type: 'doughnut',
        data: {
            labels: items.map(function (item) { return item.label; }),
            datasets: [{
                data: items.map(function (item) { return item.value; }),
                backgroundColor: items.map(function (item) { return item.color; }),
                borderColor: '#f4f4f4',
                borderWidth: 4,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '62%',
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.label + ' : ' + context.raw + '%';
                        }
                    }
                }
            }
        },
        plugins: [centerTextPlugin]
    });
}

function destroyChart() {
    if (riskChart) {
        riskChart.destroy();
        riskChart = null;
    }
}

/* =====================================================
   파싱
   형식:
   1. 내용 - 30
   2. 내용 - 25
   ===================================================== */

function parseRiskSummary(text) {
    const colors = [
        '#ef5350',
        '#ffa726',
        '#42a5f5',
        '#66bb6a',
        '#ab47bc'
    ];

    if (!text || !text.trim()) {
        return [];
    }

    const lines = text
        .split(/\r?\n/)
        .map(function (line) { return line.trim(); })
        .filter(function (line) { return line !== ''; });

    const result = [];

    lines.forEach(function (line, index) {
        const match = line.match(/^\d+\.\s*(.+?)\s*-\s*(\d+)\s*$/);

        if (!match) {
            return;
        }

        result.push({
            label: match[1].trim(),
            value: Number(match[2]),
            color: colors[index % colors.length]
        });
    });

    return result;
}

/* =====================================================
   기타
   ===================================================== */

function escapeHtml(str) {
    return String(str)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

/* =====================================================
   시작
   ===================================================== */

document.addEventListener('DOMContentLoaded', function () {
    bindLogout();
    bindBackButton();
    loadRiskData();
});