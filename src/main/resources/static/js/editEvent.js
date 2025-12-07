// ===========================
// STATE
// ===========================
let edit_requirements = [];
let edit_equipment = [];
let edit_packages = [];
let edit_appointments = [];

let edit_imagesToDelete = [];
let edit_existingImageIds = [];

let editModal = null;

// ===========================
// ON PAGE LOAD
// ===========================
document.addEventListener("DOMContentLoaded", () => {
    editModal = document.getElementById("editEventModal");
    const form = document.getElementById("event-form");

    // When edit modal opens → load data
    editModal.addEventListener("show.bs.modal", () => {
        edit_resetForm();
        edit_loadOrganizers();
        edit_loadForm(editCurrentEvent);      // <── uses global from openEditModal
        edit_toggleEndDate();
    });

    // Submit
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        await edit_submitEvent();
    });

    // Validation identical to create
    document.getElementById("edit_minParticipants").addEventListener("input", edit_validateParticipants);
    document.getElementById("edit_maxParticipants").addEventListener("input", edit_validateParticipants);

    document.getElementById("edit_startDate").addEventListener("change", edit_validateDates);
    document.getElementById("edit_endDate").addEventListener("change", edit_validateDates);
    document.getElementById("edit_cancelDeadline").addEventListener("change", edit_validateCancel);

    document.getElementById("edit_apptStart").addEventListener("change", edit_validateAppointmentDates);
    document.getElementById("edit_apptEnd").addEventListener("change", edit_validateAppointmentDates);

    document.getElementById("edit_singleDayCheckbox").addEventListener("change", edit_toggleEndDate);

    document.getElementById("edit_organizerSelect").addEventListener("change", edit_handleOrganizerSelection);
});

// ===========================
// END DATE TOGGLE
// ===========================
function edit_toggleEndDate() {
    const wrapper = document.getElementById("edit_endDateWrapper");
    const end = document.getElementById("edit_endDate");
    const checked = document.getElementById("edit_singleDayCheckbox").checked;

    if (checked) {
        wrapper.style.display = "none";
        end.value = "";
        end.removeAttribute("required");
    } else {
        wrapper.style.display = "block";
        end.setAttribute("required", "true");
    }
}

// ===========================
// RESET FORM
// ===========================
function edit_resetForm() {

    [
        "edit_name", "edit_description", "edit_startDate", "edit_endDate", "edit_cancelDeadline",
        "edit_street", "edit_houseNumber", "edit_city", "edit_postalCode",
        "edit_state", "edit_country", "edit_price", "edit_organizerSelect",
        "edit_minParticipants", "edit_maxParticipants", "edit_category",
        "edit_apptStart", "edit_apptEnd", "edit_packageTitleInput",
        "edit_packageDescInput", "edit_packagePriceInput",
        "edit_requirementInput", "edit_equipmentNameInput",
        "edit_equipmentRentableInput", "edit_orgName", "edit_orgEmail",
        "edit_orgPhone"
    ].forEach(id => document.getElementById(id).value = "");

    document.getElementById("edit_depositPercent").value = 30;

    // Reset single-day checkbox state
    document.getElementById("edit_singleDayCheckbox").checked = false;
    document.getElementById("edit_endDateWrapper").style.display = "block";
    document.getElementById("edit_endDate").setAttribute("required", "");


    // Reset images
    document.getElementById("edit_images").value = "";
    document.getElementById("edit_fileNames").innerHTML = "";
    document.getElementById("edit_existingImages").innerHTML = "";

    // Reset arrays
    edit_requirements = [];
    edit_equipment = [];
    edit_packages = [];
    edit_appointments = [];
    edit_imagesToDelete = [];
    edit_existingImageIds = [];

    // Reset containers
    document.getElementById("edit_requirementsContainer").innerHTML = "";
    document.getElementById("edit_equipmentContainer").innerHTML = "";
    document.getElementById("edit_packagesContainer").innerHTML = "";
    document.getElementById("edit_appointmentsContainer").innerHTML = "";

    // Hide input rows
    edit_hideRequirementInputs();
    edit_hideEquipmentInputs();
    edit_hidePackageInputs();
    edit_hideAppointmentInputs();

    // Multi-step reset
    edit_nextStep(false);

    document.getElementById("event-form").classList.remove("was-validated");
}

