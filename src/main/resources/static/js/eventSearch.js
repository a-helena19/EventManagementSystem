// Event Search Functionality
function filterEvents() {
    const searchInput = document.getElementById('searchInput').value.toLowerCase();
    const hideCancelled = document.getElementById('hideCancelledCheckbox').checked;
    const cards = document.querySelectorAll(".event-card");
    const searchCount = document.getElementById('searchCount');

    let visibleCount = 0;

    // Loop through all event cards
    cards.forEach(card => {
        const name = card.querySelector(".card-title")?.textContent.toLowerCase() || "";
        const location = card.dataset.location || "";
        const status = card.dataset.status?.toLowerCase() || "";

        // Check if matches search - searches in event name and location
        const matchesSearch = name.includes(searchInput) || location.includes(searchInput);
        const matchesStatus = !(hideCancelled && status === "cancelled");

        const visible = matchesSearch && matchesStatus;
        card.style.display = visible ? "" : "none";

        if (visible) visibleCount++;
    });

    // Update search count badge
    if (searchInput || hideCancelled) {
        searchCount.textContent = `${visibleCount} event${visibleCount !== 1 ? 's' : ''} found`;
        searchCount.style.display = 'inline';
    } else {
        searchCount.style.display = 'none';
    }

    // Show/hide no results message
    document.getElementById("noResults").style.display = visibleCount === 0 ? "block" : "none";
}