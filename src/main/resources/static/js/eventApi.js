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

// Render event cards and attach click listener to open details modal
function renderEvents(events) {
    const container = document.getElementById("eventsContainer");
    const template = document.getElementById("eventCardTemplate");
    const detailsTemplate = document.getElementById("detailsModalTemplate");

    container.innerHTML = "";

    events.forEach(ev => {
        // Card
        const card = template.content.cloneNode(true);
        const el = card.querySelector(".event-card");

        el.querySelector(".card-title").textContent = ev.name;
        el.querySelector(".card-location").textContent = ev.location;
        el.querySelector(".card-price").textContent = ev.price.toFixed(2) + " €";
        el.querySelector(".card-status").textContent = ev.status;
        el.dataset.status = ev.status.toLowerCase();
        el.dataset.location = ev.location.toLowerCase();

        const imgEl = el.querySelector(".event-image");
        if (ev.images && ev.images.length > 0) {
            imgEl.src = `/api/events/image/${ev.images[0].id}`;
        } else {
            imgEl.src = "/images/placeholder.png";
        }

        // Card click -> open details modal
        el.addEventListener("click", () => openDetailsModal(ev));

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
    modalEl.querySelector(".d-location").textContent = ev.location;
    modalEl.querySelector(".d-date").textContent = ev.date;
    modalEl.querySelector(".d-price").textContent = ev.price.toFixed(2) + " €";
    modalEl.querySelector(".d-status").textContent = ev.status;

    // Images gallery
    const gallery = modalEl.querySelector(".gallery");
    gallery.innerHTML = "";
    if (ev.images && ev.images.length > 0) {
        ev.images.forEach(img => {
            const imgEl = document.createElement("img");
            imgEl.src = `/api/events/image/${img.id}`;
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
    if (ev.status === "CANCELLED") {
        cancelSection.style.display = "none";
        cancelReasonEl.style.display = "block"; // Grund anzeigen
        cancelReasonEl.querySelector("span").textContent = ev.cancellationReason || "-";
    } else {
        cancelSection.style.display = "block"; // Cancel Button anzeigen
        cancelReasonEl.style.display = "none";
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

                    ev.status = "CANCELLED";
                    ev.cancellationReason = reason;

                    modalEl.querySelector(".d-status").textContent = ev.status;
                    cancelReasonEl.style.display = "block";
                    cancelReasonEl.querySelector("span").textContent = reason;
                    cancelSection.style.display = "none";

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
    }

    document.body.appendChild(modalEl);
    const bsModal = new bootstrap.Modal(modalEl);
    bsModal.show();

    modalEl.addEventListener("hidden.bs.modal", () => {
        modalEl.remove();
    });
}

// Page init
document.addEventListener("DOMContentLoaded", () => {
    loadEvents();
});
