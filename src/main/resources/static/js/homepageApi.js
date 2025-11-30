// ===========================
// GLOBAL STATE
// ===========================
let requirements = [];
let equipment = [];
let packagesList = [];
let appointments = [];

document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById('createEventModal');
    const form = document.getElementById('event-form');

    // Reset form when modal is opened
    modalEl.addEventListener('show.bs.modal', () => {
        resetForm();
        loadOrganizers();
    });

    // Form submission to backend
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        await submitEvent();
    });
    // min/max participant validation
    document.getElementById("minParticipants").addEventListener("input", validateParticipants);
    document.getElementById("maxParticipants").addEventListener("input", validateParticipants);

    // Event date validation
    document.getElementById("startDate").addEventListener("change", validateDates);
    document.getElementById("endDate").addEventListener("change", validateDates);

    // Appointment date validation
    document.getElementById("apptStart").addEventListener("change", validateAppointmentDates);
    document.getElementById("apptEnd").addEventListener("change", validateAppointmentDates);

    // Single-day toggle
    document.getElementById("singleDayCheckbox").addEventListener("change", toggleEndDate);
});

// ===========================
// CHECKBOX END DATE LOGIC
// ===========================
function toggleEndDate() {
    const wrapper = document.getElementById("endDateWrapper");
    const endDate = document.getElementById("endDate");
    const checked = document.getElementById("singleDayCheckbox").checked;

    if (checked) {
        wrapper.style.display = "none";
        endDate.value = "";
        endDate.removeAttribute("required");
    } else {
        wrapper.style.display = "block";
        endDate.setAttribute("required", "true");
    }
}

// ===========================
// RESET FORM (Modal open)
// ===========================
function resetForm() {

    // Reset normal fields
    [
        "name", "description", "startDate", "endDate",
        "street", "houseNumber", "city", "postalCode",
        "state", "country", "price", "organizerId",
        "minParticipants", "maxParticipants", "category"
    ].forEach(id => document.getElementById(id).value = "");

    document.getElementById("images").value = "";
    document.getElementById("fileNames").innerHTML = "";

    // Reset all dynamic collections
    requirements = [];
    equipment = [];
    packagesList = [];
    appointments = [];

    document.getElementById("requirementsContainer").innerHTML = "";
    document.getElementById("equipmentContainer").innerHTML = "";
    document.getElementById("packagesContainer").innerHTML = "";
    document.getElementById("appointmentsContainer").innerHTML = "";

    // Restart multi-step
    document.getElementById("event-form").classList.remove("was-validated");
    nextStep(false);

    // Minimum date today
    const today = new Date().toISOString().split("T")[0];
    document.getElementById("startDate").min = today;
    document.getElementById("endDate").min = today;
}

// ===========================
// LOAD ORGANIZERS INTO SELECT
// ===========================
async function loadOrganizers() {
    const select = document.getElementById("organizerSelect");
    select.innerHTML = `<option value="">Select Organizer</option>`;

    try {
        const res = await fetch("/api/organizers");
        const list = await res.json();

        list.forEach(o => {
            const opt = document.createElement("option");
            opt.value = o.id;
            opt.textContent = `${o.name} (${o.email})`;
            select.appendChild(opt);
        });
    } catch (err) {
        console.warn("Could not load organizers:", err);
    }
}
// ===========================
// ADD NEW ORGANIZER TOGGLE
// ===========================
function toggleNewOrganizer() {
    const box = document.getElementById("newOrganizerFields");
    box.style.display = box.style.display === "none" ? "block" : "none";
}
// ===========================
// NEXT / BACK BUTTON LOGIC
// ===========================
function nextStep(disable) {

    const form = document.getElementById("event-form");

    // Alle Inputs (nur lesen)
    const inputs = form.querySelectorAll("input, textarea, select");

    // Buttons für Adds/Removes
    const addButtons = form.querySelectorAll("button[onclick^='add'], button[onclick^='remove']");
    const removeButtons = form.querySelectorAll(".btn-close");

    // File input
    const fileInput = document.getElementById("images");

    if (disable) {
        // --- Lock all text inputs ---
        inputs.forEach(el => {
            el.setAttribute("readonly", "true");
            el.style.backgroundColor = "#e9ecef";
            el.style.cursor = "not-allowed";
        });

        // Disable selects correctly (select has no readonly)
        form.querySelectorAll("select").forEach(sel => {
            sel.setAttribute("disabled", "true");
        });

        // Hide all ADD buttons
        addButtons.forEach(btn => {
            btn.style.display = "none";
        });

        // Hide remove buttons (small X)
        removeButtons.forEach(btn => {
            btn.style.display = "none";
        });

        // Disable file upload
        fileInput.disabled = true;

        // Toggle visible buttons
        document.getElementById("nextBtn").style.display = "none";
        document.getElementById("backBtn").style.display = "inline-block";
        document.getElementById("submitBtn").style.display = "inline-block";

    } else {
        // Unlock text inputs
        inputs.forEach(el => {
            el.removeAttribute("readonly");
            el.style.backgroundColor = "white";
            el.style.cursor = "auto";
        });

        // Re-enable selects
        form.querySelectorAll("select").forEach(sel => {
            sel.removeAttribute("disabled");
        });

        // Show add buttons
        addButtons.forEach(btn => {
            btn.style.display = "inline-block";
        });

        // Show remove buttons
        removeButtons.forEach(btn => {
            btn.style.display = "inline-block";
        });

        // Enable file upload
        fileInput.disabled = false;

        // Restore visible buttons
        document.getElementById("nextBtn").style.display = "inline-block";
        document.getElementById("backBtn").style.display = "none";
        document.getElementById("submitBtn").style.display = "none";
    }
}

