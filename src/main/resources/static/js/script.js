(function () {
    const popup     = document.getElementById("popup");
    const openPopup = document.getElementById("openPopup");
    const nextBtn   = document.getElementById("nextBtn");
    const backBtn   = document.getElementById("backBtn");
    const step1     = document.getElementById("step1");
    const step2     = document.getElementById("step2");

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
        // show confirm (step 2), hide edit (step 1)
        step1.style.display = "none";
        step2.style.display = "block";

        // disable inputs (but not buttons)
        disableFields(true);

        // hide Next (optional), show Back + Submit
        if (nextBtn) nextBtn.style.display = 'none';
        if (backBtn) backBtn.style.display = '';
    });

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

    // Just to see if the file loaded
    console.log('[popup] handlers attached');
})();
