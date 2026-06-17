const btnLogout = document.getElementById('btnLogout');
const btnScrollDetail = document.getElementById('btnScrollDetail');
const detailSection = document.getElementById('detailSection');

const fullRegionNameEl = document.getElementById('fullRegionName');
const cropNameEl = document.getElementById('cropName');
const scoreEl = document.getElementById('score');

const mapCropNameEl = document.getElementById('mapCropName');
const mapRegionNameEl = document.getElementById('mapRegionName');
const cropIconEl = document.getElementById('cropIcon');

const recommendedReasonEl = document.getElementById('recommendedReason');
const cultivationMethodEl = document.getElementById('cultivationMethod');

const yearAvgTempValueEl = document.getElementById('yearAvgTempValue');
const yearMaxTempValueEl = document.getElementById('yearMaxTempValue');
const yearMinTempValueEl = document.getElementById('yearMinTempValue');

const sixMonthAvgTempValueEl = document.getElementById('sixMonthAvgTempValue');
const sixMonthMaxTempValueEl = document.getElementById('sixMonthMaxTempValue');
const sixMonthMinTempValueEl = document.getElementById('sixMonthMinTempValue');

const threeMonthAvgTempValueEl = document.getElementById('threeMonthAvgTempValue');
const threeMonthMaxTempValueEl = document.getElementById('threeMonthMaxTempValue');
const threeMonthMinTempValueEl = document.getElementById('threeMonthMinTempValue');

let map = null;
let polygonList = [];

/* =====================================================
   geojson 하드코딩 매핑
   ===================================================== */

