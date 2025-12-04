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
    document.getElementById("cancelDeadline").addEventListener("change", validateCancel);

    // Appointment date validation
    document.getElementById("apptStart").addEventListener("change", validateAppointmentDates);
    document.getElementById("apptEnd").addEventListener("change", validateAppointmentDates);

    // Single-day toggle
    document.getElementById("singleDayCheckbox").addEventListener("change", toggleEndDate);

    // Organizer-select handler
    document.getElementById("organizerSelect").addEventListener("change", handleOrganizerSelection);
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
        "name", "description", "startDate", "endDate", "cancelDeadline",
        "street", "houseNumber", "city", "postalCode",
        "state", "country", "price", "organizerSelect",
        "minParticipants", "maxParticipants", "category", "apptStart",
        "apptEnd", "apptSeasonal", "packageTitleInput", "packageDescInput",
        "packagePriceInput", "requirementInput", "equipmentNameInput",
        "equipmentRentableInput", "orgName", "orgEmail", "orgPhone",
        "depositPercent"
    ].forEach(id => {
        if (id === "depositPercent") {
            document.getElementById(id).value = 30;
        } else {
            document.getElementById(id).value = "";
        }
    });

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

    // Hide input rows
    hideRequirementInputs();
    hideEquipmentInputs();
    hidePackageInputs();
    hideAppointmentInputs();

    // Restart multi-step
    document.getElementById("event-form").classList.remove("was-validated");
    nextStep(false);

    // Minimum date today
    const today = new Date().toISOString().split("T")[0];
    document.getElementById("startDate").min = today;
    document.getElementById("endDate").min = today;
    document.getElementById("cancelDeadline").min = today;
}

// ===========================
// LOAD ORGANIZERS INTO SELECT
// ===========================
async function loadOrganizers() {
    const select = document.getElementById("organizerSelect");

    select.innerHTML = `<option value="">Select Organizer</option>
                        <option value="NEW">Add New Organizer</option>
    `;

    try {
        const res = await fetch("/api/organizers");
        const list = await res.json();

        list.forEach(o => {
            const opt = document.createElement("option");
            opt.value = o.id;
            opt.textContent = `${o.name} (${o.contactEmail})`;
            select.appendChild(opt);
        });
    } catch (err) {
        showToast("error", "Could not load organizers");
    }
}

// ===========================
// NEXT / BACK BUTTON LOGIC
// ===========================
function nextStep(disable) {
    const form = document.getElementById("event-form");
    const fileInput = document.getElementById("images");

    // Buttons / UI controls
    const nextBtn = document.getElementById("nextBtn");
    const backBtn = document.getElementById("backBtn");
    const submitBtn = document.getElementById("submitBtn");

    // Inputs without file-input
    const textInputs = Array.from(form.querySelectorAll("input, textarea"))
        .filter(el => el.type !== "file");

    // Selects + Checkboxes
    const selectAndChecks = form.querySelectorAll("select, input[type='checkbox'], input[type='radio']");

    // Add-buttons
    const addButtons = form.querySelectorAll(".add-requirement-btn, .add-equipment-btn, .add-package-btn, .add-appointment-btn");

    // Remove buttons inside dynamic containers (die kleinen X)
    const removeButtons = form.querySelectorAll("#requirementsContainer .btn-close, #equipmentContainer .btn-close, #packagesContainer .btn-close, #appointmentsContainer .btn-close");

    // Ensure form is positioned for overlay
    if (getComputedStyle(form).position === "static") {
        form.style.position = "relative";
    }

    if (disable) {
        form.classList.add("readonly");

        //    Input/Textarea
        textInputs.forEach(el => {
            el.setAttribute("readonly", "true");
            el.style.backgroundColor = "#e9ecef";
        });

        //    Selects + checkboxes
        selectAndChecks.forEach(el => {
            el.style.pointerEvents = "none";
            el.setAttribute("aria-disabled", "true");
            el.style.backgroundColor = "#e9ecef";
        });

        //    File input
        if (fileInput) {
            fileInput.classList.add("readonly-file");
            fileInput.style.pointerEvents = "none";
            fileInput.style.cursor = "not-allowed";
        }

        //    Hide add buttons and remove-buttons
        addButtons.forEach(btn => btn.style.display = "none");
        removeButtons.forEach(btn => btn.style.display = "none");

        document.getElementById("requirementInputs").style.display = "none";
        document.getElementById("equipmentInputs").style.display = "none";
        document.getElementById("packageInputs").style.display = "none";
        document.getElementById("appointmentInputs").style.display = "none";

        //    Add an invisible overlay that blocks clicks over the form content,
        //    but leaves the bottom area (buttons) accessible.
        //    We leave 80px at the bottom for Back/Submit area (adjust if needed).
        const existingOverlay = document.getElementById("formOverlay");
        if (!existingOverlay) {
            const overlay = document.createElement("div");
            overlay.id = "formOverlay";
            Object.assign(overlay.style, {
                position: "absolute",
                top: "0",
                left: "0",
                right: "0",
                bottom: "80px",            // leave space at bottom for Back/Submit
                background: "transparent",
                zIndex: "999",             // above inputs, below modal header/footer
            });
            form.appendChild(overlay);
        }

        // 7) Toggle navigation buttons
        if (nextBtn) nextBtn.style.display = "none";
        if (backBtn) backBtn.style.display = "inline-block";
        if (submitBtn) submitBtn.style.display = "inline-block";

    } else {
        // ---- Re-enable / Undo readonly ----
        form.classList.remove("readonly");

        // remove readonly on inputs & restore visuals
        textInputs.forEach(el => {
            el.removeAttribute("readonly");
            el.style.backgroundColor = "";
        });

        // restore selects / checkboxes interaction
        selectAndChecks.forEach(el => {
            el.style.pointerEvents = "";
            el.removeAttribute("aria-disabled");
            el.style.backgroundColor = "";
        });

        // file input
        if (fileInput) {
            fileInput.classList.remove("readonly-file");
            fileInput.style.pointerEvents = "";
            fileInput.style.cursor = "";
        }

        // show add/remove buttons again
        addButtons.forEach(btn => btn.style.display = "inline-block");
        removeButtons.forEach(btn => btn.style.display = "inline-block");

        // remove overlay if present
        const existingOverlay = document.getElementById("formOverlay");
        if (existingOverlay) existingOverlay.remove();

        form.querySelectorAll(".is-invalid").forEach(el => el.classList.remove("is-invalid"));

        // restore buttons visibility
        if (nextBtn) nextBtn.style.display = "inline-block";
        if (backBtn) backBtn.style.display = "none";
        if (submitBtn) submitBtn.style.display = "none";
    }
}

