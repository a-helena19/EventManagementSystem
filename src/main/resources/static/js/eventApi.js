// GLOBAL variable to store current editing event
let editCurrentEvent = null;

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

// Function to check if there is at least 1 image
window.updateImageRequirement = function(imagesInput, existingImagesContainer) {
    const existingImages = existingImagesContainer.querySelectorAll("img");
    const hasNewFiles = imagesInput.files && imagesInput.files.length > 0;

    if (existingImages.length === 0 && !hasNewFiles) {
        imagesInput.setAttribute("required", ""); // mindestens ein Bild erforderlich
    } else {
        imagesInput.removeAttribute("required");
    }
}

// Next step for booking form
function bookNextStep(readonly) {
    const fields = [
        "firstname", "lastname", "birthdate", "street", "houseNumber",
        "city", "postalCode", "email", "phone"
    ].map(id => document.getElementById(id));

    const nextBtn = document.getElementById("nextBtn");
    const backBtn = document.getElementById("backBtn");
    const submitBtn = document.getElementById("submitBtn");

    if(readonly) {
        nextBtn.style.display = "none";
        backBtn.style.display = "inline-block";
        submitBtn.style.display = "inline-block";
        fields.forEach(el => {
            el.setAttribute("readonly", "");
            el.style.border = "0px";
            el.style.backgroundColor = "#e9ecef";
            el.style.color = "#495057";
        });
    }
    else {
        nextBtn.style.display = "inline-block";
        backBtn.style.display = "none";
        submitBtn.style.display = "none";
        fields.forEach(el => {
            el.removeAttribute("readonly");
            el.style.border = "1px solid";
            el.style.backgroundColor = "white";
            el.style.color = "black";
        });
    }
}

// --- Validation for Next Button (Book) ---
function bookCheckValidation(next) {
    const form = document.querySelector(".book-form");

    if (form.checkValidity()) {
        bookNextStep(next);
    } else {
        form.classList.add('was-validated');
    }
}

