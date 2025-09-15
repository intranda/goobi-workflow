import Plyr from 'plyr';
import 'plyr/dist/plyr.css';
import VideoChapters from './video/chapters.js';
import '../../css/media/video-chapters.css';

const initPlayer = () => {
    // Check if video element exists
    const videoElement = document.getElementById('videoplayer');
    if (!videoElement) {
        console.warn('Video element #videoplayer not found');
        return;
    }

    videoElement.style.setProperty('--plyr-color-main', 'var(--clr-primary, #368ee0)');

    try {
        const player = new Plyr('#videoplayer', {
            settings: ['captions', 'quality', 'speed'],
            invertTime: false,
            iconUrl: '../resources/js/dist/plyr.svg',
        });

        player.on('error', (event) => {
            console.error('Plyr player error:', event);
        });

        // Initialize timestamp functionality after player is ready
        initTimestampButtons(player);

        return player;
    } catch (error) {
        console.error('Failed to initialize Plyr player:', error);
    }
};

/**
 * Format seconds to HH:MM:SS or HH:MM:SS.mmm format
 * @param {number} seconds - The time in seconds
 * @param {boolean} includeMilliseconds - Whether to include milliseconds
 * @returns {string} Formatted time string
 */
const formatTimestamp = (seconds, includeMilliseconds = false) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    const milliseconds = Math.floor((seconds % 1) * 1000);

    const hoursStr = hours.toString().padStart(2, '0');
    const minutesStr = minutes.toString().padStart(2, '0');
    const secsStr = secs.toString().padStart(2, '0');

    if (includeMilliseconds && milliseconds > 0) {
        const millisecondsStr = milliseconds.toString().padStart(3, '0');
        return `${hoursStr}:${minutesStr}:${secsStr}.${millisecondsStr}`;
    } else {
        return `${hoursStr}:${minutesStr}:${secsStr}`;
    }
};

/**
 * Initialize timestamp capture buttons
 * @param {Plyr} player - The Plyr instance
 */
const initTimestampButtons = (player) => {
    // Find all buttons with timestamp data attribute
    const timestampButtons = document.querySelectorAll('[data-video-set-timestamp="true"]');

    timestampButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            // Prevent default button behavior
            event.preventDefault();

            const targetId = button.getAttribute('data-video-target');
            if (!targetId) {
                console.warn('Timestamp button missing data-video-target attribute');
                return;
            }

            const targetInput = document.querySelector(`[id$="${targetId}"]`);
            if (!targetInput) {
                console.warn(`Target input element not found: ${targetId}`);
                return;
            }

            // Get current video time
            const currentTime = player.currentTime;
            if (isNaN(currentTime)) {
                console.warn('Unable to get current video time');
                return;
            }

            // Format and set the timestamp
            const formattedTime = formatTimestamp(currentTime, true);
            targetInput.value = formattedTime;

            // Trigger change event for JSF to detect the value change
            const changeEvent = new Event('change', { bubbles: true });
            targetInput.dispatchEvent(changeEvent);

            // Optional: Focus the input briefly to show it was updated
            targetInput.focus();
            setTimeout(() => targetInput.blur(), 100);

        });
    });

};

/**
 * Re-initialize timestamp buttons after AJAX updates
 */
const reinitTimestampButtons = () => {
    const videoElement = document.getElementById('videoplayer');
    if (videoElement && videoElement.plyr) {
        initTimestampButtons(videoElement.plyr);
    }
};

document.addEventListener('DOMContentLoaded', () => {
    initPlayer();
});

faces.ajax.addOnEvent((data) => {
    if (data.status === 'success') {
        const player = initPlayer();
        // Re-initialize timestamp buttons after AJAX updates
        if (player) {
            setTimeout(() => reinitTimestampButtons(), 100);
        }
    }
});

// Export for global access if needed
window.videoTimestamp = {
    formatTimestamp,
    reinitTimestampButtons
};
