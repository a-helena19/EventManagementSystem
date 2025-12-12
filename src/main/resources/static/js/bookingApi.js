// Load bookings from backend
async function loadBookings() {
    try {
        await AppSession.loadSession();
        const session = AppSession.getUser();

        if (!session.isLoggedIn) {
            showToast("error", "Please log in to view bookings");
            return;
        }

        const role = session.role || "GUEST";
        const userId = session.id;
        const userEmail = session.email;

        // Staff roles (ADMIN, BACKOFFICE, FRONTOFFICE) see all bookings
        // Regular USER only sees their own bookings (filtered by email)
        const staffRoles = ["ADMIN", "BACKOFFICE", "FRONTOFFICE"];
        const isStaff = staffRoles.includes(role);

        let url;
        if (isStaff) {
            // Staff sees all bookings
            url = "/api/bookings";
        } else {
            // Regular user only sees their own bookings, matched by account rather than the email used during booking
            if (userId) {
                url = `/api/bookings?userId=${encodeURIComponent(userId)}`;
            } else {
                url = `/api/bookings?email=${encodeURIComponent(userEmail)}`;
            }
        }

        const res = await fetch(url);
        if (res.status === 401 || res.status === 403) {
            console.warn("Session expired or invalid. Logging out.");
            await AppSession.logout();
            alert("Your session has expired. Please log in again.");
            window.location.href = "/homepage";
            return;
        }

        if (!res.ok) throw new Error("Failed to load bookings");
        const bookings = await res.json();
        await renderBookings(bookings);
    } catch (e) {
        console.error(e);
        showToast("error", "Could not load bookings");
    }
}

// Format address object
function formatAddress(address) {
    if (!address) return "-";
    return `${address.street} ${address.houseNumber}, ${address.postalCode} ${address.city}`;
}

// Format event location from event object
function formatEventLocation(location) {
    if (!location) return "-";
    return `${location.street} ${location.houseNumber}, ${location.postalCode} ${location.city}, ${location.state}, ${location.country}`;
}

// Render booking cards and attach click listener to open details modal
async function renderBookings(bookings) {
    const container = document.getElementById("bookingsContainer");
    const template = document.getElementById("bookingCardTemplate");

    container.innerHTML = "";

    // Load all events first
    let events = [];
    try {
        const eventRes = await fetch("/api/events");
        if (eventRes.ok) {
            events = await eventRes.json();
        }
    } catch (error) {
        console.error("Failed to load events:", error);
    }

    bookings.forEach(booking => {
        // Create card from template
        const card = template.content.cloneNode(true);
        const el = card.querySelector(".booking-card");

        // Find the corresponding event
        const event = events.find(e => e.id === booking.eventId);

        // Fill booking data
        if (event) {
            el.querySelector(".card-title").textContent = event.name;
        } else {
            el.querySelector(".card-title").textContent = "Event not found";
        }

        el.querySelector(".card-name").textContent = `${booking.firstname} ${booking.lastname}`;
        el.querySelector(".card-email").textContent = booking.email;
        el.querySelector(".card-date").textContent = booking.bookingDate;

        // Status badge styling
        const statusBadge = el.querySelector(".card-status");
        statusBadge.textContent = booking.status;

        // Set data attributes for filtering
        el.dataset.status = booking.status.toLowerCase();
        el.dataset.name = `${booking.firstname} ${booking.lastname}`.toLowerCase();
        el.dataset.email = booking.email.toLowerCase();
        el.dataset.eventid = booking.eventId;

        // Add event name and location for search
        if (event) {
            el.dataset.eventname = event.name.toLowerCase();
            el.dataset.eventlocation = formatEventLocation(event.location).toLowerCase();
        } else {
            el.dataset.eventname = "";
            el.dataset.eventlocation = "";
        }

        // Add badge color based on status
        if (booking.status === "ACTIVE") {
            statusBadge.classList.add("bg-success");
        } else if (booking.status === "CANCELLED") {
            statusBadge.classList.add("bg-danger");
        } else if (booking.status === "EXPIRED") {
            statusBadge.classList.add("bg-warning");
        } else if (booking.status === "EVENTCANCELLED") {
            statusBadge.classList.add("bg-secondary");
        }

        // Details button click handler
        const detailsBtn = el.querySelector(".btn-open-details");
        detailsBtn.addEventListener("click", (e) => {
            e.stopPropagation(); // Prevent card click
            openDetailsModal(booking);
        });

        // Card click also opens details
        el.addEventListener("click", () => openDetailsModal(booking));

        container.appendChild(card);
    });
}

