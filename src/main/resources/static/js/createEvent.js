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
        form.classList.remove('was-validated'); // reset validation styling
        nextStep(false);


// Set min date to today
        const today = new Date();
        const yyyy = today.getFullYear();
        const mm = String(today.getMonth() + 1).padStart(2, '0');
        const dd = String(today.getDate()).padStart(2, '0');
        date.setAttribute("min", `${yyyy}-${mm}-${dd}`);
    });
});


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

    if (disable) {
        nextBtn.style.display = "none";
        backBtn.style.display = "inline-block";
        submitBtn.style.display = "inline-block";

        [name, description, location, date, price].forEach(element => {
            element.setAttribute("readonly", "");
            element.style.border = "0px";
            element.style.backgroundColor = "#e9ecef"; // light grey/darker background
            element.style.color = "#495057"; // slightly darker text for readability
        });
        images.classList.add('readonly-file'); // readonly Attribute won't prevent choosing files -> thus we use css
        images.style.border = "0px";
        images.style.backgroundColor = "#e9ecef";
    }
    else if (!disable) {
        nextBtn.style.display = "inline-block";
        backBtn.style.display = "none";
        submitBtn.style.display = "none";

        [name, description, location, date, price].forEach(element => {
            element.removeAttribute("readonly");
            element.style.border = "1px solid";
            element.style.backgroundColor = "white"; // restore default
            element.style.color = "black";
        });
        images.classList.remove('readonly-file');
        images.style.border = "1px solid";
        images.style.backgroundColor = "white";
    }
}



function checkValidation (next) {
    const form = document.getElementById("event-form");

    if (form.checkValidity()) {
        nextStep(next); // <-- run nextStep function
    } else {
        form.classList.add('was-validated');
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

    // makes a better view
    fileNamesDiv.innerHTML = `
        <ul>
          ${files.map(f => `<li>${f.name}</li>`).join('')}
        </ul>
    `;

}