// ===========================
// LOAD FORM WITH EVENT DATA
// ===========================
function edit_loadForm(ev) {

    // simple fields
    document.getElementById("edit_name").value = ev.name;
    document.getElementById("edit_description").value = ev.description;
    document.getElementById("edit_startDate").value = ev.startDate;
    document.getElementById("edit_endDate").value = ev.endDate;
    // Auto-enable single-day mode if no endDate exists
    if (!ev.endDate || ev.endDate.trim() === "") {
        document.getElementById("edit_singleDayCheckbox").checked = true;
        document.getElementById("edit_endDateWrapper").style.display = "none";
        document.getElementById("edit_endDate").removeAttribute("required");
    } else {
        document.getElementById("edit_singleDayCheckbox").checked = false;
        document.getElementById("edit_endDateWrapper").style.display = "block";
        document.getElementById("edit_endDate").setAttribute("required", "");
    }

    document.getElementById("edit_cancelDeadline").value = ev.cancelDeadline;

    document.getElementById("edit_price").value = ev.price;
    document.getElementById("edit_category").value = ev.category;
    document.getElementById("edit_depositPercent").value = ev.depositPercent ?? 30;

    // location
    document.getElementById("edit_street").value = ev.location.street;
    document.getElementById("edit_houseNumber").value = ev.location.houseNumber;
    document.getElementById("edit_city").value = ev.location.city;
    document.getElementById("edit_postalCode").value = ev.location.postalCode;
    document.getElementById("edit_state").value = ev.location.state;
    document.getElementById("edit_country").value = ev.location.country;

    // participants
    document.getElementById("edit_minParticipants").value = ev.minParticipants;
    document.getElementById("edit_maxParticipants").value = ev.maxParticipants;

    // copy arrays
    edit_requirements = ev.requirements.map(r => ({ id: Number.isInteger(r.id) ? r.id : null, description: r.description }));
    edit_equipment = ev.equipment.map(e => ({ id: Number.isInteger(e.id) ? e.id : null, name: e.name, rentable: e.rentable }));
    edit_packages = ev.additionalPackages.map(p => ({
        id: Number.isInteger(p.id) ? p.id : null, title: p.title, description: p.description, price: p.price
    }));
    edit_appointments = ev.appointments.map(a => ({
        id: Number.isInteger(a.id) ? a.id : null, startDate: a.startDate, endDate: a.endDate, seasonal: a.seasonal
    }));

    edit_renderBadges();

    // existing images
    edit_existingImageIds = ev.imageIds ? [...ev.imageIds] : [];
    edit_renderExistingImages();
}

// ===========================
// RENDER EXISTING IMAGES
// ===========================
function edit_renderExistingImages() {

    const container = document.getElementById("edit_existingImages");
    container.innerHTML = "";

    edit_existingImageIds.forEach(id => {
        const wrap = document.createElement("div");
        wrap.className = "position-relative";
        wrap.style.width = "120px";
        wrap.style.height = "120px";

        const img = document.createElement("img");
        img.src = `/api/events/image/${id}`;
        img.className = "img-thumbnail";
        img.style.width = "120px";
        img.style.height = "120px";
        img.style.objectFit = "cover";

        const del = document.createElement("button");
        del.className = "btn btn-danger btn-sm position-absolute top-0 end-0";
        del.textContent = "X";
        del.onclick = () => {
            edit_imagesToDelete.push(id);
            wrap.remove();
            updateImageRequirement(document.getElementById("edit_images"), container);
        };

        wrap.appendChild(img);
        wrap.appendChild(del);
        container.appendChild(wrap);
    });

    updateImageRequirement(document.getElementById("edit_images"), container);
}