const GEOJSON_FILE_MAP = {
    '서울특별시': {
        '도봉구': 'dobonggu.geojson',
        '동대문구': 'dongdaemungu.geojson',
        '동작구': 'dongjakgu.geojson',
        '은평구': 'eunpyeonggu.geojson',
        '강북구': 'gangbukgu.geojson',
        '강동구': 'gangdonggu.geojson',
        '강남구': 'gangnamgu.geojson',
        '강서구': 'gangseogu.geojson',
        '금천구': 'geumcheongu.geojson',
        '구로구': 'gurogu.geojson',
        '관악구': 'gwanakgu.geojson',
        '광진구': 'gwangjingu.geojson',
        '종로구': 'jongrogu.geojson',
        '중구': 'junggu.geojson',
        '중랑구': 'jungnanggu.geojson',
        '마포구': 'mapogu.geojson',
        '노원구': 'nowongu.geojson',
        '서초구': 'seochogu.geojson',
        '서대문구': 'seodaemungu.geojson',
        '성북구': 'seongbukgu.geojson',
        '성동구': 'seongdonggu.geojson',
        '송파구': 'songpagu.geojson',
        '양천구': 'yangcheongu.geojson',
        '영등포구': 'yeongdeungpogu.geojson',
        '용산구': 'yongsangu.geojson'
    },

    '부산광역시': {
        '북구': 'bukgu.geojson',
        '부산진구': 'busanjingu.geojson',
        '동구': 'donggu.geojson',
        '동래구': 'dongnaegu.geojson',
        '강서구': 'gangseogu.geojson',
        '금정구': 'geumjeonggu.geojson',
        '기장군': 'gijanggun.geojson',
        '해운대구': 'haeundaegu.geojson',
        '중구': 'junggu.geojson',
        '남구': 'namgu.geojson',
        '사하구': 'sahagu.geojson',
        '사상구': 'sasanggu.geojson',
        '서구': 'seogu.geojson',
        '수영구': 'suyeonggu.geojson',
        '영도구': 'yeongdogu.geojson',
        '연제구': 'yeonjegu.geojson'
    },

    '대구광역시': {
        '북구': 'bukgu.geojson',
        '달서구': 'dalseogu.geojson',
        '달성군': 'dalseonggun.geojson',
        '동구': 'donggu.geojson',
        '군위군': 'gunwigung.geojson',
        '중구': 'junggu.geojson',
        '남구': 'namgu.geojson',
        '서구': 'seogu.geojson',
        '수성구': 'suseonggu.geojson'
    },

    '대전광역시': {
        '대덕구': 'daedeokgu.geojson',
        '동구': 'donggu.geojson',
        '중구': 'junggu.geojson',
        '서구': 'seogu.geojson',
        '유성구': 'yuseonggu.geojson'
    },

    '광주광역시': {
        '북구': 'bukgu.geojson',
        '동구': 'donggu.geojson',
        '광산구': 'gwangsangu.geojson',
        '남구': 'namgu.geojson',
        '서구': 'seogu.geojson'
    },

    '울산광역시': {
        '북구': 'bukgu.geojson',
        '동구': 'donggu.geojson',
        '중구': 'junggu.geojson',
        '남구': 'namgu.geojson',
        '울주군': 'uljugun.geojson'
    },

    '인천광역시': {
        '부평구': 'bupyeonggu.geojson',
        '동구': 'donggu.geojson',
        '강화군': 'ganghwagun.geojson',
        '계양구': 'gyeyanggu.geojson',
        '중구': 'junggu.geojson',
        '미추홀구': 'michuholgu.geojson',
        '남동구': 'namdonggu.geojson',
        '옹진군': 'ongjingun.geojson',
        '서구': 'seogu.geojson',
        '연수구': 'yeonsugu.geojson'
    },

    '세종특별자치시': {
        '': 'sejongsi.geojson'
    },

    '경기도': {
        '안산시': 'ansanshi.geojson',
        '안성시': 'anseongshi.geojson',
        '안양시': 'anyangshi.geojson',
        '부천시': 'bucheon.geojson',
        '동두천시': 'dongducheonshi.geojson',
        '가평군': 'gapyeonggun.geojson',
        '김포시': 'gimposhi.geojson',
        '고양시': 'goyangshi.geojson',
        '군포시': 'gunposhi.geojson',
        '구리시': 'gurishi.geojson',
        '과천시': 'gwacheonshi.geojson',
        '광주시': 'gwangjushi.geojson',
        '광명시': 'gwangmyeongshi.geojson',
        '하남시': 'hanamshi.geojson',
        '화성시': 'hwaseongshi.geojson',
        '이천시': 'icheonshi.geojson',
        '남양주시': 'namyangjushi.geojson',
        '오산시': 'osanshi.geojson',
        '파주시': 'pajushi.geojson',
        '포천시': 'pocheonshi.geojson',
        '평택시': 'pyeongtaekshi.geojson',
        '성남시': 'seongnamshi.geojson',
        '시흥시': 'siheungshi.geojson',
        '수원시': 'suwonshi.geojson',
        '의정부시': 'uijeongbushi.geojson',
        '의왕시': 'uiwangshi.geojson',
        '양주시': 'yangjushi.geojson',
        '양평군': 'yangpyeonggun.geojson',
        '여주시': 'yeojushi.geojson',
        '연천군': 'yeoncheongun.geojson',
        '용인시': 'yonginshi.geojson'
    },

    '강원도': {
        '철원군': 'cheorwongun.geojson',
        '춘천시': 'chuncheonshi.geojson',
        '동해시': 'donghaeshi.geojson',
        '강릉시': 'gangneungshi.geojson',
        '고성군': 'goseonggun.geojson',
        '횡성군': 'hoengseonggun.geojson',
        '홍천군': 'hongcheongun.geojson',
        '화천군': 'hwacheongun.geojson',
        '인제군': 'injegun.geojson',
        '정선군': 'jeongseongun.geojson',
        '평창군': 'pyeongchanggun.geojson',
        '삼척시': 'samcheokshi.geojson',
        '속초시': 'sokchoshi.geojson',
        '태백시': 'taebaekshi.geojson',
        '원주시': 'wonjushi.geojson',
        '양구군': 'yanggugun.geojson',
        '양양군': 'yangyanggun.geojson',
        '영월군': 'yeongwolgung.geojson'
    },

    '강원특별자치도': {
        '철원군': 'cheorwongun.geojson',
        '춘천시': 'chuncheonshi.geojson',
        '동해시': 'donghaeshi.geojson',
        '강릉시': 'gangneungshi.geojson',
        '고성군': 'goseonggun.geojson',
        '횡성군': 'hoengseonggun.geojson',
        '홍천군': 'hongcheongun.geojson',
        '화천군': 'hwacheongun.geojson',
        '인제군': 'injegun.geojson',
        '정선군': 'jeongseongun.geojson',
        '평창군': 'pyeongchanggun.geojson',
        '삼척시': 'samcheokshi.geojson',
        '속초시': 'sokchoshi.geojson',
        '태백시': 'taebaekshi.geojson',
        '원주시': 'wonjushi.geojson',
        '양구군': 'yanggugun.geojson',
        '양양군': 'yangyanggun.geojson',
        '영월군': 'yeongwolgung.geojson'
    },

    '충청북도': {
        '보은군': 'boeungun.geojson',
        '청주시': 'cheongjushi.geojson',
        '충주시': 'chungjushi.geojson',
        '단양군': 'danyanggun.geojson',
        '음성군': 'eumseonggun.geojson',
        '괴산군': 'gosaengun.geojson',
        '제천시': 'jecheonshi.geojson',
        '증평군': 'jeungpyeonggun.geojson',
        '진천군': 'jincheongun.geojson',
        '옥천군': 'okcheongun.geojson',
        '영동군': 'yeongdonggun.geojson'
    },

    '충청남도': {
        '아산시': 'asanshi.geojson',
        '보령시': 'boryeongshi.geojson',
        '부여군': 'buyeogun.geojson',
        '천안시': 'cheonanshi.geojson',
        '청양군': 'cheongyanggun.geojson',
        '당진시': 'dangjinshi.geojson',
        '금산군': 'geumsangun.geojson',
        '공주시': 'gongjushi.geojson',
        '계룡시': 'gyeryongshi.geojson',
        '홍성군': 'hongseonggun.geojson',
        '논산시': 'nonsanshi.geojson',
        '서천군': 'seocheongun.geojson',
        '서산시': 'seosanshi.geojson',
        '태안군': 'taeangun.geojson',
        '예산군': 'yesangun.geojson'
    },

    '전라남도': {
        '보성군': 'boseonggun.geojson',
        '담양군': 'damyanggun.geojson',
        '강진군': 'gangjingun.geojson',
        '고흥군': 'goheunggun.geojson',
        '곡성군': 'gokseonggun.geojson',
        '구례군': 'guregun.geojson',
        '광양시': 'gwangyangshi.geojson',
        '해남군': 'haenamgun.geojson',
        '함평군': 'hampyeonggun.geojson',
        '화순군': 'hwasungun.geojson',
        '장흥군': 'jangheunggun.geojson',
        '장성군': 'jangseonggun.geojson',
        '진도군': 'jindogun.geojson',
        '목포시': 'mokposhi.geojson',
        '무안군': 'muangun.geojson',
        '나주시': 'najushi.geojson',
        '신안군': 'sinangun.geojson',
        '순천시': 'suncheonshi.geojson',
        '완도군': 'wandogun.geojson',
        '영암군': 'yeongamgun.geojson',
        '영광군': 'yeonggwanggun.geojson',
        '여수시': 'yeosushi.geojson'
    },

    '전라북도': {
        '부안군': 'buangun.geojson',
        '김제시': 'gimjeshi.geojson',
        '고창군': 'gochanggun.geojson',
        '군산시': 'gunsanshi.geojson',
        '익산시': 'iksanshi.geojson',
        '임실군': 'imsilgun.geojson',
        '장수군': 'jangsugun.geojson',
        '정읍시': 'jeongeupshi.geojson',
        '전주시': 'jeonjushi.geojson',
        '진안군': 'jinangun.geojson',
        '무주군': 'mujugun.geojson',
        '남원시': 'namwonshi.geojson',
        '순창군': 'sunchanggun.geojson',
        '완주군': 'wanjugun.geojson'
    },

    '전북특별자치도': {
        '부안군': 'buangun.geojson',
        '김제시': 'gimjeshi.geojson',
        '고창군': 'gochanggun.geojson',
        '군산시': 'gunsanshi.geojson',
        '익산시': 'iksanshi.geojson',
        '임실군': 'imsilgun.geojson',
        '장수군': 'jangsugun.geojson',
        '정읍시': 'jeongeupshi.geojson',
        '전주시': 'jeonjushi.geojson',
        '진안군': 'jinangun.geojson',
        '무주군': 'mujugun.geojson',
        '남원시': 'namwonshi.geojson',
        '순창군': 'sunchanggun.geojson',
        '완주군': 'wanjugun.geojson'
    },

    '경상북도': {
        '안동시': 'andongshi.geojson',
        '봉화군': 'bonghwagun.geojson',
        '청도군': 'cheongdogun.geojson',
        '청송군': 'cheongsonggun.geojson',
        '칠곡군': 'chilgokgun.geojson',
        '김천시': 'gimcheonshi.geojson',
        '고령군': 'goryeonggun.geojson',
        '구미시': 'gumishi.geojson',
        '경주시': 'gyeongjushi.geojson',
        '경산시': 'gyeongsanshi.geojson',
        '문경시': 'mungyeongshi.geojson',
        '포항시': 'pohangshi.geojson',
        '상주시': 'sangjushi.geojson',
        '성주군': 'seongjugun.geojson',
        '의성군': 'uiseonggun.geojson',
        '울진군': 'uljingun.geojson',
        '울릉군': 'ulleunggun.geojson',
        '예천군': 'yecheongun.geojson',
        '영천시': 'yeongcheonshi.geojson',
        '영덕군': 'yeongdeokgun.geojson',
        '영주시': 'yeongjushi.geojson',
        '영양군': 'yeongyanggun.geojson'
    },

    '경상남도': {
        '창녕군': 'changnyeonggun.geojson',
        '창원시': 'changwonshi.geojson',
        '거창군': 'geochanggun.geojson',
        '거제시': 'geojeshi.geojson',
        '김해시': 'gimhaeshi.geojson',
        '고성군': 'goseonggun.geojson',
        '하동군': 'hadonggun.geojson',
        '함안군': 'hamangun.geojson',
        '함양군': 'hamyanggun.geojson',
        '합천군': 'hapcheongun.geojson',
        '진주시': 'jinjushi.geojson',
        '밀양시': 'miryangshi.geojson',
        '남해군': 'namhaegun.geojson',
        '사천시': 'sacheonshi.geojson',
        '산청군': 'sancheongun.geojson',
        '통영시': 'tongyeongshi.geojson',
        '의령군': 'uiryeonggun.geojson',
        '양산시': 'yangsanshi.geojson'
    },

    '제주도': {
        '제주시': 'jejushi.geojson',
        '서귀포시': 'seogwiposhi.geojson'
    },

    '제주특별자치도': {
        '제주시': 'jejushi.geojson',
        '서귀포시': 'seogwiposhi.geojson'
    }
};

