import Plyr from 'plyr';
import 'plyr/dist/plyr.css';
import './video/chapters.js';
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

const parseVttTimestamp = (ts) => {
    const [h, m, s] = ts.trim().split(':').map(parseFloat);
    return h * 3600 + m * 60 + s;
};

const parseVtt = (vttText) => {
    const cues = [];
    for (const block of vttText.split(/\n\n+/)) {
        const lines = block.trim().split('\n');
        const timeLineIdx = lines.findIndex(l => l.includes('-->'));
        if (timeLineIdx < 0) continue;
        const [startStr, endStr] = lines[timeLineIdx].split('-->');
        cues.push({
            start: parseVttTimestamp(startStr),
            end: parseVttTimestamp(endStr),
            text: lines.slice(timeLineIdx + 1).join('\n').trim()
        });
    }
    return cues;
};

/**
 * Update chapter cues directly via the TextTrack API from inline VTT data
 * embedded by the server in [data-role="chapter-vtt"]. This reflects the
 * current in-memory state without requiring a save or a blob URL.
 */
const updateChapters = () => {
    const dataEl = document.querySelector('[data-role="chapter-vtt"]');
    if (!dataEl) return;

    const vttContent = dataEl.textContent.trim();
    if (!vttContent) return;

    const video = document.getElementById('videoplayer');
    if (!video) return;

    let textTrack = null;
    for (const t of video.textTracks) {
        if (t.kind === 'chapters') { textTrack = t; break; }
    }
    if (!textTrack) return;

    textTrack.mode = 'hidden';
    while (textTrack.cues?.length) {
        textTrack.removeCue(textTrack.cues[0]);
    }
    for (const { start, end, text } of parseVtt(vttContent)) {
        textTrack.addCue(new VTTCue(start, end, text));
    }

    document.querySelector('video-chapters')?.refresh();
};

document.addEventListener('DOMContentLoaded', () => {
    initPlayer();
    setTimeout(updateChapters, 200);
});

faces.ajax.addOnEvent((data) => {
    if (data.status === 'success') {
        const player = initPlayer();
        setTimeout(() => {
            if (player) reinitTimestampButtons();
            updateChapters();
        }, 100);
    }
});

// Export for global access if needed
window.videoTimestamp = {
    formatTimestamp,
    reinitTimestampButtons
};