// ===========================
// BADGES RENDER
// ===========================
function edit_renderBadges() {
    const reqC = document.getElementById("edit_requirementsContainer");
    reqC.innerHTML = "";
    edit_requirements.forEach(r => reqC.insertAdjacentHTML("beforeend",
        edit_badge(r.id, r.description, "edit_removeRequirement", "edit_req")
    ));

    const eqC = document.getElementById("edit_equipmentContainer");
    eqC.innerHTML = "";
    edit_equipment.forEach(e => eqC.insertAdjacentHTML("beforeend",
        edit_badge(e.id, (e.rentable ? "Rentable: " : "Required: ") + e.name, "edit_removeEquipment", "edit_eq")
    ));

    const packC = document.getElementById("edit_packagesContainer");
    packC.innerHTML = "";
    edit_packages.forEach(p => packC.insertAdjacentHTML("beforeend",
        edit_badge(p.id, `${p.title} (€${p.price})`, "edit_removePackage", "edit_pack")
    ));

    const apC = document.getElementById("edit_appointmentsContainer");
    apC.innerHTML = "";
    edit_appointments.forEach(a => apC.insertAdjacentHTML("beforeend",
        edit_badge(a.id, `${a.startDate} → ${a.endDate} (${a.seasonal ? "Seasonal" : "Fixed"})`, "edit_removeAppointment", "edit_appt")
    ));
}

function edit_badge(id, text, removeFn, prefix) {
    return `
        <span class="badge bg-primary me-1 mb-1" id="${prefix}-${id}">
            ${text}
            <button type="button" class="btn-close btn-close-white ms-2"
                onclick="${removeFn}(${id})"></button>
        </span>`;
}

// ===========================
// ORGANIZER LOAD
// ===========================
async function edit_loadOrganizers() {
    const select = document.getElementById("edit_organizerSelect");

    select.innerHTML = `<option value="">Select Organizer</option>
                        <option value="NEW">Add New Organizer</option>`;

    const res = await fetch("/api/organizers");
    const list = await res.json();

    list.forEach(o => {
        const opt = document.createElement("option");
        opt.value = o.id;
        opt.textContent = `${o.name} (${o.contactEmail})`;
        select.appendChild(opt);
    });

    // set current organizer
    if (editCurrentEvent.organizerId)
        select.value = editCurrentEvent.organizerId;
}

// ===========================
// HANDLE NEW ORGANIZER
// ===========================
function edit_handleOrganizerSelection() {
    const val = document.getElementById("edit_organizerSelect").value;
    document.getElementById("edit_newOrganizerFields").style.display =
        val === "NEW" ? "block" : "none";
}

// ===========================
// VALIDATION copied from create
// ===========================
function edit_validateParticipants() {
    const min = +document.getElementById("edit_minParticipants").value;
    const max = +document.getElementById("edit_maxParticipants").value;

    if (!min || !max) return;

    const maxField = document.getElementById("edit_maxParticipants");

    if (max < min) maxField.setCustomValidity("Max < min");
    else maxField.setCustomValidity("");
}

function edit_validateCancel() {
    const start = document.getElementById("edit_startDate");
    const cancel = document.getElementById("edit_cancelDeadline");

    if (cancel.value > start.value) {
        cancel.setCustomValidity("Cancellation Deadline cannot be later than start date");
        return false;
    }
    cancel.setCustomValidity("");
    return true;
}

function edit_validateDates() {
    const start = document.getElementById("edit_startDate").value;
    const end = document.getElementById("edit_endDate").value;
    const single = document.getElementById("edit_singleDayCheckbox").checked;

    const endEl = document.getElementById("edit_endDate");

    if (single || !end) {
        endEl.setCustomValidity("");
        return;
    }

    if (end < start) endEl.setCustomValidity("End < start");
    else endEl.setCustomValidity("");
}

function edit_validateAppointmentDates() {
    const s = document.getElementById("edit_apptStart").value;
    const e = document.getElementById("edit_apptEnd").value;
    const el = document.getElementById("edit_apptEnd");

    if (!s || !e) {
        el.setCustomValidity("");
        return;
    }

    if (e < s) el.setCustomValidity("End < start");
    else el.setCustomValidity("");
}