const SIDO_FOLDER_MAP = {
    '서울특별시': 'Seoul',
    '부산광역시': 'Busan',
    '대구광역시': 'Daegu',
    '인천광역시': 'Incheon',
    '광주광역시': 'Gwangju',
    '대전광역시': 'Daejeon',
    '울산광역시': 'Ulsan',
    '세종특별자치시': 'Sejong',
    '경기도': 'Gyeonggido',
    '강원도': 'Gangwondo',
    '강원특별자치도': 'Gangwondo',
    '충청북도': 'Chungcheongbukdo',
    '충청남도': 'Chungcheongnamdo',
    '전라북도': 'Jeonbukdo',
    '전북특별자치도': 'Jeonbukdo',
    '전라남도': 'Jeollanamdo',
    '경상북도': 'Gyeongsangbukdo',
    '경상남도': 'Gyeongsangnamdo',
    '제주도': 'Jejudo',
    '제주특별자치도': 'Jejudo'
};

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

function bindScrollButton() {
    if (!btnScrollDetail || !detailSection) {
        return;
    }

    btnScrollDetail.addEventListener('click', function () {
        detailSection.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    });
}

/* =====================================================
   결과 데이터
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

function loadResultData() {
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
                '결과 조회 실패',
                res.msg || '결과 조회 중 오류가 발생했습니다.',
                moveUrl
            );
            return;
        }

        renderResultData(res);
        renderMapByRegion(res.fullRegionName, res.cropName);
    });
}

function renderResultData(data) {
    fullRegionNameEl.textContent = data.fullRegionName || '-';
    cropNameEl.textContent = data.cropName || '-';
    scoreEl.textContent = (data.score !== null && data.score !== undefined) ? data.score + '점' : '-';

    mapCropNameEl.textContent = data.cropName || '-';
    mapRegionNameEl.textContent = data.fullRegionName || '-';

    recommendedReasonEl.textContent = data.recommendedReason || '내용이 없습니다.';
    cultivationMethodEl.textContent = data.cultivationMethod || '내용이 없습니다.';

    renderWeatherMetrics(data.weatherSummary || '');

    setCropIcon(data.cropName);
    highlightLegend(data.cropName);
}

function setCropIcon(cropName) {
    const iconMap = {
        '벼': CTX + 'img/bbuu.png',
        '감자': CTX + 'img/gamja.png',
        '사과': CTX + 'img/saga.png',
        '포도': CTX + 'img/podo.png',
        '고추': CTX + 'img/gochu.png',
        '배추': CTX + 'img/bachu.png'
    };

    cropIconEl.src = iconMap[cropName] || (CTX + 'img/logo.png');
}

function highlightLegend(cropName) {
    const legendItems = document.querySelectorAll('.result-legend-item');

    legendItems.forEach(function (item) {
        item.style.opacity = '0.35';
        item.style.transform = 'scale(0.96)';
    });

    legendItems.forEach(function (item) {
        if (item.dataset.crop === cropName) {
            item.style.opacity = '1';
            item.style.transform = 'scale(1.05)';
        }
    });
}

/* =====================================================
   날씨 요약 카드
   ===================================================== */

