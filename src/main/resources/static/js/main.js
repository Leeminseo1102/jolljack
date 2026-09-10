const mainHeader = document.querySelector('.main-header');
const mainSidebar = document.getElementById('mainSidebar');
const sidebarToggle = document.getElementById('sidebarToggle');

if (mainHeader) {
    window.addEventListener('scroll', function () {
        if (window.scrollY > 10) {
            mainHeader.style.boxShadow = '0 4px 20px rgba(35, 70, 42, 0.12)';
        } else {
            mainHeader.style.boxShadow = '0 2px 12px rgba(35, 70, 42, 0.06)';
        }
    });
}

const serviceCards = document.querySelectorAll('.service-card');

serviceCards.forEach(function (card) {
    card.setAttribute('tabindex', '0');
    card.setAttribute('role', 'button');

    card.addEventListener('click', function () {
        const url = card.dataset.url;

        if (url) {
            location.href = url;
        }
    });

    card.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            card.click();
        }
    });
});

const btnLogout = document.getElementById('btnLogout');

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
// 사이드바
if (mainSidebar && sidebarToggle) {

    sidebarToggle.addEventListener('click', function () {

        mainSidebar.classList.toggle('open');

        const isOpen = mainSidebar.classList.contains('open');

        sidebarToggle.setAttribute(
            'aria-expanded',
            isOpen
        );

        sidebarToggle.setAttribute(
            'aria-label',
            isOpen ? '사이드바 닫기' : '사이드바 열기'
        );

        sidebarToggle.textContent =
            isOpen ? '›' : '‹';
    });
}