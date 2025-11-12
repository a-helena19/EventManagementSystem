// Event Search Functionality
function filterEvents() {
    const searchInput = document.getElementById('searchInput').value.toLowerCase();
    const hideCancelled = document.getElementById('hideCancelledCheckbox').checked;
    const cards = document.querySelectorAll(".event-card");

    let visibleCount = 0;

    // Loop through all event cards
    cards.forEach(card => {
        const name = card.querySelector(".card-title").textContent.toLowerCase();
        const location = card.dataset.location;
        const status = card.dataset.status.toLowerCase();

        const matchesSearch = name.includes(searchInput) || location.includes(searchInput);
        const matchesStatus = !(hideCancelled && status === "cancelled");

        const visible = matchesSearch && matchesStatus;
        card.style.display = visible ? "" : "none";

        if (visible) visibleCount++;
    });

    document.getElementById("noResults").style.display = visibleCount === 0 ? "block" : "none";
}