// ===========================
// MULTI-STEP NEXT/BACK
// identical to create-event.js but with edit IDs
// ===========================
function edit_nextStep(disable) {

    const form = document.getElementById("event-form");

    const next = document.getElementById("edit_nextBtn");
    const back = document.getElementById("edit_backBtn");
    const submit = document.getElementById("edit_submitBtn");

    const inputs = Array.from(form.querySelectorAll("input, textarea")).filter(i => i.type !== "file");

    const selects = form.querySelectorAll("select, input[type='checkbox'], input[type='radio']");
    const addButtons = form.querySelectorAll(".add-requirement-btn, .add-equipment-btn, .add-appointment-btn, .add-package-btn");
    const removeButtons = form.querySelectorAll(".btn-close");

    if (disable) {
        // readonly mode
        form.classList.add("readonly");
        inputs.forEach(i => {
            i.setAttribute("readonly", "");
            i.style.background = "#e9ecef";
        });
        selects.forEach(s => s.style.pointerEvents = "none");

        addButtons.forEach(b => b.style.display = "none");
        removeButtons.forEach(b => b.style.display = "none");

        document.getElementById("edit_requirementInputs").style.display = "none";
        document.getElementById("edit_equipmentInputs").style.display = "none";
        document.getElementById("edit_packageInputs").style.display = "none";
        document.getElementById("edit_appointmentInputs").style.display = "none";

        next.style.display = "none";
        back.style.display = "inline-block";
        submit.style.display = "inline-block";
    } else {
        // normal mode
        form.classList.remove("readonly");
        inputs.forEach(i => {
            i.removeAttribute("readonly");
            i.style.background = "";
        });
        selects.forEach(s => s.style.pointerEvents = "");

        addButtons.forEach(b => b.style.display = "inline-block");
        removeButtons.forEach(b => b.style.display = "inline-block");

        next.style.display = "inline-block";
        back.style.display = "none";
        submit.style.display = "none";
    }
}

// wrapper for button
function edit_checkValidation(next) {
    const form = document.getElementById("event-form");

    if (form.checkValidity()) edit_nextStep(next);
    else form.classList.add("was-validated");
}

function isVisible(el) {
    return !!(el && (el.offsetParent !== null)); //style.display = "none" could be blocked by css
}

// ===========================
// ADD / REMOVE ITEMS (Requirement/Equipment/Package/Appointment)
// identical logic as homepage.js
// ===========================
function edit_showRequirementInputs() {
    document.getElementById("edit_requirementInputs").style.display = "flex";
    document.querySelector(".add-requirement-btn").style.display = "none";
}


function edit_hideRequirementInputs() {
    document.getElementById("edit_requirementInput").value = "";
    document.getElementById("edit_requirementInputs").style.display = "none";
    document.querySelector(".add-requirement-btn").style.display = "inline-block";
}

function edit_cancelRequirement() {
    edit_hideRequirementInputs();

    // remove validation when cancel
    const input = document.getElementById("edit_requirementInput");
    if (input.classList.contains("is-invalid")) {
        input.classList.remove("is-invalid");
    }
}

function edit_saveRequirement() {
    const input = document.getElementById("edit_requirementInput");
    const val = input.value.trim();

    if (!isVisible(document.getElementById("edit_requirementInputs"))) return;

    if (!val) {
        input.classList.add("is-invalid");
        return;
    }

    input.classList.remove("is-invalid")

    const id = Date.now();
    edit_requirements.push({ id, description: val });
    edit_renderBadges();
    edit_cancelRequirement();
}

function edit_removeRequirement(id) {
    edit_requirements = edit_requirements.filter(r => r.id !== id);
    document.getElementById(`edit_req-${id}`)?.remove();
}

// EQUIPMENT
function edit_showEquipmentInputs() {
    document.getElementById("edit_equipmentInputs").style.display = "flex";
    document.querySelector(".add-equipment-btn").style.display = "none";
}

function edit_hideEquipmentInputs() {
    document.getElementById("edit_equipmentNameInput").value = "";
    document.getElementById("edit_equipmentRentableInput").value = "";
    document.getElementById("edit_equipmentInputs").style.display = "none";
    document.querySelector(".add-equipment-btn").style.display = "inline-block";
}

function edit_cancelEquipment() {

    edit_hideEquipmentInputs();

    // remove validation when cancel
    const name = document.getElementById("edit_equipmentNameInput");
    const rentable = document.getElementById("edit_equipmentRentableInput");

    if (name.classList.contains("is-invalid")) {
        name.classList.remove("is-invalid");
    }
    if (rentable.classList.contains("is-invalid")) {
        rentable.classList.remove("is-invalid");
    }

}

