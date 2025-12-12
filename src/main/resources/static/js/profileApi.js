document.addEventListener("DOMContentLoaded", async function() {
    const profileForm = document.getElementById('profileForm');
    const cancelBtn = document.getElementById('cancelBtn');

    let originalData = {};
    let isEditing = false;

    // Load user data when page loads
    await AppSession.loadSession();
    const ok = await loadUserData();
    if (!ok) return;

    setProfileReadOnly(true);

    // Form submission handler
    profileForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        if (!isEditing) return;

        // Get form values
        const formData = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            email: document.getElementById('email').value
        };

        // Frontend validation
        if (!formData.firstName || !formData.lastName || !formData.email) {
            showToast('error', 'Please fill in all required fields');
            return;
        }

        try {
            // REAL API CALL - Update profile
            const response = await fetch('/api/users/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `firstName=${encodeURIComponent(formData.firstName)}&lastName=${encodeURIComponent(formData.lastName)}&email=${encodeURIComponent(formData.email)}`
            });

            if (response.ok) {
                const result = await response.json();
                showToast('success', result.message);
                await AppSession.loadSession();
                const ok = await loadUserData();// Reload data from server
                if (!ok) return;
                setProfileReadOnly(true);
                isEditing = false;
            } else {
                const error = await response.json();
                showToast('error', error.message || 'Failed to update profile');
            }
        } catch (error) {
            showToast('error', 'Network error: ' + error.message);
        }
    });

    // Cancel button handler
    cancelBtn.addEventListener('click', async function () {
        const ok = await loadUserData();
        if (!ok) return;
        setProfileReadOnly(true);
        isEditing = false;
        showToast('info', 'Changes cancelled');
    });

    // Edit button handler
    document.getElementById('editBtn').addEventListener('click', function () {
        isEditing = true;
        setProfileReadOnly(false);
        showToast('info', 'You can now edit your profile');
    });

    // Delete button handler
    document.getElementById('deleteBtn').addEventListener('click', async function() {
        if (confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
            try {
                const response = await fetch('/api/users/profile', {
                    method: 'DELETE'
                });

                if (response.ok) {
                    // logout with session
                    await AppSession.logout();

                    showToast('success', 'Account deleted successfully');
                    setTimeout(() => {
                        // Redirect to homepage
                        window.location.href = '/homepage';
                    }, 1500);
                } else {
                    const error = await response.json();
                    showToast('error', error.message);
                }
            } catch (error) {
                showToast('error', 'Network error: ' + error.message);
            }
        }
    });



    function setProfileReadOnly(readOnly) {
        const inputs = profileForm.querySelectorAll('input');

        inputs.forEach(input => {
            if (readOnly) {
                input.setAttribute("readonly", "");
                input.style.border = "0px";
                input.style.backgroundColor = "#e9ecef";
                input.style.color = "#495057";
            } else {
                input.removeAttribute("readonly");
                input.style.border = "1px solid";
                input.style.backgroundColor = "white";
                input.style.color = "black";
            }
        });

        // Show/hide buttons
        document.getElementById('editBtn').style.display = readOnly ? 'inline-block' : 'none';
        document.getElementById('deleteBtn').style.display = readOnly ? 'inline-block' : 'none';
        document.getElementById('saveBtn').style.display = readOnly ? 'none' : 'inline-block';
        document.getElementById('cancelBtn').style.display = readOnly ? 'none' : 'inline-block';
    }

    async function loadUserData() {
        const session = AppSession.getUser();
        if (!session.isLoggedIn) {
            showToast("error", "Please log in first");
            window.location.href = "/user";
            return false;
        }

        try {
            const response = await fetch('/api/users/profile');
            if (response.ok) {
                const userData = await response.json();
                document.getElementById('firstName').value = userData.firstName || '';
                document.getElementById('lastName').value = userData.lastName || '';
                document.getElementById('email').value = userData.email || '';
                updateUserDisplay(userData);
                originalData = {...userData};

                const fullName = `${userData.firstName} ${userData.lastName}`.trim();

                const userNameDisplay = document.getElementById('userNameDisplay');
                if (userNameDisplay) {
                    userNameDisplay.textContent = fullName;
                }

                const loggedInProfile = document.getElementById('loggedInProfile');
                if (loggedInProfile && userData.firstName && userData.lastName) {
                    const initials = userData.firstName.charAt(0) + userData.lastName.charAt(0);
                    loggedInProfile.textContent = initials.toUpperCase();
                    loggedInProfile.style.backgroundColor = nameToColor(fullName);
                }
                return true;
            } else {
                // Handle unauthorized (user not logged in)
                if (response.status === 401 || response.status === 403) {
                    showToast('error', 'Please login first');
                    setTimeout(() => {
                        window.location.href = '/user';
                    }, 2000);
                    return false;
                }
                throw new Error('Failed to load user data');
            }
        } catch (error) {
            console.error('Failed to load user data:', error);
            showToast('error', 'Failed to load profile data. Please try again.');

            // Don't use mock data - show empty form instead
            document.getElementById('firstName').value = '';
            document.getElementById('lastName').value = '';
            document.getElementById('email').value = '';
            updateUserDisplay({firstName: '', lastName: '', email: ''});

            return false;
        }
    }

    function updateUserDisplay(userData) {
        const fullName = `${userData.firstName || ''} ${userData.lastName || ''}`.trim();
        document.getElementById('userFullName').textContent = fullName || 'Guest User';
        document.getElementById('userEmail').textContent = userData.email || 'Not logged in';

        // Update profile picture initials
        const profileCircle = document.getElementById('profilePicture');
        if (userData.firstName && userData.lastName) {
            const initials = userData.firstName.charAt(0) + userData.lastName.charAt(0);
            profileCircle.textContent = initials.toUpperCase();

            profileCircle.style.backgroundColor = nameToColor(fullName);
        }
    }

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

});