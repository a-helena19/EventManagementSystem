const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

registerBtn.addEventListener('click', () => {
    container.classList.add("active");

    const form = document.getElementById("sign-up-form");
    
    form.addEventListener("submit", (async (e) => {
        e.preventDefault();

        //HTML5/Bootstrap do Validation
        if (!form.checkValidity()) {
            form.classList.add("was-validated");
            return; //-> The form is invalid, so do not call the API.
        }

        //If Valid: Read data
        const data = {
            email: form.querySelector("#email").value,
            password: form.querySelector("#password").value,
            firstName: form.querySelector("#firstName").value,
            lastName: form.querySelector("#lastName").value
        };

        const formData = new FormData();
        for (const k in data) formData.append(k, data[k]);

        try {
            const res = await fetch('/api/users/create', {
                method: "POST",
                body: formData
            });

            if (!res.ok) {
                const text = await res.text();
                showToast("error", text);
                return;
            }

            const data = await res.json();
            showToast("success", `User for "${data.name}" created successfully!`);

            // Reset UI
            form.reset();
            form.classList.remove("was-validated");
        }
        catch (err) {
            console.error(err);
            showToast("error", "Failed to create user");
        }
    }))
});

loginBtn.addEventListener('click', () => {
    container.classList.remove("active");
});


// log in logic
const loginForm = document.getElementById("sign-in-form");
if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!loginForm.checkValidity()) {
            loginForm.classList.add("was-validated");
            return;
        }

        const email = loginForm.querySelector("#login-email").value;
        const password = loginForm.querySelector("#login-password").value;

        const formData = new FormData();
        formData.append("email", email);
        formData.append("password", password);

        try {
            const res = await fetch('/api/users/login', {
                method: "POST",
                body: formData
            });
            if (!res.ok) {
                const data = await res.json();
                showToast("error", data.message || "Wrong email or password");
                return;
            }

            const user = await res.json();

            // save user
            localStorage.setItem("userInfo", JSON.stringify(user));

            // redirect to Homepage
            window.location.href = "/homepage";
        }
        catch (err) {
            console.error(err);
            showToast("error", "Server error while logging in");
        }
    });
}