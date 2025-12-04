// Load bookings from backend
async function loadBookings() {
    try {
        const userInfoRaw = localStorage.getItem("userInfo");
        const user = userInfoRaw ? JSON.parse(userInfoRaw) : null;
        // Staff roles (ADMIN, BACKOFFICE, FRONTOFFICE) see all bookings
        // Regular USER only sees their own bookings (filtered by email)
        const staffRoles = ["ADMIN", "BACKOFFICE", "FRONTOFFICE"];
        const isStaff = user && staffRoles.includes(user.role);

        let url;
        if (!user) {
            // Not logged in - redirect to login or show error
            showToast("error", "Please log in to view bookings");
            return;
        } else if (isStaff) {
            // Staff sees all bookings
            url = "/api/bookings";
        } else {
            // Regular user only sees their own bookings, matched by account rather than the email used during booking
            if (user.id) {
                url = `/api/bookings?userId=${encodeURIComponent(user.id)}`;
            } else {
                url = `/api/bookings?email=${encodeURIComponent(user.email)}`;
            }
        }

        const res = await fetch(url);
        if (res.status === 401 || res.status === 403) {
            console.warn("Session expired or invalid. Logging out.");
            localStorage.removeItem("userInfo");
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

// Open details modal and populate booking information
async function openDetailsModal(booking) {
    const detailsTemplate = document.getElementById("detailsModalTemplate");
    const modalContent = detailsTemplate.content.cloneNode(true);
    const modalEl = modalContent.querySelector(".modal");

    // Fill personal information (left column)
    modalEl.querySelector(".d-name").textContent = `${booking.firstname} ${booking.lastname}`;
    modalEl.querySelector(".d-birthdate").textContent = booking.birthDate || "-";
    modalEl.querySelector(".d-email").textContent = booking.email;
    modalEl.querySelector(".d-phone").textContent = booking.phoneNumber || "-";
    modalEl.querySelector(".d-address").textContent = formatAddress(booking.address);

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

    const cancelBtn = modalEl.querySelector(".btn-open-cancel");
    if (booking.status === "CANCELLED" || booking.status === "EXPIRED" || booking.status === "EVENTCANCELLED") {
        cancelBtn.style.display = "none";
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
            } else {
                modalEl.querySelector(".d-eventname").textContent = "Event not found";
                modalEl.querySelector(".d-eventlocation").textContent = "-";
            }
        }
    } catch (error) {
        console.error("Failed to load event details:", error);
        modalEl.querySelector(".d-eventname").textContent = "Error loading event";
        modalEl.querySelector(".d-eventlocation").textContent = "-";
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