function renderWeatherMetrics(text) {
    const metrics = extractWeatherMetrics(text);

    yearAvgTempValueEl.textContent = metrics.year.avg;
    yearMaxTempValueEl.textContent = metrics.year.max;
    yearMinTempValueEl.textContent = metrics.year.min;

    sixMonthAvgTempValueEl.textContent = metrics.sixMonth.avg;
    sixMonthMaxTempValueEl.textContent = metrics.sixMonth.max;
    sixMonthMinTempValueEl.textContent = metrics.sixMonth.min;

    threeMonthAvgTempValueEl.textContent = metrics.threeMonth.avg;
    threeMonthMaxTempValueEl.textContent = metrics.threeMonth.max;
    threeMonthMinTempValueEl.textContent = metrics.threeMonth.min;
}

function extractWeatherMetrics(text) {
    return {
        year: {
            avg: extractMetric(text, /최근\s*1년[\s\S]*?평균기온\s*([-\d.]+)\s*℃/),
            max: extractMetric(text, /최근\s*1년[\s\S]*?(?:일)?최고기온(?:\s*평균)?\s*([-\d.]+)\s*℃/),
            min: extractMetric(text, /최근\s*1년[\s\S]*?(?:일)?최저기온(?:\s*평균)?\s*([-\d.]+)\s*℃/)
        },
        sixMonth: {
            avg: extractMetric(text, /최근\s*6개월[\s\S]*?평균기온\s*([-\d.]+)\s*℃/),
            max: extractMetric(text, /최근\s*6개월[\s\S]*?(?:일)?최고기온(?:\s*평균)?\s*([-\d.]+)\s*℃/),
            min: extractMetric(text, /최근\s*6개월[\s\S]*?(?:일)?최저기온(?:\s*평균)?\s*([-\d.]+)\s*℃/)
        },
        threeMonth: {
            avg: extractMetric(text, /최근\s*3개월[\s\S]*?평균기온\s*([-\d.]+)\s*℃/),
            max: extractMetric(text, /최근\s*3개월[\s\S]*?(?:일)?최고기온(?:\s*평균)?\s*([-\d.]+)\s*℃/),
            min: extractMetric(text, /최근\s*3개월[\s\S]*?(?:일)?최저기온(?:\s*평균)?\s*([-\d.]+)\s*℃/)
        }
    };
}

