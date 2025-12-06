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

// Multi-Step Booking Flow
let currentBookingStep = 1;
let currentBookingEvent = null;

function showBookingStep(step, modalEl) {
    const steps = modalEl.querySelectorAll('.booking-step');
    steps.forEach(stepEl => {
        if (parseInt(stepEl.dataset.step) === step) {
            stepEl.classList.add('active');
        } else {
            stepEl.classList.remove('active');
        }
    });

    const backBtn = modalEl.querySelector('#bookBackBtn');
    const nextBtn = modalEl.querySelector('#bookNextBtn');
    const submitBtn = modalEl.querySelector('#bookSubmitBtn');

    if (step === 1) {
        backBtn.style.display = 'none';
        nextBtn.style.display = 'inline-block';
        submitBtn.style.display = 'none';
    } else if (step === 2) {
        backBtn.style.display = 'inline-block';
        nextBtn.style.display = 'inline-block';
        submitBtn.style.display = 'none';
    } else if (step === 3) {
        backBtn.style.display = 'inline-block';
        nextBtn.style.display = 'none';
        submitBtn.style.display = 'inline-block';
    }
}

function validateStep1(form) {
    const fields = ['firstname', 'lastname', 'birthdate', 'street', 'houseNumber', 'city', 'postalCode', 'email', 'phone'];
    let valid = true;

    fields.forEach(fieldId => {
        const field = form.querySelector(`#${fieldId}`);
        if (!field.checkValidity()) {
            valid = false;
        }
    });

    if (!valid) {
        form.classList.add('was-validated');
    }

    return valid;
}

function validateStep2(form) {
    const paymentMethod = form.querySelector('input[name="paymentMethod"]:checked');

    if (!paymentMethod) {
        showToast("error", "Please select a payment method");
        return false;
    }

    // If credit card is selected, validate card details
    if (paymentMethod.value === 'creditcard') {
        const cardNumber = form.querySelector('#cardNumber');
        const expiryDate = form.querySelector('#expiryDate');
        const cvv = form.querySelector('#cvv');

        if (!cardNumber.value || !cardNumber.checkValidity()) {
            showToast("error", "Please enter a valid 16-digit card number");
            cardNumber.focus();
            return false;
        }

        if (!expiryDate.value || !expiryDate.checkValidity()) {
            showToast("error", "Please enter expiry date in MM/YY format");
            expiryDate.focus();
            return false;
        }

        // Check if expiry date is in the future
        const [month, year] = expiryDate.value.split('/').map(Number);
        const now = new Date();
        const currentYear = now.getFullYear() % 100;
        const currentMonth = now.getMonth() + 1;

        if (year < currentYear || (year === currentYear && month < currentMonth)) {
            showToast("error", "Card has expired. Please use a valid card");
            expiryDate.focus();
            return false;
        }

        if (!cvv.value || !cvv.checkValidity()) {
            showToast("error", "Please enter a valid CVV (3-4 digits)");
            cvv.focus();
            return false;
        }
    }

    return true;
}

function updateOverview(form, event) {
    // Personal information
    const firstname = form.querySelector('#firstname').value;
    const lastname = form.querySelector('#lastname').value;
    const birthdate = form.querySelector('#birthdate').value;
    const street = form.querySelector('#street').value;
    const houseNumber = form.querySelector('#houseNumber').value;
    const city = form.querySelector('#city').value;
    const postalCode = form.querySelector('#postalCode').value;
    const email = form.querySelector('#email').value;
    const phone = form.querySelector('#phone').value;

    form.querySelector('#overview-name').textContent = `${firstname} ${lastname}`;
    form.querySelector('#overview-birthdate').textContent = birthdate;
    form.querySelector('#overview-address').textContent = `${street} ${houseNumber}, ${postalCode} ${city}`;
    form.querySelector('#overview-email').textContent = email;
    form.querySelector('#overview-phone').textContent = phone;

    // Payment method
    const paymentMethod = form.querySelector('input[name="paymentMethod"]:checked');
    const paymentMethodText = paymentMethod.value.charAt(0).toUpperCase() + paymentMethod.value.slice(1);
    form.querySelector('#overview-payment-method').textContent = paymentMethodText;

    // Calculate deposit and total
    const depositPercent = event.depositPercent || 30;
    const totalPrice = event.price;
    const depositAmount = (totalPrice * depositPercent) / 100;

    form.querySelector('#overview-deposit').textContent = `€${depositAmount.toFixed(2)}`;
    form.querySelector('#overview-total').textContent = `€${totalPrice.toFixed(2)}`;
}


