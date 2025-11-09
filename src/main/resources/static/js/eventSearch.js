// Event Search Functionality
function filterEvents() {
    const searchInput = document.getElementById('searchInput');
    const searchTerm = searchInput.value.toLowerCase().trim();
    const hideCancelledCheckbox = document.getElementById('hideCancelledCheckbox');
    const hideCancelled = hideCancelledCheckbox.checked;
    const eventCards = document.querySelectorAll('.event-card');
    const noResults = document.getElementById('noResults');
    const searchCount = document.getElementById('searchCount');

    let visibleCount = 0;

    // Loop through all event cards
    eventCards.forEach(card => {
        // Get the data attributes
        const name = card.getAttribute('data-name').toLowerCase();
        const location = card.getAttribute('data-location').toLowerCase();
        const description = card.getAttribute('data-description') ?
            card.getAttribute('data-description').toLowerCase() : '';
        const status = card.getAttribute('data-status');

        // Check if search term matches any field
        const matchesSearch = name.includes(searchTerm) ||
            location.includes(searchTerm) ||
            description.includes(searchTerm);

        // Check if event is cancelled and should be hidden
        const isCancelled = status === 'CANCELLED';
        const shouldHide = hideCancelled && isCancelled;

        // Show or hide the card based on both conditions
        if (matchesSearch && !shouldHide) {
            card.style.display = '';
            visibleCount++;

            // Add fade-in animation
            card.style.animation = 'fadeIn 0.3s ease-in';
        } else {
            card.style.display = 'none';
        }
    });

    // Update search count
    if (searchTerm || hideCancelled) {
        searchCount.textContent = `${visibleCount} event${visibleCount !== 1 ? 's' : ''} found`;
        searchCount.style.display = 'inline';
    } else {
        searchCount.style.display = 'none';
    }

    // Show/hide no results message
    if (visibleCount === 0) {
        noResults.style.display = 'block';
        noResults.style.animation = 'fadeIn 0.3s ease-in';
    } else {
        noResults.style.display = 'none';
    }
}

// Clear search on page load
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('searchInput');
    const hideCancelledCheckbox = document.getElementById('hideCancelledCheckbox');

    if (searchInput) {
        searchInput.value = '';

        // Add clear button functionality
        searchInput.addEventListener('input', (e) => {
            if (e.target.value === '') {
                filterEvents();
            }
        });
    }

    // Save checkbox state to localStorage
    if (hideCancelledCheckbox) {
        // Load saved state
        const savedState = localStorage.getItem('hideCancelledEvents');
        if (savedState === 'true') {
            hideCancelledCheckbox.checked = true;
            filterEvents();
        }

        // Save state on change
        hideCancelledCheckbox.addEventListener('change', () => {
            localStorage.setItem('hideCancelledEvents', hideCancelledCheckbox.checked);
        });
    }
});

// Add fade-in animation keyframes if not already in CSS
if (!document.querySelector('style[data-search-animations]')) {
    const style = document.createElement('style');
    style.setAttribute('data-search-animations', 'true');
    style.textContent = `
        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    `;
    document.head.appendChild(style);
}