// Calculate Refund in back-end
async function loadAndSetRefund(eventId, cancelModalEl) {
    try {
        const res = await fetch(`/api/bookings/refund/${eventId}`);
        if (!res.ok) throw new Error("Failed to load refund");

        const dto = await res.json();

        const amount = Number(dto.refund);

        // Refund-Span find and set
        const refundSpan = cancelModalEl.querySelector("#calculateRefund");
        if (refundSpan) {
            const formatted = amount
                .toFixed(2)
                .replace(".", ",");

            refundSpan.textContent = `You will get ${formatted} € as refund`;
        }
    } catch (err) {
        showToast("error", `cannot load refund: ${err}`);
    }
}

// Open Cancel Modal
async function openCancelBookingModal(booking, event, modalEl, cancelReasonEl, cancelDateEl, cancelSection) {
    const cancelTemplate = document.getElementById("cancelModalTemplate");
    const cancelContent = cancelTemplate.content.cloneNode(true);
    const cancelModalEl = cancelContent.querySelector(".modal");

    // Hide Details-Modal
    const detailsBsModal = bootstrap.Modal.getInstance(modalEl);
    detailsBsModal.hide();

    loadAndSetRefund(event.id, cancelModalEl);

    // Set up form submit
    cancelModalEl.querySelector(".cancel-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const reason = cancelModalEl.querySelector(".cancellationReason").value;
        try {
            const res = await fetch(`/api/bookings/cancel/${event.id}/${booking.id}`, {
                method: "PUT",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({reason})
            });
            if (!res.ok) throw new Error(await res.text());
            showToast("success", "Event cancelled successfully!");
            await loadBookings();
            filterBookings();

            const date = new Date();
            const yyyy = date.getFullYear();
            const mm = String(date.getMonth() + 1).padStart(2, '0');
            const dd = String(date.getDate()).padStart(2, '0');

            const cancelDate = `${yyyy}-${mm}-${dd}`;

            // Update UI status and cancellation reason
            booking.status = "CANCELLED";
            booking.cancelReason = reason;
            booking.cancelDate = cancelDate;

            const statusBadge = modalEl.querySelector(".d-status");
            statusBadge.textContent = booking.status;
            statusBadge.classList.add("bg-danger");
            cancelReasonEl.style.display = "block";
            cancelReasonEl.querySelector(".info-value").textContent = reason;
            cancelDateEl.style.display = "block";
            cancelDateEl.querySelector(".info-value").textContent = cancelDate;
            cancelSection.style.display = "none";

            bootstrap.Modal.getInstance(cancelModalEl).hide();
        } catch (err) {
            console.error(err);
            showToast("error", "Failed to cancel booking");
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
// Open details modal and populate booking information
async function openDetailsModal(booking) {
    const detailsTemplate = document.getElementById("detailsModalTemplate");
    const modalContent = detailsTemplate.content.cloneNode(true);
    const modalEl = modalContent.querySelector(".modal");

    const cancelReasonEl = modalEl.querySelector(".d-cancelReason");
    const cancelDateEl = modalEl.querySelector(".d-cancelDate");

    // Fill personal information (left column)
    modalEl.querySelector(".d-name").textContent = `${booking.firstname} ${booking.lastname}`;
    modalEl.querySelector(".d-birthdate").textContent = booking.birthDate || "-";
    modalEl.querySelector(".d-email").textContent = booking.email;
    modalEl.querySelector(".d-phone").textContent = booking.phoneNumber || "-";
    modalEl.querySelector(".d-address").textContent = formatAddress(booking.address);
    cancelReasonEl.querySelector(".info-value").textContent = booking.cancelReason || "-";
    cancelDateEl.querySelector(".info-value").textContent = booking.cancelDate || "-";

    // Fill booking information (right column)
    modalEl.querySelector(".d-bookingdate").textContent = booking.bookingDate;

    // Initially show loading state
    modalEl.querySelector(".d-eventname").textContent = "Loading...";
    modalEl.querySelector(".d-eventlocation").textContent = "Loading...";

    // Status badge
    const statusBadge = modalEl.querySelector(".d-status");
    statusBadge.textContent = booking.status;

    // Add badge color based on status
    if (booking.status === "ACTIVE") {
        statusBadge.classList.add("bg-success");
    } else if (booking.status === "CANCELLED") {
        statusBadge.classList.add("bg-danger");
    } else if (booking.status === "EXPIRED") {
        statusBadge.classList.add("bg-warning");
    } else if (booking.status === "EVENTCANCELLED") {
        statusBadge.classList.add("bg-secondary");
    }

    // Add modal to DOM and show it
    document.body.appendChild(modalEl);
    const bsModal = new bootstrap.Modal(modalEl);
    bsModal.show();

    // Fetch event details
    try {
        const eventRes = await fetch("/api/events");
        if (eventRes.ok) {
            const events = await eventRes.json();
            const event = events.find(e => e.id === booking.eventId);

            if (event) {
                modalEl.querySelector(".d-eventname").textContent = event.name;
                modalEl.querySelector(".d-eventlocation").textContent = formatEventLocation(event.location);
                const price = event.price + "€";
                modalEl.querySelector(".d-eventprice").textContent = price;

                const cancelBtn = modalEl.querySelector(".btn-open-cancel");
                const cancelSection = modalEl.querySelector(".cancel-section");
                const cancelDeadlineEl = modalEl.querySelector(".d-cancelDeadline");
                const cancelDeadlineVal = event.cancelDeadline;
                if (cancelDeadlineVal !== null) {
                    cancelDeadlineEl.querySelector(".info-value").textContent = cancelDeadlineVal;
                } else {
                    cancelDeadlineEl.querySelector(".info-value").textContent = "-";
                }
                // Booking can ONLY be canceled if ACTIVE
                if (booking.status === "ACTIVE") {
                    cancelSection.style.display = "block";
                    cancelBtn.addEventListener("click", () => openCancelBookingModal(booking, event, modalEl, cancelReasonEl, cancelDateEl, cancelSection));
                } else {
                    cancelSection.style.display = "none";
                    cancelReasonEl.style.display = "block";
                    cancelDateEl.style.display = "block";
                }

            } else {
                modalEl.querySelector(".d-eventname").textContent = "Event not found";
                modalEl.querySelector(".d-eventlocation").textContent = "-";
                modalEl.querySelector(".d-eventprice").textContent = "-";
                const cancelDeadlineEl = modalEl.querySelector(".d-cancelDeadline");
                cancelDeadlineEl.querySelector(".info-value").textContent = "-";
            }
        }
    } catch (error) {
        console.error("Failed to load event details:", error);
        modalEl.querySelector(".d-eventname").textContent = "Error loading event";
        modalEl.querySelector(".d-eventlocation").textContent = "-";
        modalEl.querySelector(".d-eventprice").textContent = "-";
    }


    // Clean up when modal is hidden
    modalEl.addEventListener("hidden.bs.modal", () => {
        modalEl.remove();
    });
}

// Filter bookings based on search input and checkboxes
function filterBookings() {
    const searchInput = document.getElementById('searchInput').value.toLowerCase();
    const hideCancelled = document.getElementById('hideCancelledCheckbox').checked;
    const cards = document.querySelectorAll(".booking-card");
    const searchCount = document.getElementById('searchCount');

    let visibleCount = 0;

    // Loop through all booking cards
    cards.forEach(card => {
        const name = card.dataset.name || "";
        const email = card.dataset.email || "";
        const eventName = card.dataset.eventname || "";
        const eventLocation = card.dataset.eventlocation || "";
        const status = card.dataset.status || "";

        // Check if matches search - now includes event name and location
        const matchesSearch =
            name.includes(searchInput) ||
            email.includes(searchInput) ||
            eventName.includes(searchInput) ||
            eventLocation.includes(searchInput);

        // Check if should be hidden based on status - hide both CANCELLED and EVENTCANCELLED
        const matchesStatus = !(hideCancelled && (status === "cancelled" || status === "eventcancelled"));

        // Show or hide card
        const visible = matchesSearch && matchesStatus;
        card.style.display = visible ? "" : "none";

        if (visible) visibleCount++;
    });

    // Update search count badge
    if (searchInput || hideCancelled) {
        searchCount.textContent = `${visibleCount} booking${visibleCount !== 1 ? 's' : ''} found`;
        searchCount.style.display = 'inline';
    } else {
        searchCount.style.display = 'none';
    }

    // Show/hide "no results" message
    document.getElementById("noResults").style.display = visibleCount === 0 ? "block" : "none";
}

// Initialize page when DOM is ready
document.addEventListener("DOMContentLoaded", () => {
    loadBookings();
});