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
export const updateChapters = () => {
    const dataEl = document.querySelector('[data-role="chapter-vtt"]');
    if (!dataEl) return;

    const vttContent = dataEl.textContent.trim();

    const video = document.getElementById('videoplayer');
    if (!video) return;

    let textTrack = null;
    for (const t of video.textTracks) {
        if (t.kind === 'chapters') { textTrack = t; break; }
    }
    if (!textTrack) return;

    textTrack.mode = 'hidden';
    // Snapshot the live cue list before removal — iterating the live list while
    // removeCue() modifies it can exit early and leave stale cues behind.
    for (const cue of Array.from(textTrack.cues || [])) {
        textTrack.removeCue(cue);
    }
    for (const { start, end, text } of parseVtt(vttContent)) {
        textTrack.addCue(new VTTCue(start, end, text));
    }

    document.querySelector('video-chapters')?.refresh();
};