// check email validation
function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// For NEXT button
function checkValidation(next) {
    const form = document.getElementById("event-form");

    // --- Organizer validation BEFORE nextStep ---
    if (next === true) {
        const organizerSelect = document.getElementById("organizerSelect");
        const newOrgVisible = organizerSelect.value === "NEW";

        if (newOrgVisible) {
            let valid = true;

            const name = document.getElementById("orgName");
            const email = document.getElementById("orgEmail");

            if (!name.value.trim()) {
                name.classList.add("is-invalid");
                valid = false;
            } else name.classList.remove("is-invalid");

            if (!email.value.trim() || !isValidEmail(email.value.trim())) {
                email.classList.add("is-invalid");
                valid = false;
            } else email.classList.remove("is-invalid");

            if (!valid) return;

        }
    }

    // ---Normal HTML validation ---
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
    if (isNaN(min) || isNaN(max)) {
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
// VALIDATE CANCELLATION DEADLINE
// ===========================
function validateCancel() {
    const start = document.getElementById("startDate");
    const cancel = document.getElementById("cancelDeadline");

    if (cancel.value > start.value) {
        cancel.setCustomValidity("Cancellation Deadline cannot be later than start date");
        return false;
    }
    cancel.setCustomValidity("");
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

function isVisible(el) {
    return !!(el && (el.offsetParent !== null)); //style.display = "none" could be blocked by css
}

// ===========================
// SHOW / REMOVE / SAVE REQUIREMENTS
// ===========================
function showRequirementInputs() {
    document.getElementById("requirementInputs").style.display = "flex";
    document.querySelector(".add-requirement-btn").style.display = "none";
}

function hideRequirementInputs() {
    document.getElementById("requirementInput").value = "";
    document.getElementById("requirementInputs").style.display = "none";
    document.querySelector(".add-requirement-btn").style.display = "inline-block";
}

function cancelRequirement() {
    hideRequirementInputs();

    // remove validation when cancel
    const input = document.getElementById("requirementInput");
    if (input.classList.contains("is-invalid")) {
        input.classList.remove("is-invalid");
    }

}

function saveRequirement() {
    const input = document.getElementById("requirementInput");
    const value = input.value.trim();

    if (!isVisible(document.getElementById("requirementInputs"))) return;

    if (!value) {
        input.classList.add("is-invalid");
        return;
    }

    input.classList.remove("is-invalid");

    const id = Date.now();
    requirements.push({ id, description: value });

    document.getElementById("requirementsContainer")
        .insertAdjacentHTML("beforeend",
            badgeTemplate(id, value, "removeRequirement")
        );

    cancelRequirement();
}

function removeRequirement(id) {
    requirements = requirements.filter(r => r.id !== id);
    const el = document.getElementById("req-" + id);
    if (el) el.remove();
}


// ===========================
// SHOW / REMOVE / SAVE EQUIPMENT
// ===========================
function showEquipmentInputs() {
    document.getElementById("equipmentInputs").style.display = "flex";
    document.querySelector(".add-equipment-btn").style.display = "none";
}

function hideEquipmentInputs() {
    document.getElementById("equipmentNameInput").value = "";
    document.getElementById("equipmentRentableInput").value = "";
    document.getElementById("equipmentInputs").style.display = "none";
    document.querySelector(".add-equipment-btn").style.display = "inline-block";
}

function cancelEquipment() {

    hideEquipmentInputs();
    // remove validation when cancel
    const name = document.getElementById("equipmentNameInput");
    const rentable = document.getElementById("equipmentRentableInput");

    if (name.classList.contains("is-invalid")) {
        name.classList.remove("is-invalid");
    }
    if (rentable.classList.contains("is-invalid")) {
        rentable.classList.remove("is-invalid");
    }
}

function saveEquipment() {
    const name = document.getElementById("equipmentNameInput").value.trim();
    const rentable = document.getElementById("equipmentRentableInput").value;

    if (!isVisible(document.getElementById("equipmentInputs"))) return;

    let valid = true;

    if (!name) {
        document.getElementById("equipmentNameInput").classList.add("is-invalid");
        valid = false;
    } else {
        document.getElementById("equipmentNameInput").classList.remove("is-invalid");
    }

    if (!rentable) {
        document.getElementById("equipmentRentableInput").classList.add("is-invalid");
        valid = false;
    } else {
        document.getElementById("equipmentRentableInput").classList.remove("is-invalid");
    }

    if (!valid) return;

    const id = Date.now();
    equipment.push({ id, name, rentable: rentable === "true" });

    document.getElementById("equipmentContainer")
        .insertAdjacentHTML("beforeend",
            badgeTemplate(id, (rentable === "true" ? "Rentable: " : "Required: ") + name, "removeEquipment")
        );

    cancelEquipment();
}

function removeEquipment(id) {
    equipment = equipment.filter(e => e.id !== id);
    const el = document.getElementById("eq-" + id);
    if (el) el.remove();
}


// ===========================
// SHOW / REMOVE / SAVE PACKAGES
// ===========================
function showPackageInputs() {
    document.getElementById("packageInputs").style.display = "flex";
    document.querySelector(".add-package-btn").style.display = "none";
}

function hidePackageInputs() {
    document.getElementById("packageTitleInput").value = "";
    document.getElementById("packageDescInput").value = "";
    document.getElementById("packagePriceInput").value = "";
    document.getElementById("packageInputs").style.display = "none";
    document.querySelector(".add-package-btn").style.display = "inline-block";
}

function cancelPackage() {

    hidePackageInputs();

    // remove validation when cancel
    const title = document.getElementById("packageTitleInput");
    const desc = document.getElementById("packageDescInput");
    const price = document.getElementById("packagePriceInput");

    if (title.classList.contains("is-invalid")) {
        title.classList.remove("is-invalid");
    }
    if (desc.classList.contains("is-invalid")) {
        desc.classList.remove("is-invalid");
    }
    if (price.classList.contains("is-invalid")) {
        price.classList.remove("is-invalid");
    }
}

function savePackage() {
    const title = document.getElementById("packageTitleInput").value.trim();
    const desc = document.getElementById("packageDescInput").value.trim();
    const price = parseFloat(document.getElementById("packagePriceInput").value);

    if (!isVisible(document.getElementById("packageInputs"))) return;

    let valid = true;

    if (!title) {
        document.getElementById("packageTitleInput").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("packageTitleInput").classList.remove("is-invalid");

    if (!desc) {
        document.getElementById("packageDescInput").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("packageDescInput").classList.remove("is-invalid");

    if (!price || isNaN(price) || price < 0) {
        document.getElementById("packagePriceInput").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("packagePriceInput").classList.remove("is-invalid");

    if (!valid) return;

    const id = Date.now();
    packagesList.push({ id, title, description: desc, price });

    document.getElementById("packagesContainer")
        .insertAdjacentHTML("beforeend",
            badgeTemplate(id, `${title} (€${price})`, "removePackage")
        );

    cancelPackage();
}

function removePackage(id) {
    packagesList = packagesList.filter(p => p.id !== id);
    const el = document.getElementById("pack-" + id);
    if (el) el.remove();
}

// ===========================
// SHOW / REMOVE / SAVE APPOINTMENTS
// ===========================
function showAppointmentInputs() {
    document.getElementById("appointmentInputs").style.display = "flex";
    document.querySelector(".add-appointment-btn").style.display = "none";
}

function hideAppointmentInputs() {
    document.getElementById("apptStart").value = "";
    document.getElementById("apptEnd").value = "";
    document.getElementById("apptSeasonal").checked = false;
    document.getElementById("appointmentInputs").style.display = "none";
    document.querySelector(".add-appointment-btn").style.display = "inline-block";
}
function cancelAppointment() {
    hideAppointmentInputs();

    // remove validation when cancel
    const start = document.getElementById("apptStart");
    const end = document.getElementById("apptEnd");

    if (start.classList.contains("is-invalid")) {
        start.classList.remove("is-invalid");
    }
    if (end.classList.contains("is-invalid")) {
        end.classList.remove("is-invalid");
    }
}

function saveAppointment() {
    const start = document.getElementById("apptStart").value;
    const end = document.getElementById("apptEnd").value;
    const seasonal = document.getElementById("apptSeasonal").checked;

    if (!isVisible(document.getElementById("appointmentInputs"))) return;

    let valid = true;

    if (!start) {
        document.getElementById("apptStart").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("apptStart").classList.remove("is-invalid");

    if (!end) {
        document.getElementById("apptEnd").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("apptEnd").classList.remove("is-invalid");

    if (start && end && end < start) {
        document.getElementById("apptEnd").classList.add("is-invalid");
        valid = false;
    }

    if (!valid) return;

    const id = Date.now();
    appointments.push({ id, startDate: start, endDate: end, seasonal });

    document.getElementById("appointmentsContainer")
        .insertAdjacentHTML("beforeend",
            badgeTemplate(id, `${start} → ${end} (${seasonal ? "Seasonal" : "Fixed"})`, "removeAppointment")
        );

    cancelAppointment();
}

function removeAppointment(id) {
    appointments = appointments.filter(a => a.id !== id);
    document.getElementById("appt-" + id)?.remove();
}

// ===========================
// Selection handler for adding new Organizer
// ===========================
function handleOrganizerSelection() {
    const selectValue = document.getElementById("organizerSelect").value;
    const newOrgBox = document.getElementById("newOrganizerFields");

    if (selectValue === "NEW") {
        // Show inputs
        newOrgBox.style.display = "block";

        // Clear previous text
        document.getElementById("orgName").value = "";
        document.getElementById("orgEmail").value = "";
        document.getElementById("orgPhone").value = "";

    } else {
        // Hide inputs
        newOrgBox.style.display = "none";
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
// CREATE ORGANIZER
// ===========================
async function createOrganizer() {
    try {
        const Orgdto = {
            name : document.getElementById("orgName").value,
            email : document.getElementById("orgEmail").value,
            phone : document.getElementById("orgPhone").value
        }

        const res = await fetch("/api/organizers/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(Orgdto)
        });

        if (res.ok) {
            const created = await res.json();

            // put new organizer into select
            const select = document.getElementById("organizerSelect");
            const option = document.createElement("option");
            option.value = created.id;
            option.textContent = `${created.name} (${created.contactEmail})`;

            select.appendChild(option);
            select.value = created.id;

            showToast("success", "Organizer created!");

            return created.id;
        } else {
            showToast("error", "Failed to create organizer");
            return null;
        }
    }catch (e) {
        showToast("error", "Network error: " + err.message);
        return null;
    }

}
// ===========================
// SUBMIT EVENT (JSON + multipart)
// ===========================
async function submitEvent() {
    try {
        let organizerId = null;

        // ➤ If "Add New Organizer" selected → create first
        if (document.getElementById("organizerSelect").value === "NEW") {

            // Validate before creating
            const name = document.getElementById("orgName").value.trim();
            const email = document.getElementById("orgEmail").value.trim();

            if (!name || !email) {
                showToast("error", "Please provide organizer name and valid email");
                return;
            }

            // --- organizer erstellen ---
            organizerId = await createOrganizer();
            if (!organizerId) {
                showToast("error", "Organizer could not be created");
                return;
            }
        }
        else {
            organizerId = parseInt(document.getElementById("organizerSelect").value);
        }

        const dpVal = parseInt(document.getElementById("depositPercent").value, 10);
        // Build JSON DTO
        const dto = {
            name: document.getElementById("name").value,
            description: document.getElementById("description").value,
            startDate: document.getElementById("startDate").value,
            endDate: document.getElementById("endDate").value,
            cancelDeadline: document.getElementById("cancelDeadline").value,
            price: parseFloat(document.getElementById("price").value),
            depositPercent:isNaN(dpVal) ? 30 : dpVal,
            category: document.getElementById("category").value,
            organizerId: organizerId, // ALWAYS VALID ID

            newOrganizer: null, // REMOVED FROM BACKEND

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
