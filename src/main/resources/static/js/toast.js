function showToast(type, message) {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) return;

    // Create Toast
    const toastElement = document.createElement("div");
    toastElement.className = `toast align-items-center border-0`;
    toastElement.setAttribute("role", "alert");
    toastElement.setAttribute("aria-live", "assertive");
    toastElement.setAttribute("aria-atomic", "true");

    // Icon & Title
    let icon = '';
    let title = '';
    let toastClass = '';

    if (type === "success") {
        icon = '✓';
        title = 'Success';
        toastClass = 'toast-success';
    } else if (type === "error") {
        icon = '✗';
        title = 'Error';
        toastClass = 'toast-error';
    }

    toastElement.classList.add(toastClass);

    // HTML Struktur wie im alten Template
    toastElement.innerHTML = `
        <div class="toast-header">
            <strong class="me-auto">
                <span class="toast-icon">${icon}</span>
                <span class="toast-title">${title}</span>
            </strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body">${message}</div>
    `;

    toastContainer.appendChild(toastElement);

    const bsToast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 5000
    });
    bsToast.show();

    toastElement.addEventListener("hidden.bs.toast", () => toastElement.remove());
}