function navigateBookingStep(direction, modalEl, event) {
    const form = modalEl.querySelector('.book-form');

    if (direction === 'next') {
        if (currentBookingStep === 1) {
            if (!validateStep1(form)) return;
            currentBookingStep = 2;
        } else if (currentBookingStep === 2) {
            if (!validateStep2(form)) return;
            updateOverview(form, event);
            currentBookingStep = 3;
        }
    } else if (direction === 'back') {
        if (currentBookingStep > 1) {
            currentBookingStep--;
        }
    }

    showBookingStep(currentBookingStep, modalEl);
}

// Open shared Book Modal
function openBookModal(ev, onCloseCallback) {
    const bookTemplate = document.getElementById("bookModalTemplate");
    const bookContent = bookTemplate.content.cloneNode(true);
    const bookModalEl = bookContent.querySelector(".modal");

    currentBookingStep = 1;
    currentBookingEvent = ev;

    const dateOfBirth = bookModalEl.querySelector("#birthdate");

    // Set max date to today
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    dateOfBirth.setAttribute("max", `${yyyy}-${mm}-${dd}`);

    const paymentMethodCards = bookModalEl.querySelectorAll('.payment-method-card');
    const creditCardForm = bookModalEl.querySelector('#creditCardForm');

    paymentMethodCards.forEach(card => {
        card.addEventListener('click', function() {
            const radio = this.querySelector('input[type="radio"]');
            radio.checked = true;

            // Remove selected class from all cards
            paymentMethodCards.forEach(c => c.classList.remove('selected'));
            // Add selected class to clicked card
            this.classList.add('selected');

            // Show/hide credit card form
            if (radio.value === 'creditcard') {
                creditCardForm.style.display = 'block';
                // Make credit card fields required
                creditCardForm.querySelector('#cardNumber').setAttribute('required', '');
                creditCardForm.querySelector('#expiryDate').setAttribute('required', '');
                creditCardForm.querySelector('#cvv').setAttribute('required', '');
            } else {
                creditCardForm.style.display = 'none';
                // Remove required attribute
                creditCardForm.querySelector('#cardNumber').removeAttribute('required');
                creditCardForm.querySelector('#expiryDate').removeAttribute('required');
                creditCardForm.querySelector('#cvv').removeAttribute('required');
            }
        });
    });

    // Navigation buttons
    const backBtn = bookModalEl.querySelector('#bookBackBtn');
    const nextBtn = bookModalEl.querySelector('#bookNextBtn');

    backBtn.addEventListener('click', () => {
        navigateBookingStep('back', bookModalEl, ev);
    });

    nextBtn.addEventListener('click', () => {
        navigateBookingStep('next', bookModalEl, ev);
    });

    // Form submit handler
    bookModalEl.querySelector(".book-form").addEventListener("submit", async (e) => {
        e.preventDefault();

        const form = bookModalEl.querySelector(".book-form");
        const submitBtn = bookModalEl.querySelector('#bookSubmitBtn');

        // Disable submit button to prevent double submission
        submitBtn.disabled = true;
        submitBtn.textContent = 'Processing...';

        console.log("Submitting booking with payment for event:", ev);

        const paymentMethod = form.querySelector('input[name="paymentMethod"]:checked').value;



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
            eventId: ev?.id,
            paymentMethod: paymentMethod
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
            const res = await fetch('/api/bookings/createWithPayment', {
                method: "POST",
                body: formData
            });

            const responseData = await res.json();

            if (res.ok && responseData.success) {
                // Payment successful
                showToast("success", `Booking confirmed! Payment successful. Confirmation sent to ${responseData.email}`);

                // Close modal
                const modal = bootstrap.Modal.getInstance(bookModalEl);
                modal.hide();

                // Reload events
                await loadEvents();
            } else {
                // Payment failed - go back to step 2
                showToast("error", responseData.message || "Payment failed. Please try again.");
                currentBookingStep = 2;
                showBookingStep(currentBookingStep, bookModalEl);
                submitBtn.disabled = false;
                submitBtn.textContent = 'Confirm & Pay';
            }

        }
        catch (err) {
            console.error(err);
            showToast("error", "Failed to process booking. Please try again.");
            currentBookingStep = 2;
            showBookingStep(currentBookingStep, bookModalEl);
            submitBtn.disabled = false;
            submitBtn.textContent = 'Confirm & Pay';
        }
    });

    // Cleanup and optional callback on close
    bookModalEl.addEventListener("hidden.bs.modal", () => {
        bookModalEl.remove();
        currentBookingStep = 1;
        currentBookingEvent = null;
        if (typeof onCloseCallback === "function") onCloseCallback();
    });

    document.body.appendChild(bookModalEl);
    const bsBookModal = new bootstrap.Modal(bookModalEl);
    bsBookModal.show();

    showBookingStep(1, bookModalEl);

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
    const canBook = ["ADMIN", "BACKOFFICE", "FRONTOFFICE", "USER", "GUEST"].includes(role);


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
    const canBook = ["ADMIN", "BACKOFFICE", "FRONTOFFICE", "USER", "GUEST"].includes(role);

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