function edit_saveEquipment() {
    const name = document.getElementById("edit_equipmentNameInput").value.trim();
    const rentable = document.getElementById("edit_equipmentRentableInput").value;

    if (!isVisible(document.getElementById("edit_equipmentInputs"))) return;

    let valid = true;

    if (!name) {
        document.getElementById("edit_equipmentNameInput").classList.add("is-invalid");
        valid = false;
    } else {
        document.getElementById("edit_equipmentNameInput").classList.remove("is-invalid");
    }

    if (!rentable) {
        document.getElementById("edit_equipmentRentableInput").classList.add("is-invalid");
        valid = false;
    } else {
        document.getElementById("edit_equipmentRentableInput").classList.remove("is-invalid");
    }

    if (!valid) return;

    const id = Date.now();
    edit_equipment.push({ id, name, rentable: rentable === "true" });
    edit_renderBadges();
    edit_cancelEquipment();
}

function edit_removeEquipment(id) {
    edit_equipment = edit_equipment.filter(e => e.id !== id);
    document.getElementById(`edit_eq-${id}`)?.remove();
}

// PACKAGE
function edit_showPackageInputs() {
    document.getElementById("edit_packageInputs").style.display = "flex";
    document.querySelector(".add-package-btn").style.display = "none";
}

function edit_hidePackageInputs() {
    document.getElementById("edit_packageTitleInput").value = "";
    document.getElementById("edit_packageDescInput").value = "";
    document.getElementById("edit_packagePriceInput").value = "";
    document.getElementById("edit_packageInputs").style.display = "none";
    document.querySelector(".add-package-btn").style.display = "inline-block";
}