// Open shared Book Modal
function openBookModal(ev, onCloseCallback) {
    const bookTemplate = document.getElementById("bookModalTemplate");
    const bookContent = bookTemplate.content.cloneNode(true);
    const bookModalEl = bookContent.querySelector(".modal");

    const dateOfBirth = bookModalEl.querySelector("#birthdate");

    // Set max date to today
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    dateOfBirth.setAttribute("max", `${yyyy}-${mm}-${dd}`);

    // Form submit handler
    bookModalEl.querySelector(".book-form").addEventListener("submit", async (e) => {
        e.preventDefault();

        const form = bookModalEl.querySelector(".book-form");

        console.log("Submitting booking for event:", ev);
        console.log("eventId =", ev?.id);
        // Get values from the modal (only inside!)
        const data = {
            firstname: form.querySelector("#firstname").value,
            lastname: form.querySelector("#lastname").value,
            birthdate: form.querySelector("#birthdate").value,
            street: form.querySelector("#street").value,
            houseNumber: form.querySelector("#houseNumber").value,
            city: form.querySelector("#city").value,
            postalCode: form.querySelector("#postalCode").value,
            email: form.querySelector("#email").value,
            phone: form.querySelector("#phone").value,
            eventId: ev?.id
        };

        const formData = new FormData();
        for (const k in data) formData.append(k, data[k]);

        // Attach the logged-in user's id so bookings remain visible even if a different email is entered
        const userInfoRaw = localStorage.getItem("userInfo");
        const user = userInfoRaw ? JSON.parse(userInfoRaw) : null;
        if (user?.id) {
            formData.append("userId", user.id);
        }

        try {
            const res = await fetch('/api/bookings/create', {
                method: "POST",
                body: formData
            });

            if (!res.ok) {
                const text = await res.text();
                showToast("error", text);
                return;
            }

            const data = await res.json();
            showToast("success", `Booking for "${data.name}" created successfully!`);

            // Reset UI
            form.reset();
            bookNextStep(false);

            // Close modal
            const modal = bootstrap.Modal.getInstance(bookModalEl);
            modal.hide();
        }
        catch (err) {
            console.error(err);
            showToast("error", "Failed to book event");
        }
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


// Open Cancel Modal
function openCancelModal(ev, modalEl, cancelReasonEl, cancelSection, bookSection, editSection) {
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

            // Update UI status and cancellation reason
            ev.status = "CANCELLED";
            ev.cancellationReason = reason;

            const statusBadge = modalEl.querySelector(".d-status");
            statusBadge.textContent = ev.status;
            statusBadge.classList.add("bg-danger");
            cancelReasonEl.style.display = "block";
            cancelReasonEl.querySelector("span").textContent = reason;
            cancelSection.style.display = "none";
            bookSection.style.display = "none";
            editSection.style.display = "none";

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
    const role = typeof getCurrentUserRole === "function" ? getCurrentUserRole() : "GUEST";
    const canBook = ["ADMIN", "BACKOFFICE", "FRONTOFFICE", "USER"].includes(role);


    container.innerHTML = "";

    events.forEach(ev => {
        // Card
        const card = template.content.cloneNode(true);
        const el = card.querySelector(".event-card");

        el.querySelector(".card-title").textContent = ev.name;
        el.querySelector(".card-location").textContent = formatLocation(ev.location);
        el.querySelector(".card-price").textContent = ev.price.toFixed(2) + " €";

        // Status badge styling
        const statusBadge = el.querySelector(".card-status");
        statusBadge.textContent = ev.status;

        // Add badge color based on status
        if (ev.status === "ACTIVE") {
            statusBadge.classList.add("bg-success");
        } else if (ev.status === "CANCELLED") {
            statusBadge.classList.add("bg-danger");
        } else if (ev.status === "EXPIRED") {
            statusBadge.classList.add("bg-warning");
        }


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

        if (ev.status && ev.status.toLowerCase() === "active" && canBook) {
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
    const role = typeof getCurrentUserRole === "function" ? getCurrentUserRole() : "GUEST";
    const canManageEvents = role === "ADMIN" || role === "BACKOFFICE";
    const canBook = ["ADMIN", "BACKOFFICE", "FRONTOFFICE", "USER"].includes(role);

    modalEl.querySelector(".modal-title").textContent = ev.name;
    modalEl.querySelector(".d-name").textContent = ev.name;
    modalEl.querySelector(".d-desc").textContent = ev.description || "-";
    modalEl.querySelector(".d-location").textContent = formatLocation(ev.location);
    modalEl.querySelector(".d-date").textContent = ev.startDate;
    modalEl.querySelector(".d-price").textContent = ev.price.toFixed(2) + " €";

    // Status badge
    const statusBadge = modalEl.querySelector(".d-status");
    statusBadge.textContent = ev.status;

    // Add badge color based on status
    if (ev.status === "ACTIVE") {
        statusBadge.classList.add("bg-success");
    } else if (ev.status === "CANCELLED") {
        statusBadge.classList.add("bg-danger");
    } else if (ev.status === "EXPIRED") {
        statusBadge.classList.add("bg-warning");
    }

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

    const editSection = modalEl.querySelector(".edit-section");
    const editBtn = modalEl.querySelector(".btn-open-edit");

    if (ev.status && ev.status.toLowerCase() === "cancelled" || ev.status && ev.status.toLowerCase() === "expired") {
        cancelSection.style.display = "none";
        bookSection.style.display = "none";
        editSection.style.display = "none";
        if (ev.status && ev.status.toLowerCase() === "cancelled") {
            cancelReasonEl.style.display = "block"; // show reason
            cancelReasonEl.querySelector("span").textContent = ev.cancellationReason || "-";
        }
    } else {
        cancelSection.style.display = canManageEvents ? "block" : "none"; // show cancel button
        bookSection.style.display = canBook ? "block" : "none";
        editSection.style.display = canManageEvents ? "block" : "none";
        cancelReasonEl.style.display = "none";

        if (canManageEvents) {
            editBtn.addEventListener("click", () => openEditModal(ev, modalEl))
            cancelBtn.addEventListener("click", () => openCancelModal(ev, modalEl, cancelReasonEl, cancelSection, bookSection, editSection));
        }

        if (canBook) {
            //Booking handler
            bookBtn.addEventListener("click", () => {
                const detailsBsModal = bootstrap.Modal.getInstance(modalEl);
                detailsBsModal.hide();

                openBookModal(ev, () => {
                    // Das wird ausgeführt, wenn Book-Modal geschlossen wird!
                    detailsBsModal.show();})

            });
        }

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

function openEditModal(ev, detailsModalEl) {

    // store globally so editEvent.js can read it
    editCurrentEvent = ev;

    // hide details modal FIRST (falls offen)
    if (detailsModalEl) {
        const instance = bootstrap.Modal.getInstance(detailsModalEl);
        if (instance) instance.hide();
    }

    // open edit modal
    const editModal = new bootstrap.Modal(document.getElementById("editEventModal"));
    editModal.show();
}
