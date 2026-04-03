window.CommonModal = (function () {
    const modal = document.getElementById('commonModal');
    const dim = modal ? modal.querySelector('.common-modal-dim') : null;
    const titleEl = document.getElementById('commonModalTitle');
    const bodyEl = document.getElementById('commonModalBody');
    const extraEl = document.getElementById('commonModalExtra');
    const cancelBtn = document.getElementById('commonModalCancel');
    const confirmBtn = document.getElementById('commonModalConfirm');

    let confirmCallback = null;
    let cancelCallback = null;

    function resetModal() {
        if (!titleEl || !bodyEl || !extraEl || !cancelBtn || !confirmBtn) {
            return;
        }

        titleEl.innerHTML = '';
        bodyEl.innerHTML = '';
        extraEl.innerHTML = '';

        cancelBtn.textContent = '취소';
        confirmBtn.textContent = '확인';

        cancelBtn.style.display = 'inline-flex';
        confirmBtn.style.display = 'inline-flex';

        cancelBtn.className = 'common-modal-btn common-modal-btn-cancel';
        confirmBtn.className = 'common-modal-btn common-modal-btn-confirm';
    }

    function close() {
        if (!modal) {
            return;
        }

        modal.classList.add('hidden');
        document.body.style.overflow = '';
        confirmCallback = null;
        cancelCallback = null;
        resetModal();
    }

    function applyButtonClass(button, type) {
        button.className = 'common-modal-btn';

        if (type === 'neutral') {
            button.classList.add('common-modal-btn-neutral');
            return;
        }

        if (type === 'danger') {
            button.classList.add('common-modal-btn-cancel');
            return;
        }

        button.classList.add('common-modal-btn-confirm');
    }

    function open(options) {
        if (!modal || !titleEl || !bodyEl || !extraEl || !cancelBtn || !confirmBtn) {
            return;
        }

        const opts = options || {};

        titleEl.innerHTML = opts.title || '';
        bodyEl.innerHTML = opts.body || '';
        extraEl.innerHTML = opts.extraHtml || '';

        cancelBtn.textContent = opts.cancelText || '취소';
        confirmBtn.textContent = opts.confirmText || '확인';

        cancelBtn.style.display = opts.hideCancel ? 'none' : 'inline-flex';
        confirmBtn.style.display = opts.hideConfirm ? 'none' : 'inline-flex';

        applyButtonClass(cancelBtn, opts.cancelType || 'danger');
        applyButtonClass(confirmBtn, opts.confirmType || 'primary');

        confirmCallback = typeof opts.onConfirm === 'function' ? opts.onConfirm : null;
        cancelCallback = typeof opts.onCancel === 'function' ? opts.onCancel : null;

        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }

    if (confirmBtn) {
        confirmBtn.addEventListener('click', function () {
            const fn = confirmCallback;
            close();

            if (fn) {
                fn();
            }
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', function () {
            const fn = cancelCallback;
            close();

            if (fn) {
                fn();
            }
        });
    }

    if (dim) {
        dim.addEventListener('click', function () {
            const fn = cancelCallback;
            close();

            if (fn) {
                fn();
            }
        });
    }

    document.addEventListener('keydown', function (e) {
        if (!modal || modal.classList.contains('hidden')) {
            return;
        }

        if (e.key === 'Escape') {
            const fn = cancelCallback;
            close();

            if (fn) {
                fn();
            }
        }
    });

    return {
        open: open,
        close: close
    };
})();