function edit_cancelPackage() {

    edit_hidePackageInputs();

    // remove validation when cancel
    const title = document.getElementById("edit_packageTitleInput");
    const desc = document.getElementById("edit_packageDescInput");
    const price = document.getElementById("edit_packagePriceInput");

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

function edit_savePackage() {
    const title = document.getElementById("edit_packageTitleInput").value.trim();
    const desc = document.getElementById("edit_packageDescInput").value.trim();
    const price = parseFloat(document.getElementById("edit_packagePriceInput").value);

    if (!isVisible(document.getElementById("edit_packageInputs"))) return;

    let valid = true;

    if (!title) {
        document.getElementById("edit_packageTitleInput").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("edit_packageTitleInput").classList.remove("is-invalid");

    if (!desc) {
        document.getElementById("edit_packageDescInput").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("edit_packageDescInput").classList.remove("is-invalid");

    if (!price || isNaN(price) || price < 0) {
        document.getElementById("edit_packagePriceInput").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("edit_packagePriceInput").classList.remove("is-invalid");

    if (!valid) return;


    const id = Date.now();
    edit_packages.push({ id, title, description: desc, price });
    edit_renderBadges();
    edit_cancelPackage();
}

function edit_removePackage(id) {
    edit_packages = edit_packages.filter(p => p.id !== id);
    document.getElementById(`edit_pack-${id}`)?.remove();
}

// APPOINTMENT
function edit_showAppointmentInputs() {
    document.getElementById("edit_appointmentInputs").style.display = "flex";
    document.querySelector(".add-appointment-btn").style.display = "none";
}

function edit_hideAppointmentInputs() {
    document.getElementById("edit_apptStart").value = "";
    document.getElementById("edit_apptEnd").value = "";
    document.getElementById("edit_apptSeasonal").checked = false;
    document.getElementById("edit_appointmentInputs").style.display = "none";
    document.querySelector(".add-appointment-btn").style.display = "inline-block";
}

function edit_cancelAppointment() {

    edit_hideAppointmentInputs();

    // remove validation when cancel
    const start = document.getElementById("edit_apptStart");
    const end = document.getElementById("edit_apptEnd");

    if (start.classList.contains("is-invalid")) {
        start.classList.remove("is-invalid");
    }
    if (end.classList.contains("is-invalid")) {
        end.classList.remove("is-invalid");
    }

}

function edit_saveAppointment() {
    const start = document.getElementById("edit_apptStart").value;
    const end = document.getElementById("edit_apptEnd").value;
    const seasonal = document.getElementById("edit_apptSeasonal").checked;

    if (!isVisible(document.getElementById("edit_appointmentInputs"))) return;

    let valid = true;

    if (!start) {
        document.getElementById("edit_apptStart").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("edit_apptStart").classList.remove("is-invalid");

    if (!end) {
        document.getElementById("edit_apptEnd").classList.add("is-invalid");
        valid = false;
    } else document.getElementById("edit_apptEnd").classList.remove("is-invalid");

    if (start && end && end < start) {
        document.getElementById("edit_apptEnd").classList.add("is-invalid");
        valid = false;
    }

    if (!valid) return;

    const id = Date.now();
    edit_appointments.push({ id, startDate: start, endDate: end, seasonal });
    edit_renderBadges();
    edit_cancelAppointment();
}

function edit_removeAppointment(id) {
    edit_appointments = edit_appointments.filter(a => a.id !== id);
    document.getElementById(`edit_appt-${id}`)?.remove();
}

// ===========================
// SUBMIT EVENT UPDATE
// ===========================
async function edit_submitEvent() {

    let organizerId;
    const orgSelect = document.getElementById("edit_organizerSelect").value;

    if (orgSelect === "NEW") {
        // create first
        const name = document.getElementById("edit_orgName").value.trim();
        const email = document.getElementById("edit_orgEmail").value.trim();
        const phone = document.getElementById("edit_orgPhone").value.trim();

        const dto = { name, email, phone };

        const res = await fetch("/api/organizers/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            showToast("error", "Organizer could not be created");
            return;
        }
        const created = await res.json();
        organizerId = created.id;
    } else {
        organizerId = parseInt(orgSelect);
    }

    // build JSON
    const dpVal = parseInt(document.getElementById("edit_depositPercent").value, 10);

    const eventDto = {
        name: document.getElementById("edit_name").value,
        description: document.getElementById("edit_description").value,
        startDate: document.getElementById("edit_startDate").value,
        endDate: document.getElementById("edit_endDate").value,
        cancelDeadline: document.getElementById("edit_cancelDeadline").value,
        price: parseFloat(document.getElementById("edit_price").value),
        depositPercent: isNaN(dpVal) ? 30 : dpVal,
        category: document.getElementById("edit_category").value,
        organizerId: organizerId,
        minParticipants: parseInt(document.getElementById("edit_minParticipants").value),
        maxParticipants: parseInt(document.getElementById("edit_maxParticipants").value),

        location: {
            street: document.getElementById("edit_street").value,
            houseNumber: document.getElementById("edit_houseNumber").value,
            city: document.getElementById("edit_city").value,
            postalCode: document.getElementById("edit_postalCode").value,
            state: document.getElementById("edit_state").value,
            country: document.getElementById("edit_country").value
        },

        requirements: edit_requirements.map(r => ({
            id: Number.isInteger(r.id) ? r.id : null,
            description: r.description
        })),

        equipment: edit_equipment.map(e => ({
            id: Number.isInteger(e.id) ? e.id : null,
            name: e.name,
            rentable: e.rentable
        })),

        additionalPackages: edit_packages.map(p => ({
            id: Number.isInteger(p.id) ? p.id : null,
            title: p.title,
            description: p.description,
            price: p.price
        })),

        appointments: edit_appointments.map(a => ({
            id: Number.isInteger(a.id) ? a.id : null,
            startDate: a.startDate,
            endDate: a.endDate,
            seasonal: a.seasonal
        }))
    };

    // multipart
    const formData = new FormData();
    formData.append("event", new Blob([JSON.stringify(eventDto)], { type: "application/json" }));
    edit_imagesToDelete.forEach(id => {
        formData.append("deleteImageIds", id);
    });

    const files = document.getElementById("edit_images").files;
    for (const f of files) formData.append("images", f);

    const res = await fetch(`/api/events/edit/${editCurrentEvent.id}`, {
        method: "PUT",
        body: formData
    });

    if (!res.ok) {
        showToast("error", "Update failed");
        return;
    }

    showToast("success", "Event updated!");

    bootstrap.Modal.getInstance(editModal).hide();

    await loadEvents();
}

