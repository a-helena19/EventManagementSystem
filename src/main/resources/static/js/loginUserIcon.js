document.addEventListener("DOMContentLoaded", () => {
    // getting user from local storage
    const loggedInContainer = document.getElementById("loggedInContainer");
    const loggedOutContainer = document.getElementById("loggedOutContainer");
    const adminNav = document.getElementById("adminNav");

    const saved = localStorage.getItem("userInfo");
    if (!saved) {
        loggedInContainer.style.display = "none";
        loggedOutContainer.style.display = "flex";
        if (adminNav) adminNav.style.display = "none";
        applyRoleUI(null);
        return;
    }

    // change user variable into JSON
    const user = JSON.parse(saved);

    // Save the first alphabet from firstname and lastname
    const initials = user.name
        .split(" ")
        .map(n => n[0].toUpperCase())
        .join("");


    const loggedInProfileIcon = document.getElementById("loggedInProfile");

    loggedInProfileIcon.textContent = initials;

    loggedInProfileIcon.style.backgroundColor = nameToColor(user.name);
    loggedInContainer.style.display = "flex";
    loggedOutContainer.style.display = "none";
    applyRoleUI(user);

    const userNameDisplay = document.getElementById("userNameDisplay");

    if (userNameDisplay) {
        userNameDisplay.textContent = user.name; // shows the full name
    }


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
function logout() {
    // delete user from storage
    localStorage.removeItem("userInfo");
    window.location.reload();
}


function applyRoleUI(user) {
    const role = getCurrentUserRole(user);
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

function getCurrentUserRole(user) {
    let storedUser = user;
    if (!storedUser) {
        const saved = localStorage.getItem("userInfo");
        if (!saved) return "GUEST";
        try {
            storedUser = JSON.parse(saved);
        } catch (e) {
            return "GUEST";
        }
    }

    if (!storedUser.role) return "GUEST";
    return storedUser.role.toUpperCase();
}

window.getCurrentUserRole = getCurrentUserRole;