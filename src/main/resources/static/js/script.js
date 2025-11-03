(function () {
    const popup     = document.getElementById("popup");
    const openPopup = document.getElementById("openPopup");
    const nextBtn   = document.getElementById("nextBtn");
    const backBtn   = document.getElementById("backBtn");
    const step1     = document.getElementById("step1");
    const step2     = document.getElementById("step2");
    const cancelBtn = document.getElementById("cancelBtn");

    console.log('[popup] init', { popup, openPopup, nextBtn, backBtn, step1, step2 });

    if (!popup || !openPopup || !step1 || !step2) {
        console.error('[popup] missing required elements');
        return;
    }

    function disableFields(disabled) {
        document
            .querySelectorAll('#event-form input, #event-form textarea, #event-form select')
            .forEach(el => { el.disabled = disabled; });
    }

    openPopup?.addEventListener("click", () => {
        console.log('[popup] open click');
        popup.style.display = "block";
        // always start on step 1 in editable mode
        step1.style.display = "block";
        step2.style.display = "none";
        disableFields(false);
        nextBtn && (nextBtn.style.display = '');
        backBtn && (backBtn.style.display = '');
    });

    nextBtn?.addEventListener("click", () => {
        console.log('[popup] next click -> confirm');

        // Vor dem Wechsel zu Step 2: Überblick erstellen
        updateReview();

        // show confirm (step 2), hide edit (step 1)
        step1.style.display = "none";
        step2.style.display = "block";

        // disable inputs (but not buttons)
        disableFields(true);

        // hide Next (optional), show Back + Submit
        if (nextBtn) nextBtn.style.display = 'none';
        if (backBtn) backBtn.style.display = '';
    });

    // Funktion, um den Review-Inhalt zu aktualisieren
    function updateReview() {
        const reviewContent = document.getElementById("reviewContent");
        if (!reviewContent) return;

        // Werte aus den Formularfeldern holen
        const name = document.getElementById("name").value;
        const description = document.getElementById("description").value;
        const location = document.getElementById("location").value;
        const date = document.getElementById("date").value;
        const price = document.getElementById("price").value;

        // Inhalt für Step 2 dynamisch erstellen
        reviewContent.innerHTML = `
            <ul class="list-group">
                <li class="list-group-item"><strong>Name:</strong> ${name}</li>
                <li class="list-group-item"><strong>Description:</strong> ${description || '-'}</li>
                <li class="list-group-item"><strong>Location:</strong> ${location}</li>
                <li class="list-group-item"><strong>Date:</strong> ${date}</li>
                <li class="list-group-item"><strong>Price:</strong> €${parseFloat(price).toFixed(2)}</li>
            </ul>
        `;
    }

    backBtn?.addEventListener("click", () => {
        console.log('[popup] back click -> edit');
        // back to edit
        step2.style.display = "none";
        step1.style.display = "block";

        // re-enable inputs
        disableFields(false);

        // show Next again
        if (nextBtn) nextBtn.style.display = '';
    });

    cancelBtn?.addEventListener("click", () => {
        console.log('[popup] cancel click -> close popup');
        popup.style.display = "none";
        step1.style.display = "block";
        step2.style.display = "none";
        disableFields(false);
    })

    // Just to see if the file loaded
    console.log('[popup] handlers attached');
})();