function extractMetric(text, regex) {
    const match = text.match(regex);

    if (!match) {
        return '-';
    }

    const value = Number(match[1]);

    if (Number.isNaN(value)) {
        return '-';
    }

    return value.toFixed(1) + '℃';
}

/* =====================================================
   작물 색상
   ===================================================== */

function getCropColor(cropName) {
    const colorMap = {
        '벼': '#E4E66B',
        '감자': '#F0B400',
        '사과': '#EA0000',
        '포도': '#A45AE9',
        '고추': '#A8001F',
        '배추': '#61E61A'
    };

    return colorMap[cropName] || '#A45AE9';
}

/* =====================================================
   지도
   ===================================================== */

function initMap() {
    if (!window.naver || !window.naver.maps) {
        openErrorModal(
            '지도 로드 실패',
            '네이버 지도 스크립트가 로드되지 않았습니다. client id를 확인해주세요.'
        );
        return false;
    }

    map = new naver.maps.Map('resultMap', {
        center: new naver.maps.LatLng(36.5, 127.8),
        zoom: 7,
        zoomControl: true,
        zoomControlOptions: {
            position: naver.maps.Position.TOP_RIGHT
        }
    });

    const mapEl = document.getElementById('resultMap');
    if (mapEl) {
        mapEl.style.opacity = '1';
    }

    console.log('지도 초기화 완료');
    return true;
}

