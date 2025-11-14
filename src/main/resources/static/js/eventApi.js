// Load events from backend
async function loadEvents() {
    try {
        const res = await fetch("/api/events");
        if (!res.ok) throw new Error("Failed to load events");
        const events = await res.json();
        renderEvents(events);
    } catch (e) {
        console.error(e);
        showToast("error", "Could not load events");
    }
}

// Open shared Book Modal
function openBookModal(ev, onCloseCallback) {
    const bookTemplate = document.getElementById("bookModalTemplate");
    const bookContent = bookTemplate.content.cloneNode(true);
    const bookModalEl = bookContent.querySelector(".modal");

    const nextBtn = bookModalEl.querySelector("#nextBtn");
    const backBtn = bookModalEl.querySelector("#backBtn");
    const submitBtn = bookModalEl.querySelector("#submitBtn");

    // show booking confirmation step
    nextBtn.addEventListener("click", () => {
        nextBtn.style.display = "none";
        backBtn.style.display = "inline-block";
        submitBtn.style.display = "inline-block";
    });

    // go back to first step
    backBtn.addEventListener("click", () => {
        nextBtn.style.display = "inline-block";
        backBtn.style.display = "none";
        submitBtn.style.display = "none";
    });

    // Form submit
    bookModalEl.querySelector(".book-form").addEventListener("submit", (e) => {
        e.preventDefault();
        showToast("success", "Booking functionality coming soon!");
        bootstrap.Modal.getInstance(bookModalEl).hide();
    });

    // Cleanup and optional callback on close
    bookModalEl.addEventListener("hidden.bs.modal", () => {
        bookModalEl.remove();
        if (typeof onCloseCallback === "function") onCloseCallback();
    });

    document.body.appendChild(bookModalEl);
    const bsBookModal = new bootstrap.Modal(bookModalEl);
    bsBookModal.show();

}

// getting a location object
function formatLocation(location) {
    if (!location) return "-";
    return `${location.street} ${location.houseNumber}, ${location.postalCode} ${location.city}, ${location.state}, ${location.country}`;
}

// Render event cards and attach click listener to open details modal
function renderEvents(events) {
    const container = document.getElementById("eventsContainer");
    const template = document.getElementById("eventCardTemplate");


    container.innerHTML = "";

    events.forEach(ev => {
        // Card
        const card = template.content.cloneNode(true);
        const el = card.querySelector(".event-card");

        el.querySelector(".card-title").textContent = ev.name;
        el.querySelector(".card-location").textContent = formatLocation(ev.location);
        el.querySelector(".card-price").textContent = ev.price.toFixed(2) + " €";
        el.querySelector(".card-status").textContent = ev.status;
        el.dataset.status = ev.status.toLowerCase();
        el.dataset.location = formatLocation(ev.location).toLowerCase();

        const imgEl = el.querySelector(".event-image");
        if (ev.imageIds && ev.imageIds.length > 0) {
            imgEl.src = `/api/events/image/${ev.imageIds[0]}`;
        } else {
            imgEl.src = "/images/default.jpg";
        }

        // Card click -> open details modal
        el.addEventListener("click", () => openDetailsModal(ev));

        // --- ENABLE CARD BOOK BUTTON ---
        const bookSection = el.querySelector(".book-section");
        const bookBtn = el.querySelector(".btn-open-book");

        if (ev.status && ev.status.toLowerCase() === "active") {
            bookSection.style.display = "block";

            bookBtn.addEventListener("click", (e) => {
                e.stopPropagation(); // prevent opening details modal
                openBookModal(ev);
            });
        }
        else {
            bookSection.style.display = "none";
        }

        container.appendChild(card);
    });
}

