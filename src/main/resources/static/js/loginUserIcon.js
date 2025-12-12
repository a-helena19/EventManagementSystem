document.addEventListener("DOMContentLoaded", async () => {
    await AppSession.loadSession();
    // getting user from local storage
    const loggedInContainer = document.getElementById("loggedInContainer");
    const loggedOutContainer = document.getElementById("loggedOutContainer");
    const adminNav = document.getElementById("adminNav");
    const loggedInProfileIcon = document.getElementById("loggedInProfile");
    const userNameDisplay = document.getElementById("userNameDisplay");

    async function updateUI() {
        await AppSession.loadSession();

        const session = AppSession.getUser();
        const isLoggedIn = session.isLoggedIn;
        const role = session.role || "GUEST";

        // Guest state
        if (!isLoggedIn) {
            if (loggedInContainer) loggedInContainer.style.display = "none";
            if (loggedOutContainer) loggedOutContainer.style.display = "flex";
            if (adminNav) adminNav.style.display = "none";
            return;
        }

        // Logged-in UI
        if (loggedInContainer) loggedInContainer.style.display = "flex";
        if (loggedOutContainer) loggedOutContainer.style.display = "none";

        // Show initials
        const initials = (session.fullName || session.email || "?")
            .split(" ")
            .map(n => n[0]?.toUpperCase() || "")
            .join("");

        if (loggedInProfileIcon) {
            loggedInProfileIcon.textContent = initials;
            loggedInProfileIcon.style.backgroundColor = nameToColor(session.fullName || "");
        }

        if (userNameDisplay) {
            userNameDisplay.textContent = session.fullName || session.email;
        }

        // Admin navigation
        if (adminNav) {
            adminNav.style.display = (role === "ADMIN") ? "block" : "none";
        }

        // Buttons depending on role
        applyRoleUI(role);
    }

    // react to session updates in real time
    window.addEventListener("app:session-changed", updateUI);

    // initial UI
    updateUI();

});

function nameToColor(name) {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    let color = "#";
    for (let i = 0; i < 3; i++) {
        const value = (hash >> (i * 8)) & 255;
        color += ("00" + value.toString(16)).substr(-2);
    }
    return color;
}

//logout
async function logout() {
    await AppSession.logout();
    window.location.href = "/homepage";
}

// ROLE UI LOGIC
function applyRoleUI(role) {
    const adminNav = document.getElementById("adminNav");
    const createEventButton = document.getElementById("createEventButton");

    const canManageEvents = role === "ADMIN" || role === "BACKOFFICE";

    if (adminNav) {
        adminNav.style.display = role === "ADMIN" ? "block" : "none";
    }

    if (createEventButton) {
        createEventButton.style.display = canManageEvents ? "inline-block" : "none";
    }
}

window.getCurrentUserRole = () => AppSession.getUser().role || "GUEST";