// For NEXT button
function checkValidation(next) {
    const form = document.getElementById("event-form");
    if (form.checkValidity()) {
        nextStep(next);
    } else {
        form.classList.add("was-validated");
    }
}
// ===========================
// VALIDATE MIN/MAX PARTICIPANTS
// ===========================
function validateParticipants() {
    const minField = document.getElementById("minParticipants");
    const maxField = document.getElementById("maxParticipants");

    const min = parseInt(minField.value);
    const max = parseInt(maxField.value);

    // If one field empty → reset errors
    if (!min || !max) {
        minField.setCustomValidity("");
        maxField.setCustomValidity("");
        return true;
    }

    if (max < min) {
        maxField.setCustomValidity("Max participants cannot be lower than min participants.");
        return false;
    }

    maxField.setCustomValidity("");
    return true;
}

// ===========================
// VALIDATE END DATES
// ===========================
function validateDates() {
    const start = document.getElementById("startDate");
    const end = document.getElementById("endDate");
    const single = document.getElementById("singleDayCheckbox").checked;

    if (single || !end.value) {
        end.setCustomValidity("");
        return true;
    }

    if (end.value < start.value) {
        end.setCustomValidity("End date cannot be earlier than start date");
        return false;
    }

    end.setCustomValidity("");
    return true;
}

// ===========================
// VALIDATE Appointments END DATES
// ===========================
function validateAppointmentDates() {
    const start = document.getElementById("apptStart");
    const end = document.getElementById("apptEnd");

    if (!start.value || !end.value) {
        end.setCustomValidity("");
        return;
    }

    if (end.value < start.value) {
        end.setCustomValidity("End date cannot be earlier than start date");
    } else {
        end.setCustomValidity("");
    }
}



// ===========================
// ADD / REMOVE REQUIREMENTS
// ===========================
function addRequirement() {
    const input = document.getElementById("requirementInput");
    const value = input.value.trim();
    if (!value) return;

    const id = Date.now();
    requirements.push({ id, description: value });

    const container = document.getElementById("requirementsContainer");
    container.insertAdjacentHTML("beforeend", badgeTemplate(id, value, "removeRequirement"));

    input.value = "";
}

function removeRequirement(id) {
    requirements = requirements.filter(r => r.id !== id);
    document.getElementById("req-" + id)?.remove();
}

// ===========================
// ADD / REMOVE EQUIPMENT
// ===========================
function addEquipment() {
    const name = document.getElementById("equipmentNameInput").value.trim();
    const rentable = document.getElementById("equipmentRentableInput").value === "true";

    if (!name) return;

    const id = Date.now();
    equipment.push({ id, name, rentable });

    const container = document.getElementById("equipmentContainer");
    container.insertAdjacentHTML("beforeend",
        badgeTemplate(id, (rentable ? "Rentable: " : "Required: ") + name, "removeEquipment")
    );

    document.getElementById("equipmentNameInput").value = "";
}

function removeEquipment(id) {
    equipment = equipment.filter(e => e.id !== id);
    document.getElementById("eq-" + id)?.remove();
}

// ===========================
// ADD / REMOVE PACKAGES
// ===========================
function addPackage() {
    const title = document.getElementById("packageTitleInput").value.trim();
    const desc = document.getElementById("packageDescInput").value.trim();
    const price = parseFloat(document.getElementById("packagePriceInput").value);

    if (!title || isNaN(price)) return;

    const id = Date.now();
    packagesList.push({ id, title, description: desc, price });

    document.getElementById("packagesContainer")
        .insertAdjacentHTML("beforeend",
            badgeTemplate(id, `${title} (€${price})`, "removePackage")
        );

    document.getElementById("packageTitleInput").value = "";
    document.getElementById("packageDescInput").value = "";
    document.getElementById("packagePriceInput").value = "";
}