// Open details modal and populate info
function openDetailsModal(ev) {
    const detailsTemplate = document.getElementById("detailsModalTemplate");
    const modalContent = detailsTemplate.content.cloneNode(true);
    const modalEl = modalContent.querySelector(".modal");

    modalEl.querySelector(".modal-title").textContent = ev.name;
    modalEl.querySelector(".d-name").textContent = ev.name;
    modalEl.querySelector(".d-desc").textContent = ev.description || "-";
    modalEl.querySelector(".d-location").textContent = formatLocation(ev.location);
    modalEl.querySelector(".d-date").textContent = ev.date;
    modalEl.querySelector(".d-price").textContent = ev.price.toFixed(2) + " €";
    modalEl.querySelector(".d-status").textContent = ev.status;

    // Images gallery
    const gallery = modalEl.querySelector(".gallery");
    gallery.innerHTML = "";
    if (ev.imageIds && ev.imageIds.length > 0) {
        ev.imageIds.forEach(imgId => {
            const imgEl = document.createElement("img");
            imgEl.src = `/api/events/image/${imgId}`;
            imgEl.className = "img-thumbnail";
            imgEl.style.width = "120px";
            imgEl.style.height = "80px";
            imgEl.style.objectFit = "cover";
            gallery.appendChild(imgEl);
        });
    }

    // Cancel Button logic
    const cancelSection = modalEl.querySelector(".cancel-section");
    const cancelBtn = modalEl.querySelector(".btn-open-cancel");
    const cancelReasonEl = modalEl.querySelector(".d-cancelreason");

    const bookSection = modalEl.querySelector(".book-section");
    const bookBtn = modalEl.querySelector(".btn-open-book");

    if (ev.status && ev.status.toLowerCase() === "cancelled") {
        cancelSection.style.display = "none";
        bookSection.style.display = "none";
        cancelReasonEl.style.display = "block"; // show reason
        cancelReasonEl.querySelector("span").textContent = ev.cancellationReason || "-";
    } else {
        cancelSection.style.display = "block"; // show cancel button
        bookSection.style.display = "block";
        cancelReasonEl.style.display = "none";

        // Cancel handler
        cancelBtn.addEventListener("click", () => {
            const cancelTemplate = document.getElementById("cancelModalTemplate");
            const cancelContent = cancelTemplate.content.cloneNode(true);
            const cancelModalEl = cancelContent.querySelector(".modal");

            // Hide Details-Modal
            const detailsBsModal = bootstrap.Modal.getInstance(modalEl);
            detailsBsModal.hide();

            // Set up form submit
            cancelModalEl.querySelector(".cancel-form").addEventListener("submit", async (e) => {
                e.preventDefault();
                const reason = cancelModalEl.querySelector(".cancellationReason").value;
                try {
                    const res = await fetch(`/api/events/cancel/${ev.id}`, {
                        method: "PUT",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ reason })
                    });
                    if (!res.ok) throw new Error(await res.text());
                    showToast("success", "Event cancelled successfully!");
                    await loadEvents();
                    filterEvents();

                    // Update UI
                    ev.status = "CANCELLED";
                    ev.cancellationReason = reason;

                    modalEl.querySelector(".d-status").textContent = ev.status;
                    cancelReasonEl.style.display = "block";
                    cancelReasonEl.querySelector("span").textContent = reason;
                    cancelSection.style.display = "none";
                    bookSection.style.display = "none";

                    bootstrap.Modal.getInstance(cancelModalEl).hide();
                } catch (err) {
                    console.error(err);
                    showToast("error", "Failed to cancel event");
                }
            });

            // When Cancel-Modal is closed, show Details-Modal again
            cancelModalEl.addEventListener("hidden.bs.modal", () => {
                cancelModalEl.remove();
                detailsBsModal.show();
            });

            document.body.appendChild(cancelModalEl);
            const bsCancelModal = new bootstrap.Modal(cancelModalEl);
            bsCancelModal.show();

        });

        // Book button inside details modal
        bookBtn.addEventListener("click", () => {
            const detailsModal = bootstrap.Modal.getInstance(modalEl);
            detailsModal.hide();
            openBookModal(ev, () => {
                detailsModal.show();
            });
        });

    }

    document.body.appendChild(modalEl);
    const bsModal = new bootstrap.Modal(modalEl);
    bsModal.show();

    modalEl.addEventListener("hidden.bs.modal", () => {
        modalEl.remove();
    });
}

// Page initialize
document.addEventListener("DOMContentLoaded", () => {
    loadEvents();
});
