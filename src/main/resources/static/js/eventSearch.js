// Event Search Functionality
function filterEvents() {
    const searchInput = document.getElementById('searchInput');
    const searchTerm = searchInput.value.toLowerCase().trim();
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
        
        // Check if search term matches any field
        const matches = name.includes(searchTerm) || 
                       location.includes(searchTerm) || 
                       description.includes(searchTerm);
        
        // Show or hide the card based on match
        if (matches) {
            card.style.display = '';
            visibleCount++;
            
            // Add fade-in animation
            card.style.animation = 'fadeIn 0.3s ease-in';
        } else {
            card.style.display = 'none';
        }
    });
    
    // Update search count
    if (searchTerm) {
        searchCount.textContent = `${visibleCount} event${visibleCount !== 1 ? 's' : ''} found`;
        searchCount.style.display = 'inline';
    } else {
        searchCount.style.display = 'none';
    }
    
    // Show/hide no results message
    if (visibleCount === 0 && searchTerm) {
        noResults.style.display = 'block';
        noResults.style.animation = 'fadeIn 0.3s ease-in';
    } else {
        noResults.style.display = 'none';
    }
}

// Clear search on page load
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
        
        // Add clear button functionality
        searchInput.addEventListener('input', (e) => {
            if (e.target.value === '') {
                filterEvents();
            }
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