function removePackage(id) {
    packagesList = packagesList.filter(p => p.id !== id);
    document.getElementById("pack-" + id)?.remove();
}

// ===========================
// ADD / REMOVE APPOINTMENTS
// ===========================
function addAppointment() {
    const start = document.getElementById("apptStart").value;
    const end = document.getElementById("apptEnd").value;
    const seasonal = document.getElementById("apptSeasonal").checked;

    if (!start || !end) return;

    const id = Date.now();
    appointments.push({ id, startDate: start, endDate: end, seasonal });

    const container = document.getElementById("appointmentsContainer");
    container.insertAdjacentHTML("beforeend",
        badgeTemplate(id, `${start} → ${end} (${seasonal ? "Seasonal" : "Fixed"})`, "removeAppointment")
    );

    document.getElementById("apptStart").value = "";
    document.getElementById("apptEnd").value = "";
    document.getElementById("apptSeasonal").checked = false;
}

function removeAppointment(id) {
    appointments = appointments.filter(a => a.id !== id);
    document.getElementById("appt-" + id)?.remove();
}

// ===========================
// Add new Organizer
// ===========================
function toggleNewOrganizer() {
    const box = document.getElementById("newOrganizerFields");
    box.style.display = box.style.display === "none" ? "block" : "none";

    if (box.style.display === "block") {
        document.getElementById("organizerSelect").value = "";
    }
}

// ===========================
// BADGE TEMPLATE
// ===========================
function badgeTemplate(id, text, removeFn) {
    return `
        <span class="badge bg-primary" id="${removeFn.includes("Requirement") ? "req" :
        removeFn.includes("Equipment") ? "eq" :
            removeFn.includes("Package") ? "pack" : "appt"}-${id}">
            ${text}
            <button type="button" class="btn-close btn-close-white ms-2" onclick="${removeFn}(${id})"></button>
        </span>
    `;
}

// ===========================
// FILE NAME PREVIEW
// ===========================
document.getElementById("images")?.addEventListener("change", () => {
    const files = Array.from(document.getElementById("images").files);
    const div = document.getElementById("fileNames");

    if (!files.length) {
        div.innerHTML = "No files selected";
        return;
    }

    div.innerHTML = `<ul>${files.map(f => `<li>${f.name}</li>`).join("")}</ul>`;
});

// ===========================
// SUBMIT EVENT (JSON + multipart)
// ===========================
async function submitEvent() {
    try {
        // Build JSON DTO
        const dto = {
            name: document.getElementById("name").value,
            description: document.getElementById("description").value,
            startDate: document.getElementById("startDate").value,
            endDate: document.getElementById("endDate").value,
            price: parseFloat(document.getElementById("price").value),
            category: document.getElementById("category").value,
            // Organizer (existing OR new)
            organizerId: document.getElementById("newOrganizerFields").style.display === "none"
                ? parseInt(document.getElementById("organizerSelect").value) || null
                : null,

            newOrganizer: document.getElementById("newOrganizerFields").style.display === "block"
                ? {
                    name: document.getElementById("orgName").value,
                    email: document.getElementById("orgEmail").value,
                    phone: document.getElementById("orgPhone").value
                }
                : null,

            minParticipants: parseInt(document.getElementById("minParticipants").value),
            maxParticipants: parseInt(document.getElementById("maxParticipants").value),

            location: {
                street: document.getElementById("street").value,
                houseNumber: document.getElementById("houseNumber").value,
                city: document.getElementById("city").value,
                postalCode: document.getElementById("postalCode").value,
                state: document.getElementById("state").value,
                country: document.getElementById("country").value
            },

            requirements: requirements.map(r => ({ description: r.description })),
            equipment: equipment.map(e => ({ name: e.name, rentable: e.rentable })),
            additionalPackages: packagesList.map(p => ({
                title: p.title,
                description: p.description,
                price: p.price
            })),
            appointments: appointments.map(a => ({
                startDate: a.startDate,
                endDate: a.endDate,
                seasonal: a.seasonal
            }))
        };

        // Build multipart/form-data
        const formData = new FormData();
        formData.append("event", new Blob([JSON.stringify(dto)], { type: "application/json" }));

        const imageFiles = document.getElementById("images").files;
        for (const f of imageFiles) {
            formData.append("images", f);
        }

        const res = await fetch("/api/events/create", {
            method: "POST",
            body: formData
        });

        if (res.ok) {
            const data = await res.json();
            showToast("success", `Event "${data.name}" created successfully!`);
            bootstrap.Modal.getInstance(document.getElementById("createEventModal")).hide();
            resetForm();
        } else {
            showToast("error", "Error: " + await res.text());
        }

    } catch (err) {
        showToast("error", "Network error: " + err.message);
    }
}
