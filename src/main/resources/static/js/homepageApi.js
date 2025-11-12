document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById('createEventModal');
    const form = document.getElementById('event-form');
    const name = document.getElementById('name');
    const description = document.getElementById('description');
    const location = document.getElementById('location');
    const date = document.getElementById('date');
    const price = document.getElementById('price');
    const images = document.getElementById('images');

    // Reset form when modal is opened
    modalEl.addEventListener('show.bs.modal', () => {
        name.value = '';
        description.value = '';
        location.value = '';
        date.value = '';
        price.value = '';
        images.value = null;
        listFileNames();
        form.classList.remove('was-validated');
        nextStep(false);

        // Set min date to today
        const today = new Date();
        const yyyy = today.getFullYear();
        const mm = String(today.getMonth() + 1).padStart(2, '0');
        const dd = String(today.getDate()).padStart(2, '0');
        date.setAttribute("min", `${yyyy}-${mm}-${dd}`);
    });

    // Form submission to backend
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        // Build FormData for backend
        const formData = new FormData();
        formData.append("name", name.value);
        formData.append("description", description.value);
        formData.append("location", location.value);
        formData.append("date", date.value);
        formData.append("price", price.value);

        if (images.files.length > 0) {
            for (const file of images.files) {
                formData.append("images", file);
            }
        }

        try {
            const res = await fetch("/api/events/create", {
                method: "POST",
                body: formData
            });

            if (res.ok) {
                const data = await res.json();
                showToast("success", `Event "${data.name}" created successfully!`);
                form.reset();
                listFileNames();
                nextStep(false);
                const modal = bootstrap.Modal.getInstance(modalEl);
                modal.hide();
            } else {
                const text = await res.text();
                showToast("error", text);
            }
        } catch (err) {
            showToast("error", "Network error: " + err.message);
        }
    });
});

// --- Multi-step Event Form (Next/Back) ---
function nextStep(disable) {
    const name = document.getElementById('name');
    const description = document.getElementById('description');
    const location = document.getElementById('location');
    const date = document.getElementById('date');
    const price = document.getElementById('price');
    const images = document.getElementById('images');
    const imagesLabel = document.querySelector('label[for="images"]');
    const submitBtn = document.getElementById('submitBtn');
    const backBtn = document.getElementById('backBtn');
    const nextBtn = document.getElementById('nextBtn');

    if (disable) {
        nextBtn.style.display = "none";
        backBtn.style.display = "inline-block";
        submitBtn.style.display = "inline-block";

        [name, description, location, date, price].forEach(el => {
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
    } else {
        nextBtn.style.display = "inline-block";
        backBtn.style.display = "none";
        submitBtn.style.display = "none";

        [name, description, location, date, price].forEach(el => {
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
    }
}

// --- Validation for Next Button ---
function checkValidation(next) {
    const form = document.getElementById("event-form");

    if (form.checkValidity()) {
        nextStep(next);
    } else {
        form.classList.add('was-validated');
    }
}

// --- Display file names ---
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
