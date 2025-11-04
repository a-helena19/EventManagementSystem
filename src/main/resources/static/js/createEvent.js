function setEmpty() {
    const name = document.getElementById('name');
    const description = document.getElementById('description');
    const location = document.getElementById('location');
    const date = document.getElementById('date');
    const price = document.getElementById('price');
    const images = document.getElementById('images');

    name.value = "";
    description.value = "";
    location.value = "";
    date.value = "";
    price.value = "";
    images.value = null;

}


function openPopup(open) {
    const openBtn = document.getElementById('openFormBtn');
    const popup = document.getElementById('popupForm');
    const closePopup = document.getElementById('closePopup');
    const date = document.getElementById('date');


    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0'); // Months start at 0
    const dd = String(today.getDate()).padStart(2, '0');
    const minDate = `${yyyy}-${mm}-${dd}`;


    if (open === "true") {
        // Popup öffnen
        openBtn.addEventListener('click', () => {
            popup.style.display = 'flex';
            setEmpty();
            nextStep("false");
            date.setAttribute("min", minDate);
            // Set min date = today


        });
    }
    else if (open === "false") {
        // Popup schließen
        closePopup.addEventListener('click', () => {
            popup.style.display = 'none';
        });
    }

}


function nextStep(disable) {
    const name = document.getElementById('name');
    const description = document.getElementById('description');
    const location = document.getElementById('location');
    const date = document.getElementById('date');
    const price = document.getElementById('price');
    const images = document.getElementById('images');
    const submitBtn = document.getElementById('submitBtn');
    const backBtn = document.getElementById('backBtn');
    const nextBtn = document.getElementById('nextBtn');

    if (disable === "true") {
        nextBtn.setAttribute("style", "display: none;");
        backBtn.setAttribute("style", "display: inline;");
        submitBtn.setAttribute("style", "display: inline;");

        name.setAttribute("readonly", "");
        description.setAttribute("readonly", "");
        location.setAttribute("readonly", "");
        date.setAttribute("readonly", "");
        price.setAttribute("readonly", "");
        images.classList.add('readonly-file'); // readonly Attribute won't prevent choosing files -> thus we use css


        name.setAttribute("style", "border: 0px");
        description.setAttribute("style", "border: 0px");
        location.setAttribute("style", "border: 0px");
        date.setAttribute("style", "border: 0px");
        price.setAttribute("style", "border: 0px");
        images.setAttribute("style", "border: 0px");
    }
    else if (disable === "false") {
        nextBtn.setAttribute("style", "display: flex;");
        backBtn.setAttribute("style", "display: none;");
        submitBtn.setAttribute("style", "display: none;");

        name.removeAttribute("readonly");
        description.removeAttribute("readonly");
        location.removeAttribute("readonly");
        date.removeAttribute("readonly");
        price.removeAttribute("readonly");
        images.classList.remove('readonly-file');


        name.setAttribute("style", "border: 1px solid");
        description.setAttribute("style", "border: 1px solid");
        location.setAttribute("style", "border: 1px solid");
        date.setAttribute("style", "border: 1px solid");
        price.setAttribute("style", "border: 1px solid");
        images.setAttribute("style", "border: 1px solid");
    }
}



function checkValidation (next) {
    const form = document.getElementById("event-form");

    // HTML5 validation check
    if (form.checkValidity()) {
        // Optionally: extra custom verification
        // e.g., check if some field matches a pattern, etc.

        nextStep(next); // <-- run your function
    } else {
        // Show native browser validation messages
        form.reportValidity();
    }
}

function listFileNames() {
    const fileInput = document.getElementById('images');
    const fileNamesDiv = document.getElementById('fileNames');


    const files = Array.from(fileInput.files);

    if (files.length === 0) {
        fileNamesDiv.textContent = "No files selected";
        return;
    }

    // Dateinamen schön anzeigen
    fileNamesDiv.innerHTML = `
        <ul>
          ${files.map(f => `<li>${f.name}</li>`).join('')}
        </ul>
    `;

}