function clearPolygons() {
    polygonList.forEach(function (polygon) {
        polygon.setMap(null);
    });

    polygonList = [];
}

function renderMapByRegion(fullRegionName, cropName) {
    if (!map && !initMap()) {
        return;
    }

    const geoJsonPath = getGeoJsonPath(fullRegionName);

    console.log('fullRegionName =', fullRegionName);
    console.log('cropName =', cropName);
    console.log('CTX =', CTX);
    console.log('geoJsonPath =', geoJsonPath);

    if (!geoJsonPath) {
        openErrorModal(
            '지도 데이터 없음',
            '해당 지역의 geojson 파일 경로를 찾지 못했습니다.'
        );
        return;
    }

    fetch(geoJsonPath)
        .then(function (res) {
            console.log('fetch status =', res.status);
            console.log('fetch ok =', res.ok);

            if (!res.ok) {
                throw new Error('fetch 실패 : ' + res.status + ', path : ' + geoJsonPath);
            }

            return res.json();
        })
        .then(function (geojson) {
            console.log('geojson 로드 성공');
            console.log('geojson type =', geojson.type);
            console.log('geojson features =', geojson.features);

            drawGeoJsonOnMap(geojson, getCropColor(cropName));
        })
        .catch(function (e) {
            console.error('geojson 처리 에러 =', e);

            openErrorModal(
                '지도 데이터 오류',
                'geojson 처리 중 오류가 발생했습니다.'
            );
        });
}

