function showToast(type, message) {
    const toastContainer = document.getElementById('toastContainer');
    const toastTemplate = document.getElementById('toastTemplate');

    const toastElement = toastTemplate.content.cloneNode(true).querySelector('.toast');

    const toastId = 'toast-' + Date.now();
    toastElement.id = toastId;

    let icon, title, toastClass;
    if (type === 'success') {
        icon = '✓';
        title = 'Success';
        toastClass = 'toast-success';
    } else if (type === 'error') {
        icon = '✗';
        title = 'Error';
        toastClass = 'toast-error';
    }

    toastElement.classList.add(toastClass);

    toastElement.querySelector('.toast-icon').textContent = icon;
    toastElement.querySelector('.toast-title').textContent = title;

    toastElement.querySelector('.toast-body').textContent = message;

    toastContainer.appendChild(toastElement);

    const bsToast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 5000
    });

    bsToast.show();

    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const toastData = document.getElementById('toastData');
    if (toastData) {
        const type = toastData.dataset.type;
        const message = toastData.dataset.message;
        if (type && message) {
            showToast(type, message);
        }
    }
});