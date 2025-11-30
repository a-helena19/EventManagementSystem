const mockUserData = {
    id: 1,
    firstName: "John",
    lastName: "Doe",
    email: "john.doe@example.com"
};

document.addEventListener("DOMContentLoaded", function() {
    const profileForm = document.getElementById('profileForm');
    const cancelBtn = document.getElementById('cancelBtn');

    let originalData = {};
    let isEditing = false;

    // Load user data when page loads
    loadUserData();
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

        try {  // ADD try-catch block
            // REAL API CALL - REPLACE the setTimeout
            const response = await fetch('/api/users/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `firstName=${encodeURIComponent(formData.firstName)}&lastName=${encodeURIComponent(formData.lastName)}&email=${encodeURIComponent(formData.email)}`
            });

            if (response.ok) {
                const userData = await response.json();
                showToast('success', 'Profile updated successfully!');
                updateUserDisplay(userData);
                setProfileReadOnly(true);
                isEditing = false;
            } else {
                const error = await response.json();
                showToast('error', error.message);
            }
        } catch (error) {
            showToast('error', 'Network error: ' + error.message);
        }
    });


    // Cancel button handler
    cancelBtn.addEventListener('click', function () {
        loadUserData();
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
                    showToast('success', 'Account deleted successfully');
                    setTimeout(() => {
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

    async function loadUserData() {  // ADD async
        try {  // ADD try-catch
            // REAL API CALL - REPLACE mock data
            const response = await fetch('/api/users/profile');
            if (response.ok) {
                const userData = await response.json();
                document.getElementById('firstName').value = userData.firstName || '';
                document.getElementById('lastName').value = userData.lastName || '';
                document.getElementById('email').value = userData.email || '';
                updateUserDisplay(userData);
                originalData = {...userData};
            } else {
                throw new Error('Failed to load user data');
            }
        } catch (error) {
            console.error('Failed to load user data:', error);
            showToast('error', 'Failed to load profile data');
            // Fallback to mock data
            const mockUserData = {firstName: "John", lastName: "Doe", email: "user@example.com"};
            document.getElementById('firstName').value = mockUserData.firstName;
            document.getElementById('lastName').value = mockUserData.lastName;
            document.getElementById('email').value = mockUserData.email;
            updateUserDisplay(mockUserData);
        }
    }

    function updateUserDisplay(userData) {
        document.getElementById('userFullName').textContent = `${userData.firstName} ${userData.lastName}`;
        document.getElementById('userEmail').textContent = userData.email;
    }
})