function drawGeoJsonOnMap(geojson, fillColor) {
    clearPolygons();

    const bounds = new naver.maps.LatLngBounds();

    if (!geojson || !geojson.features || !Array.isArray(geojson.features)) {
        throw new Error('geojson.features 형식이 올바르지 않습니다.');
    }

    console.log('drawGeoJsonOnMap 진입');
    console.log('feature 개수 =', geojson.features.length);

    geojson.features.forEach(function (feature, index) {
        if (!feature.geometry) {
            console.warn('geometry 없음, index =', index);
            return;
        }

        const geometry = feature.geometry;
        console.log('feature index =', index, ', geometry type =', geometry.type);

        if (geometry.type === 'Polygon') {
            const polygonPaths = convertPolygonCoordinates(geometry.coordinates, bounds);

            const polygon = new naver.maps.Polygon({
                map: map,
                paths: polygonPaths,
                fillColor: fillColor,
                fillOpacity: 0.55,
                strokeColor: fillColor,
                strokeOpacity: 0.28,
                strokeWeight: 1
            });

            polygonList.push(polygon);
            return;
        }

        if (geometry.type === 'MultiPolygon') {
            geometry.coordinates.forEach(function (polygonCoords) {
                const polygonPaths = convertPolygonCoordinates(polygonCoords, bounds);

                const polygon = new naver.maps.Polygon({
                    map: map,
                    paths: polygonPaths,
                    fillColor: fillColor,
                    fillOpacity: 0.55,
                    strokeColor: fillColor,
                    strokeOpacity: 0.28,
                    strokeWeight: 1
                });

                polygonList.push(polygon);
            });

            return;
        }

        console.warn('지원하지 않는 geometry type =', geometry.type);
    });

    console.log('polygonList.length =', polygonList.length);

    if (polygonList.length > 0) {
        map.fitBounds(bounds, {
            top: 60,
            right: 60,
            bottom: 60,
            left: 60
        });
    } else {
        console.warn('polygon이 없습니다. 좌표 변환 또는 geometry를 확인하세요.');
    }
}

function convertPolygonCoordinates(coordinates, bounds) {
    if (!Array.isArray(coordinates)) {
        throw new Error('coordinates가 배열이 아닙니다.');
    }

    return coordinates.map(function (ring, ringIndex) {
        if (!Array.isArray(ring)) {
            throw new Error('ring 배열 형식이 올바르지 않습니다. ringIndex = ' + ringIndex);
        }

        return ring.map(function (coord, coordIndex) {
            if (!Array.isArray(coord) || coord.length < 2) {
                throw new Error('coord 형식 오류. ringIndex = ' + ringIndex + ', coordIndex = ' + coordIndex);
            }

            const lng = Number(coord[0]);
            const lat = Number(coord[1]);

            if (Number.isNaN(lng) || Number.isNaN(lat)) {
                throw new Error('좌표 숫자 변환 실패. lng = ' + coord[0] + ', lat = ' + coord[1]);
            }

            const latLng = new naver.maps.LatLng(lat, lng);
            bounds.extend(latLng);

            return latLng;
        });
    });
}

/* =====================================================
   geojson 경로 매핑
   ===================================================== */

function getGeoJsonPath(fullRegionName) {
    const region = parseRegion(fullRegionName);

    if (!region || !region.sidoName) {
        return '';
    }

    const folderName = SIDO_FOLDER_MAP[region.sidoName] || '';
    if (!folderName) {
        return '';
    }

    const sidoMap = GEOJSON_FILE_MAP[region.sidoName];
    if (!sidoMap) {
        return '';
    }

    const fileName = sidoMap[region.sigunguName || ''];
    if (!fileName) {
        return '';
    }

    return CTX + 'geojson/' + folderName + '/' + fileName;
}

function parseRegion(fullRegionName) {
    if (!fullRegionName) {
        return null;
    }

    const safeName = fullRegionName.trim();
    const parts = safeName.split(/\s+/);

    if (parts.length === 1) {
        return {
            sidoName: parts[0],
            sigunguName: ''
        };
    }

    return {
        sidoName: parts[0],
        sigunguName: parts.slice(1).join('')
    };
}

/* =====================================================
   시작
   ===================================================== */

document.addEventListener('DOMContentLoaded', function () {
    bindLogout();
    bindScrollButton();
    loadResultData();
});