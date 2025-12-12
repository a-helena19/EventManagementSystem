document.addEventListener("DOMContentLoaded", async () => {
    // --- Load real session from backend ---
    await AppSession.loadSession();
    const session = AppSession.getUser();

    const roleBadge = document.getElementById("roleBadge");

    if (roleBadge) {
        roleBadge.textContent = `You are logged in as ${session.role}`;
        roleBadge.style.display = "inline-block";
    }

    if (!session.isLoggedIn || session.role !== "ADMIN") {
        showToast("error", "You are not allowed to view this page.");
        setTimeout(() => window.location.href = "/homepage", 1200);
        return;
    }

    loadUsers();
});

async function loadUsers() {
    const tbody = document.querySelector("#usersTable tbody");
    if (!tbody) return;

    try {
        const res = await fetch("/api/users");
        if (!res.ok) throw new Error("Failed to load users");

        const users = await res.json();
        tbody.innerHTML = "";

        users.forEach(user => {
            const tr = document.createElement("tr");

            const nameTd = document.createElement("td");
            nameTd.textContent = `${user.firstName} ${user.lastName}`;

            const emailTd = document.createElement("td");
            emailTd.textContent = user.email || "-";

            const roleTd = document.createElement("td");
            roleTd.textContent = user.role;

            const actionsTd = document.createElement("td");
            actionsTd.classList.add("text-end");

            const select = document.createElement("select");
            select.classList.add("form-select", "form-select-sm", "w-auto", "d-inline-block");
            ["ADMIN", "BACKOFFICE", "FRONTOFFICE", "USER"].forEach(r => {
                const option = document.createElement("option");
                option.value = r;
                option.textContent = r;
                if (user.role === r) option.selected = true;
                select.appendChild(option);
            });

            const saveBtn = document.createElement("button");
            saveBtn.textContent = "Update";
            saveBtn.classList.add("btn", "btn-primary", "btn-sm", "ms-2");
            saveBtn.addEventListener("click", async () => {
                await updateUserRole(user.id, select.value, roleTd);
            });

            actionsTd.appendChild(select);
            actionsTd.appendChild(saveBtn);

            tr.appendChild(nameTd);
            tr.appendChild(emailTd);
            tr.appendChild(roleTd);
            tr.appendChild(actionsTd);

            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-danger">${err.message}</td></tr>`;
    }
}

async function updateUserRole(userId, role, roleTd) {
    try {
        const res = await fetch(`/api/users/${userId}/role?role=${role}`, {
            method: "PUT"
        });

        if (!res.ok) {
            const data = await res.json();
            throw new Error(data.message || "Failed to update role");
        }

        roleTd.textContent = role;
        showToast("success", "User role updated");
    } catch (err) {
        console.error(err);
        showToast("error", err.message);
    }
}
