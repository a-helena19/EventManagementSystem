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
function updateImageRequirement(imagesInput, existingImagesContainer) {
    const existingImages = existingImagesContainer.querySelectorAll("img");
    const hasNewFiles = imagesInput.files && imagesInput.files.length > 0;

    if (existingImages.length === 0 && !hasNewFiles) {
        imagesInput.setAttribute("required", ""); // mindestens ein Bild erforderlich
    } else {
        imagesInput.removeAttribute("required");
    }
}


function listFileNames() {
    const fileInput = document.getElementById('images');
    const fileNamesDiv = document.getElementById('fileNames');
    const files = Array.from(fileInput.files);

    if (!files.length) {
        fileNamesDiv.textContent = "No files selected";
        return;
    }

    fileNamesDiv.innerHTML = `<ul>${files.map(f => `<li>${f.name}</li>`).join('')}</ul>`;
}

// Next step for edit form
function editNextStep(readonly) {
    const fields = [
        "name", "description", "editStreet", "editHouseNumber", "editCity",
        "editPostalCode", "state", "country", "date", "price"
    ].map(id => document.getElementById(id));
    const images = document.getElementById("images");
    const imagesLabel = document.querySelector('label[for="images"]');
    const existingImagesContainer = document.getElementById("existingImages");

    const nextBtn = document.getElementById("edit-nextBtn");
    const backBtn = document.getElementById("edit-backBtn");
    const submitBtn = document.getElementById("edit-submitBtn");

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
        images.classList.add('readonly-file');
        images.style.border = "0px";
        images.style.backgroundColor = "#e9ecef";
        images.style.pointerEvents = "none";

        if (imagesLabel) {
            imagesLabel.style.pointerEvents = "none";
            imagesLabel.style.opacity = "1";
            imagesLabel.style.cursor = "not-allowed";
        }

        // Disable remove buttons on existing images
        if (existingImagesContainer) {
            existingImagesContainer.querySelectorAll("button").forEach(btn => {
                btn.style.pointerEvents = "none";
                btn.style.opacity = "0.5";
            });
        }

    } else {
        nextBtn.style.display = "inline-block";
        backBtn.style.display = "none";
        submitBtn.style.display = "none";
        fields.forEach(el => {
            el.removeAttribute("readonly");
            el.style.border = "1px solid";
            el.style.backgroundColor = "white";
            el.style.color = "black";
        });

        images.classList.remove('readonly-file');
        images.style.border = "1px solid";
        images.style.backgroundColor = "white";
        images.style.pointerEvents = "auto";

        if (imagesLabel) {
            imagesLabel.style.pointerEvents = "auto";
            imagesLabel.style.opacity = "1";
            imagesLabel.style.cursor = "pointer";
        }

        // Enable remove buttons on existing images
        if (existingImagesContainer) {
            existingImagesContainer.querySelectorAll("button").forEach(btn => {
                btn.style.pointerEvents = "auto";
                btn.style.opacity = "1";
            });
        }
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

// ---Validation for Next Button (Edit) ---
function editCheckValidation(next) {
    const form = document.querySelector(".edit-form");

    if (form.checkValidity()) {
        editNextStep(next);
    } else {
        form.classList.add('was-validated');
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

function openEditModal(ev, modalEl) {
    const editTemplate = document.getElementById("editModalTemplate");
    const editContent = editTemplate.content.cloneNode(true);
    const editModalEl = editContent.querySelector(".modal");

    const detailsBsModal = bootstrap.Modal.getInstance(modalEl);
    detailsBsModal.hide();

    const name = editModalEl.querySelector('#name');
    const description = editModalEl.querySelector('#description');
    const street = editModalEl.querySelector('#editStreet');
    const houseNumber = editModalEl.querySelector('#editHouseNumber');
    const city = editModalEl.querySelector('#editCity');
    const postalCode = editModalEl.querySelector('#editPostalCode');
    const state = editModalEl.querySelector('#state');
    const country = editModalEl.querySelector('#country');
    const date = editModalEl.querySelector('#date');
    const price = editModalEl.querySelector('#price');
    const images = editModalEl.querySelector("#images");
    // Fill inputs

    name.value = ev.name;
    description.value = ev.description;
    street.value = ev.location.street;
    houseNumber.value = ev.location.houseNumber;
    city.value = ev.location.city;
    postalCode.value = ev.location.postalCode;
    state.value = ev.location.state;
    country.value = ev.location.country;
    date.value = ev.date;
    price.value = ev.price;

    let imagesToDelete = [];
    const existingImagesContainer = editModalEl.querySelector("#existingImages");
    existingImagesContainer.innerHTML = "";

    if (ev.imageIds && ev.imageIds.length > 0) {
        ev.imageIds.forEach(id => {
            const wrapper = document.createElement("div");
            wrapper.classList.add("position-relative", "m-2");
            wrapper.style.width = "120px";

            const img = document.createElement("img");
            img.src = `/api/events/image/${id}`;
            img.classList.add("img-thumbnail");
            img.style.width = "120px";
            img.style.height = "120px";
            img.style.objectFit = "cover";

            const btn = document.createElement("button");
            btn.type = "button";
            btn.classList.add("btn", "btn-sm", "btn-danger", "position-absolute", "top-0", "end-0");
            btn.innerText = "X";
            btn.onclick = () => {
                imagesToDelete.push(id);
                wrapper.remove();
                // Check whether input should be required now
                updateImageRequirement(images, existingImagesContainer);
            };

            wrapper.appendChild(img);
            wrapper.appendChild(btn);
            existingImagesContainer.appendChild(wrapper);
        });
    }

    // Event listener für neue Dateien
    images.addEventListener("change", () => {
        updateImageRequirement(images, existingImagesContainer);
        listFileNames();
    });


    // Initial check after rendering
    updateImageRequirement(images, existingImagesContainer);


    //Track whether submit was pressed
    let submitted = false;

    // Form submission to backend
    editModalEl.querySelector(".edit-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        submitted = true;

        // Build FormData for backend
        const formData = new FormData();
        formData.append("name", name.value);
        formData.append("description", description.value);
        formData.append("street", street.value);
        formData.append("houseNumber", houseNumber.value);
        formData.append("city", city.value);
        formData.append("postalCode", postalCode.value);
        formData.append("state", state.value);
        formData.append("country", country.value);
        formData.append("date", date.value);
        formData.append("price", price.value);

        if (images && images.files.length > 0) {
            for (const file of images.files) {
                formData.append("images", file);
            }
        }

        // Deleted images
        if (imagesToDelete.length > 0) {
            formData.append("deleteImageIds", JSON.stringify(imagesToDelete));
        }

        for (let [key, value] of formData.entries()) {
            console.log(key, value);
        }

        try {
            const res = await fetch(`/api/events/edit/${ev.id}/status/${ev.status}`, {
                method: "PUT",
                body: formData
            });

            if (res.ok) {
                const data = await res.json();
                showToast("success", `Event "${data.name}" was updated successfully!`);
                await loadEvents();

                const editBsModal = bootstrap.Modal.getInstance(editModalEl);
                editBsModal.hide();
            } else {
                const text = await res.text();
                showToast("error", text);
            }
        } catch (err) {
            showToast("error", "Network error: " + err.message);
        }
    });

    editModalEl.addEventListener("hidden.bs.modal", () => {
        editModalEl.remove();

        if (!submitted) {
            detailsBsModal.show();
        }
    });

    document.body.appendChild(editModalEl);
    const bsEditModal = new bootstrap.Modal(editModalEl);
    bsEditModal.show();
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

            // Update UI
            ev.status = "CANCELLED";
            ev.cancellationReason = reason;

            modalEl.querySelector(".d-status").textContent = ev.status;
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

    const editSection = modalEl.querySelector(".edit-section");
    const editBtn = modalEl.querySelector(".btn-open-edit");

    if (ev.status && ev.status.toLowerCase() === "cancelled") {
        cancelSection.style.display = "none";
        bookSection.style.display = "none";
        editSection.style.display = "none";
        cancelReasonEl.style.display = "block"; // show reason
        cancelReasonEl.querySelector("span").textContent = ev.cancellationReason || "-";
    } else {
        cancelSection.style.display = "block"; // show cancel button
        bookSection.style.display = "block";
        editSection.style.display ="block";
        cancelReasonEl.style.display = "none";

        editBtn.addEventListener("click", () => openEditModal(ev, modalEl))
        //Booking handler
        bookBtn.addEventListener("click", () => {
            const detailsBsModal = bootstrap.Modal.getInstance(modalEl);
            detailsBsModal.hide();

            openBookModal(ev, () => {
                // Das wird ausgeführt, wenn Book-Modal geschlossen wird!
                detailsBsModal.show();})

        });

        // Cancel handler
        cancelBtn.addEventListener("click", () => openCancelModal(ev, modalEl, cancelReasonEl, cancelSection, bookSection, editSection));

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
