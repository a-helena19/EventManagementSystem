const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

registerBtn.addEventListener('click', () => {
    container.classList.add("active");

    const form = document.getElementById("sign-up-form");
    console.log(form);
    
    form.addEventListener("submit", (async (e) => {
        e.preventDefault();

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