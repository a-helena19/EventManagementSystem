(function () {
    // small escape helper to avoid html injection in badges
    function escapeHtml(s) {
        if (s === null || s === undefined) return "";
        return String(s).replace(/[&<>"']/g, function (m) {
            return ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' })[m];
        });
    }

    // openEditModal(ev, detailsModalEl) - main entrypoint used by openDetailsModal
    window.openEditModal = async function (ev, detailsModalEl = null) {
        if (!ev) { showToast("error", "Event not provided"); return; }

        const editModalTemplate = document.getElementById("editModalTemplate");
        const content = editModalTemplate.content.cloneNode(true);
        const modalEl = content.querySelector(".modal");
        const form = modalEl.querySelector(".edit-form");

        // hide original details modal if provided
        const detailsBsModal = detailsModalEl ? bootstrap.Modal.getInstance(detailsModalEl) : null;
        if (detailsBsModal) detailsBsModal.hide();

        // scoped selector
        const $ = sel => modalEl.querySelector(sel);

        // elements mapping (IDs from your template)
        const nameEl = $("#edit_name");
        const descriptionEl = $("#edit_description");
        const singleDayEl = $("#edit_singleDayCheckbox");
        const startDateEl = $("#edit_startDate");
        const endDateEl = $("#edit_endDate");
        const endDateWrapper = $("#edit_endDateWrapper");
        const priceEl = $("#edit_price");
        const categoryEl = $("#edit_category");
        const depositEl = $("#edit_depositPercent");

        const streetEl = $("#edit_street");
        const houseNumberEl = $("#edit_houseNumber");
        const cityEl = $("#edit_city");
        const postalCodeEl = $("#edit_postalCode");
        const stateEl = $("#edit_state");
        const countryEl = $("#edit_country");

        const appointmentsContainer = $("#edit_appointmentsContainer");
        const packagesContainer = $("#edit_packagesContainer");
        const requirementsContainer = $("#edit_requirementsContainer");
        const equipmentContainer = $("#edit_equipmentContainer");

        const minParticipantsEl = $("#edit_minParticipants");
        const maxParticipantsEl = $("#edit_maxParticipants");

        const existingImagesContainer = $("#edit_existingImages");
        const imagesInput = $("#edit_images");
        const fileNamesDiv = $("#edit_fileNames");

        const organizerSelect = $("#edit_organizerSelect");
        const newOrgBox = $("#edit_newOrganizerFields");
        const orgNameEl = $("#edit_orgName");
        const orgEmailEl = $("#edit_orgEmail");
        const orgPhoneEl = $("#edit_orgPhone");

        const nextBtn = $("#edit_nextBtn");
        const backBtn = $("#edit_backBtn");
        const submitBtn = $("#edit_submitBtn");

        // local mutable state
        let editRequirements = [];
        let editEquipment = [];
        let editPackages = [];
        let editAppointments = [];
        let imagesToDelete = [];
        let existingImageIds = [];

        const eventId = ev.id;

        // Populate simple fields
        nameEl.value = ev.name || "";
        descriptionEl.value = ev.description || "";
        startDateEl.value = ev.startDate || "";
        endDateEl.value = ev.endDate || "";
        priceEl.value = (ev.price != null) ? ev.price : "";
        categoryEl.value = ev.category || "";
        depositEl.value = (ev.depositPercent != null) ? ev.depositPercent : 30;

        streetEl.value = ev.location?.street || "";
        houseNumberEl.value = ev.location?.houseNumber || "";
        cityEl.value = ev.location?.city || "";
        postalCodeEl.value = ev.location?.postalCode || "";
        stateEl.value = ev.location?.state || "";
        countryEl.value = ev.location?.country || "";

        minParticipantsEl.value = ev.minParticipants != null ? ev.minParticipants : "";
        maxParticipantsEl.value = ev.maxParticipants != null ? ev.maxParticipants : "";

        // Seed collections preserving original IDs when available
        editRequirements = (ev.requirements || []).map(r => ({ id: r.id || (Date.now()+Math.random()), description: r.description }));
        editEquipment = (ev.equipment || []).map(e => ({ id: e.id || (Date.now()+Math.random()), name: e.name, rentable: !!e.rentable }));
        editPackages = (ev.additionalPackages || []).map(p => ({ id: p.id || (Date.now()+Math.random()), title: p.title, description: p.description, price: p.price }));
        editAppointments = (ev.appointments || []).map(a => ({ id: a.id || (Date.now()+Math.random()), startDate: a.startDate, endDate: a.endDate, seasonal: !!a.seasonal }));

        // Render badges and expose removal funcs globally for inline btns
        function renderBadges() {
            // requirements
            requirementsContainer.innerHTML = "";
            editRequirements.forEach(r => {
                const span = document.createElement("span");
                span.className = "badge bg-primary me-1 mb-1";
                span.id = `edit_req_${r.id}`;
                span.innerHTML = `${escapeHtml(r.description)} <button type="button" class="btn-close btn-close-white ms-2" onclick="editRemoveRequirement(${r.id})"></button>`;
                requirementsContainer.appendChild(span);
            });

            // equipment
            equipmentContainer.innerHTML = "";
            editEquipment.forEach(e => {
                const text = (e.rentable ? "Rentable: " : "Required: ") + e.name;
                const span = document.createElement("span");
                span.className = "badge bg-primary me-1 mb-1";
                span.id = `edit_eq_${e.id}`;
                span.innerHTML = `${escapeHtml(text)} <button type="button" class="btn-close btn-close-white ms-2" onclick="editRemoveEquipment(${e.id})"></button>`;
                equipmentContainer.appendChild(span);
            });

            // packages
            packagesContainer.innerHTML = "";
            editPackages.forEach(p => {
                const span = document.createElement("span");
                span.className = "badge bg-primary me-1 mb-1";
                span.id = `edit_pack_${p.id}`;
                span.innerHTML = `${escapeHtml(p.title)} (€${p.price}) <button type="button" class="btn-close btn-close-white ms-2" onclick="editRemovePackage(${p.id})"></button>`;
                packagesContainer.appendChild(span);
            });

            // appointments
            appointmentsContainer.innerHTML = "";
            editAppointments.forEach(a => {
                const span = document.createElement("span");
                span.className = "badge bg-primary me-1 mb-1";
                span.id = `edit_appt_${a.id}`;
                span.innerHTML = `${escapeHtml(a.startDate)} → ${escapeHtml(a.endDate)} (${a.seasonal ? "Seasonal" : "Fixed"}) <button type="button" class="btn-close btn-close-white ms-2" onclick="editRemoveAppointment(${a.id})"></button>`;
                appointmentsContainer.appendChild(span);
            });
        }

        // global removers for inline onclick
        window.editRemoveRequirement = (id) => { editRequirements = editRequirements.filter(r => r.id !== id); renderBadges(); };
        window.editRemoveEquipment = (id) => { editEquipment = editEquipment.filter(e => e.id !== id); renderBadges(); };
        window.editRemovePackage = (id) => { editPackages = editPackages.filter(p => p.id !== id); renderBadges(); };
        window.editRemoveAppointment = (id) => { editAppointments = editAppointments.filter(a => a.id !== id); renderBadges(); };

        renderBadges();

        // Existing images rendering + delete logic
        existingImagesContainer.innerHTML = "";
        existingImageIds = (ev.imageIds || []).slice();
        if (existingImageIds.length > 0) {
            existingImageIds.forEach(imgId => {
                const wrap = document.createElement("div");
                wrap.className = "position-relative m-2";
                wrap.style.width = "120px";

                const img = document.createElement("img");
                img.src = `/api/events/image/${imgId}`;
                img.className = "img-thumbnail";
                img.style.width = "120px";
                img.style.height = "120px";
                img.style.objectFit = "cover";

                const btn = document.createElement("button");
                btn.type = "button";
                btn.className = "btn btn-sm btn-danger position-absolute top-0 end-0";
                btn.innerText = "X";
                btn.onclick = () => {
                    imagesToDelete.push(imgId);
                    wrap.remove();
                    updateImageRequirement(imagesInput, existingImagesContainer);
                };

                wrap.appendChild(img);
                wrap.appendChild(btn);
                existingImagesContainer.appendChild(wrap);
            });
        }

        // images input change -> show filenames + update requirement state
        imagesInput.addEventListener("change", () => {
            const files = Array.from(imagesInput.files || []);
            if (!files.length) fileNamesDiv.innerHTML = "No files selected";
            else fileNamesDiv.innerHTML = `<ul>${files.map(f => `<li>${escapeHtml(f.name)}</li>`).join("")}</ul>`;
            updateImageRequirement(imagesInput, existingImagesContainer);
        });

        // initial requirement check
        updateImageRequirement(imagesInput, existingImagesContainer);

        // Date min restrictions
        const today = new Date().toISOString().split("T")[0];
        startDateEl.min = today;
        endDateEl.min = today;

        // single-day toggle logic
        if (!endDateEl.value || endDateEl.value === "" || endDateEl.value === startDateEl.value) {
            singleDayEl.checked = true;
            endDateWrapper.style.display = "none";
            endDateEl.removeAttribute("required");
            if (!endDateEl.value) endDateEl.value = startDateEl.value || "";
        } else {
            singleDayEl.checked = false;
            endDateWrapper.style.display = "block";
            endDateEl.setAttribute("required", "true");
        }

        singleDayEl.addEventListener("change", () => {
            if (singleDayEl.checked) {
                endDateWrapper.style.display = "none";
                endDateEl.value = startDateEl.value || "";
                endDateEl.removeAttribute("required");
            } else {
                endDateWrapper.style.display = "block";
                endDateEl.setAttribute("required", "true");
            }
        });
        startDateEl.addEventListener("change", () => { if (singleDayEl.checked) endDateEl.value = startDateEl.value; });

        // Load organizers
        async function loadOrganizersForEdit() {
            organizerSelect.innerHTML = `<option value="">Select Organizer</option><option value="NEW">Add New Organizer</option>`;
            try {
                const res = await fetch('/api/organizers');
                if (!res.ok) throw new Error('Failed to load organizers');
                const list = await res.json();
                list.forEach(o => {
                    const opt = document.createElement("option");
                    opt.value = o.id;
                    opt.textContent = `${o.name} (${o.contactEmail})`;
                    organizerSelect.appendChild(opt);
                });
                if (ev.organizerId) organizerSelect.value = ev.organizerId;
            } catch (err) {
                showToast("error", "Could not load organizers");
            }
        }

        organizerSelect.addEventListener("change", () => {
            if (organizerSelect.value === "NEW") {
                newOrgBox.style.display = "block";
                orgNameEl.value = ""; orgEmailEl.value = ""; orgPhoneEl.value = "";
            } else {
                newOrgBox.style.display = "none";
            }
        });

        await loadOrganizersForEdit();

        // dynamic subforms functions (show/save/cancel) - expose to global for inline handlers
        window.editShowRequirementInputs = () => { $("#edit_requirementInputs").style.display = "flex"; modalEl.querySelector(".add-requirement-btn").style.display = "none"; };
        window.editCancelRequirement = () => { $("#edit_requirementInput").value = ""; $("#edit_requirementInputs").style.display = "none"; modalEl.querySelector(".add-requirement-btn").style.display = "inline-block"; };
        window.editSaveRequirement = () => {
            const v = $("#edit_requirementInput").value.trim();
            if (!v) { $("#edit_requirementInput").classList.add("is-invalid"); return; }
            editRequirements.push({ id: Date.now() + Math.random(), description: v });
            window.editCancelRequirement();
            renderBadges();
        };

        window.editShowEquipmentInputs = () => { $("#edit_equipmentInputs").style.display = "flex"; modalEl.querySelector(".add-equipment-btn").style.display = "none"; };
        window.editCancelEquipment = () => { $("#edit_equipmentNameInput").value = ""; $("#edit_equipmentRentableInput").value = ""; $("#edit_equipmentInputs").style.display = "none"; modalEl.querySelector(".add-equipment-btn").style.display = "inline-block"; };
        window.editSaveEquipment = () => {
            const n = $("#edit_equipmentNameInput").value.trim(); const r = $("#edit_equipmentRentableInput").value;
            if (!n || !r) { if(!n) $("#edit_equipmentNameInput").classList.add("is-invalid"); if(!r) $("#edit_equipmentRentableInput").classList.add("is-invalid"); return; }
            editEquipment.push({ id: Date.now() + Math.random(), name: n, rentable: r === "true" });
            window.editCancelEquipment();
            renderBadges();
        };

        window.editShowPackageInputs = () => { $("#edit_packageInputs").style.display = "flex"; modalEl.querySelector(".add-package-btn").style.display = "none"; };
        window.editCancelPackage = () => { $("#edit_packageTitleInput").value = ""; $("#edit_packageDescInput").value = ""; $("#edit_packagePriceInput").value = ""; $("#edit_packageInputs").style.display = "none"; modalEl.querySelector(".add-package-btn").style.display = "inline-block"; };
        window.editSavePackage = () => {
            const t = $("#edit_packageTitleInput").value.trim(); const d = $("#edit_packageDescInput").value.trim(); const p = parseFloat($("#edit_packagePriceInput").value);
            if (!t || !d || isNaN(p) || p < 0) { if(!t) $("#edit_packageTitleInput").classList.add("is-invalid"); if(!d) $("#edit_packageDescInput").classList.add("is-invalid"); if(isNaN(p) || p<0) $("#edit_packagePriceInput").classList.add("is-invalid"); return; }
            editPackages.push({ id: Date.now() + Math.random(), title: t, description: d, price: p });
            window.editCancelPackage();
            renderBadges();
        };

        window.editShowAppointmentInputs = () => { $("#edit_appointmentInputs").style.display = "flex"; modalEl.querySelector(".add-appointment-btn").style.display = "none"; };
        window.editCancelAppointment = () => { $("#edit_apptStart").value = ""; $("#edit_apptEnd").value = ""; $("#edit_apptSeasonal").checked = false; $("#edit_appointmentInputs").style.display = "none"; modalEl.querySelector(".add-appointment-btn").style.display = "inline-block"; };
        window.editSaveAppointment = () => {
            const s = $("#edit_apptStart").value; const e = $("#edit_apptEnd").value; const seasonal = $("#edit_apptSeasonal").checked;
            if (!s || !e || e < s) { if(!s) $("#edit_apptStart").classList.add("is-invalid"); if(!e || e < s) $("#edit_apptEnd").classList.add("is-invalid"); return; }
            editAppointments.push({ id: Date.now() + Math.random(), startDate: s, endDate: e, seasonal });
            window.editCancelAppointment();
            renderBadges();
        };

        // validation helpers for participants & dates
        function validateParticipants() {
            const min = parseInt(minParticipantsEl.value); const max = parseInt(maxParticipantsEl.value);
            if (isNaN(min) || isNaN(max)) { minParticipantsEl.setCustomValidity(""); maxParticipantsEl.setCustomValidity(""); return true; }
            if (max < min) { maxParticipantsEl.setCustomValidity("Max participants cannot be lower than min participants."); return false; }
            maxParticipantsEl.setCustomValidity(""); return true;
        }
        minParticipantsEl.addEventListener("input", validateParticipants);
        maxParticipantsEl.addEventListener("input", validateParticipants);

        function validateDates() {
            if (singleDayEl.checked || !endDateEl.value) { endDateEl.setCustomValidity(""); return true; }
            if (endDateEl.value < startDateEl.value) { endDateEl.setCustomValidity("End date cannot be earlier than start date"); return false; }
            endDateEl.setCustomValidity(""); return true;
        }
        startDateEl.addEventListener("change", validateDates);
        endDateEl.addEventListener("change", validateDates);

        // Next/Back readonly toggle
        function setReadonlyMode(readonly) {
            const textInputs = Array.from(form.querySelectorAll("input, textarea")).filter(i => i.type !== "file");
            const selectAndChecks = form.querySelectorAll("select, input[type='checkbox'], input[type='radio']");
            const addButtons = form.querySelectorAll(".add-requirement-btn, .add-equipment-btn, .add-package-btn, .add-appointment-btn");

            if (readonly) {
                form.classList.add("readonly");
                textInputs.forEach(el => { el.setAttribute("readonly","true"); el.style.backgroundColor="#e9ecef"; });
                selectAndChecks.forEach(el => { el.style.pointerEvents="none"; el.setAttribute("aria-disabled","true"); el.style.backgroundColor="#e9ecef"; });
                imagesInput.classList.add("readonly-file"); imagesInput.style.pointerEvents = "none";
                addButtons.forEach(btn => btn.style.display = "none");
                // hide close buttons on badges for review
                form.querySelectorAll(".btn-close").forEach(b => b.style.display = "none");

                if (!form.querySelector("#editFormOverlay")) {
                    const overlay = document.createElement("div");
                    overlay.id = "editFormOverlay";
                    Object.assign(overlay.style, { position:"absolute", top:"0", left:"0", right:"0", bottom:"80px", background:"transparent", zIndex:"999" });
                    form.appendChild(overlay);
                }

                if (nextBtn) nextBtn.style.display = "none";
                if (backBtn) backBtn.style.display = "inline-block";
                if (submitBtn) submitBtn.style.display = "inline-block";
            } else {
                form.classList.remove("readonly");
                textInputs.forEach(el => { el.removeAttribute("readonly"); el.style.backgroundColor=""; });
                selectAndChecks.forEach(el => { el.style.pointerEvents=""; el.removeAttribute("aria-disabled"); el.style.backgroundColor=""; });
                imagesInput.classList.remove("readonly-file"); imagesInput.style.pointerEvents = "";
                addButtons.forEach(btn => btn.style.display = "inline-block");
                form.querySelectorAll(".btn-close").forEach(b => b.style.display = "");
                const overlay = form.querySelector("#editFormOverlay"); if (overlay) overlay.remove();
                if (nextBtn) nextBtn.style.display = "inline-block";
                if (backBtn) backBtn.style.display = "none";
                if (submitBtn) submitBtn.style.display = "none";
                form.classList.remove("was-validated");
            }
        }

        // Expose next/back functions used by inline onclick in template
        window.editNextStep = (readonly) => setReadonlyMode(readonly);

        window.editCheckValidation = (next) => {
            // organizer new validation
            if (next === true && organizerSelect.value === "NEW") {
                if (!orgNameEl.value.trim() || !orgEmailEl.value.trim()) {
                    if (!orgNameEl.value.trim()) orgNameEl.classList.add("is-invalid"); else orgNameEl.classList.remove("is-invalid");
                    if (!orgEmailEl.value.trim()) orgEmailEl.classList.add("is-invalid"); else orgEmailEl.classList.remove("is-invalid");
                    return;
                }
            }
            if (form.checkValidity() && validateParticipants() && validateDates()) {
                setReadonlyMode(next);
            } else {
                form.classList.add("was-validated");
            }
        };

        // Submit handler -> builds DTO, sends multipart/form-data (event JSON + files) and deleteImageIds as query params
        let submitted = false;
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            submitted = true;

            // build DTO matching EditEventRequestDTO
            const dto = {
                id: eventId,
                name: nameEl.value || null,
                description: descriptionEl.value || null,
                startDate: startDateEl.value || null,
                endDate: (singleDayEl.checked ? (startDateEl.value || null) : (endDateEl.value || null)),
                price: priceEl.value ? parseFloat(priceEl.value) : null,
                depositPercent: depositEl.value ? parseInt(depositEl.value, 10) : null,
                category: categoryEl.value || null,
                organizerId: organizerSelect.value && organizerSelect.value !== "NEW" ? parseInt(organizerSelect.value, 10) : null,
                newOrganizer: organizerSelect.value === "NEW" ? { name: orgNameEl.value || null, email: orgEmailEl.value || null, phone: orgPhoneEl.value || null } : null,
                minParticipants: minParticipantsEl.value ? parseInt(minParticipantsEl.value, 10) : null,
                maxParticipants: maxParticipantsEl.value ? parseInt(maxParticipantsEl.value, 10) : null,
                location: {
                    street: streetEl.value || null,
                    houseNumber: houseNumberEl.value || null,
                    city: cityEl.value || null,
                    postalCode: postalCodeEl.value || null,
                    state: stateEl.value || null,
                    country: countryEl.value || null
                },
                appointments: editAppointments.map(a => ({
                    id: a.id && typeof a.id === "number" ? a.id : null,
                    startDate: a.startDate,
                    endDate: a.endDate,
                    seasonal: !!a.seasonal
                })),
                requirements: editRequirements.map(r => ({ id: r.id && typeof r.id === "number" ? r.id : null, description: r.description })),
                equipment: editEquipment.map(e => ({ id: e.id && typeof e.id === "number" ? e.id : null, name: e.name, rentable: !!e.rentable })),
                additionalPackages: editPackages.map(p => ({ id: p.id && typeof p.id === "number" ? p.id : null, title: p.title, description: p.description, price: p.price }))
            };

            const formData = new FormData();
            formData.append("event", new Blob([JSON.stringify(dto)], { type: "application/json" }));

            // append new files
            const files = Array.from(imagesInput.files || []);
            files.forEach(f => formData.append("images", f));

            // build URL with deleteImageIds as query string
            let url = `/api/events/edit/${eventId}`;
            if (imagesToDelete.length > 0) {
                const params = imagesToDelete.map(id => `deleteImageIds=${encodeURIComponent(id)}`).join("&");
                url = `${url}?${params}`;
            }

            try {
                const res = await fetch(url, { method: "PUT", body: formData });
                if (!res.ok) {
                    const txt = await res.text();
                    showToast("error", txt || "Failed to update event");
                    return;
                }
                const data = await res.json();
                showToast("success", `Event "${data.name}" updated successfully!`);
                await loadEvents();
                const bs = bootstrap.Modal.getInstance(modalEl);
                if (bs) bs.hide();
            } catch (err) {
                console.error(err);
                showToast("error", "Network error: " + err.message);
            }
        });

        // When the modal closes remove it and show details modal again if not submitted
        modalEl.addEventListener("hidden.bs.modal", () => {
            modalEl.remove();
            if (!submitted && detailsBsModal) detailsBsModal.show();
        });

        // attach to DOM and show
        document.body.appendChild(modalEl);
        const bsModal = new bootstrap.Modal(modalEl);
        bsModal.show();
    }; // end openEditModal

})();
