/**
 * Toggles visibility of elements via [data-toggle-notification] buttons.
 * Uses event delegation so it works after AJAX DOM replacements.
 */
export const initNotificationToggle = () => {
    document.addEventListener('click', (event) => {
        const btn = event.target.closest('[data-toggle-notification]');
        if (!btn) return;

        const targetId = btn.getAttribute('data-toggle-notification');
        const panel = document.getElementById(targetId);
        if (!panel) return;

        panel.classList.toggle('show');
        btn.setAttribute('aria-expanded', panel.classList.contains('show'));
    });
}
