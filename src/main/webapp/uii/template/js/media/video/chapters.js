/**
 * Video Chapters Custom Element
 *
 * A Web Component that displays video chapters as clickable navigation links
 * and automatically highlights the current chapter based on video playback position.
 *
 * Usage:
 * <video-chapters video-id="videoplayer"></video-chapters>
 */
class VideoChapters extends HTMLElement {
    constructor() {
        super();
        this.videoElement = null;
        this.textTrack = null;
        this.chapters = [];
        this.currentChapterIndex = -1;

        // Bind methods to maintain context
        this.handleCueChange = this.handleCueChange.bind(this);
        this.handleTimeUpdate = this.handleTimeUpdate.bind(this);
        this.handleChapterClick = this.handleChapterClick.bind(this);
    }

    connectedCallback() {
        this.render();
        this.init();
    }

    disconnectedCallback() {
        this.cleanup();
    }

    static get observedAttributes() {
        return ['video-id'];
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name === 'video-id' && oldValue !== newValue) {
            this.cleanup();
            this.init();
        }
    }

    render() {
        this.innerHTML = `
            <div class="video-chapters">
                <h3 class="chapters-title">Chapters</h3>
                <ol class="chapters-list"></ol>
            </div>
        `;
    }

    init() {
        const videoId = this.getAttribute('video-id');
        if (!videoId) {
            console.warn('VideoChapters: video-id attribute is required');
            return;
        }

        this.videoElement = document.getElementById(videoId);
        if (!this.videoElement) {
            console.warn(`VideoChapters: Video element with id "${videoId}" not found`);
            return;
        }

        // Wait for video metadata to be loaded
        if (this.videoElement.readyState >= 1) {
            this.setupChapters();
        } else {
            this.videoElement.addEventListener('loadedmetadata', () => {
                this.setupChapters();
            }, { once: true });
        }
    }

    setupChapters() {
        const tracks = this.videoElement.textTracks;

        // Find chapters track
        for (let i = 0; i < tracks.length; i++) {
            if (tracks[i].kind === 'chapters') {
                this.textTrack = tracks[i];
                break;
            }
        }

        if (!this.textTrack) {
            console.info('VideoChapters: No chapters track found');
            this.style.display = 'none';
            return;
        }

        // Enable the track to load cues
        this.textTrack.mode = 'hidden';

        // Update the title with the track label
        this.updateTitle();

        // Wait for cues to load
        this.waitForCues().then(() => {
            this.buildChaptersList();
            this.attachEventListeners();
        });
    }

    waitForCues() {
        return new Promise((resolve) => {
            const checkCues = () => {
                if (this.textTrack.cues && this.textTrack.cues.length > 0) {
                    resolve();
                } else {
                    setTimeout(checkCues, 100);
                }
            };
            setTimeout(checkCues, 50);
        });
    }

    updateTitle() {
        const titleElement = this.querySelector('.chapters-title');
        if (titleElement && this.textTrack) {
            // Use the track label if available, otherwise fallback to "Chapters"
            const title = this.getAttribute('label') || 'Chapters';
            titleElement.textContent = title;
        }
    }

    buildChaptersList() {
        const chaptersListElement = this.querySelector('.chapters-list');
        const cues = this.textTrack.cues;

        // Clear existing chapters
        chaptersListElement.innerHTML = '';
        this.chapters = [];

        for (let i = 0; i < cues.length; i++) {
            const cue = cues[i];
            const chapterData = {
                text: cue.text,
                startTime: cue.startTime,
                endTime: cue.endTime,
                index: i
            };

            this.chapters.push(chapterData);

            const listItem = document.createElement('li');
            listItem.className = 'chapter-item';

            const link = document.createElement('a');
            link.className = 'chapter-link';
            link.href = '#';
            link.dataset.startTime = cue.startTime;
            link.dataset.index = i;

            // Format timestamp for display
            const timestamp = this.formatTime(cue.startTime);

            link.innerHTML = `
                <span class="chapter-timestamp">${timestamp}</span>
                <span class="chapter-title">${this.sanitizeText(cue.text)}</span>
            `;

            link.addEventListener('click', this.handleChapterClick);

            listItem.appendChild(link);
            chaptersListElement.appendChild(listItem);
        }
    }

    attachEventListeners() {
        // Listen for cue changes (more accurate for chapter boundaries)
        this.textTrack.addEventListener('cuechange', this.handleCueChange);

        // Also listen to timeupdate as fallback
        this.videoElement.addEventListener('timeupdate', this.handleTimeUpdate);
    }

    handleCueChange() {
        if (this.textTrack.activeCues && this.textTrack.activeCues.length > 0) {
            const activeCue = this.textTrack.activeCues[0];
            const chapterIndex = this.findChapterIndex(activeCue.startTime);
            this.setActiveChapter(chapterIndex);
        }
    }

    handleTimeUpdate() {
        const currentTime = this.videoElement.currentTime;
        const chapterIndex = this.findCurrentChapterIndex(currentTime);

        if (chapterIndex !== this.currentChapterIndex) {
            this.setActiveChapter(chapterIndex);
        }
    }

    handleChapterClick(event) {
        event.preventDefault();

        const startTime = parseFloat(event.currentTarget.dataset.startTime);
        const index = parseInt(event.currentTarget.dataset.index);

        // Jump to chapter time
        this.videoElement.currentTime = startTime;

        // Update active chapter immediately for visual feedback
        this.setActiveChapter(index);

        // Trigger play if video is paused
        if (this.videoElement.paused) {
            this.videoElement.play().catch(e => {
                console.warn('VideoChapters: Could not auto-play video:', e);
            });
        }

        // Dispatch custom event
        this.dispatchEvent(new CustomEvent('chapterchange', {
            detail: {
                chapter: this.chapters[index],
                index: index,
                startTime: startTime
            },
            bubbles: true
        }));
    }

    findChapterIndex(startTime) {
        return this.chapters.findIndex(chapter => chapter.startTime === startTime);
    }

    findCurrentChapterIndex(currentTime) {
        for (let i = this.chapters.length - 1; i >= 0; i--) {
            if (currentTime >= this.chapters[i].startTime) {
                return i;
            }
        }
        return -1;
    }

    setActiveChapter(index) {
        if (index === this.currentChapterIndex) return;

        // Remove current active states
        const activeElements = this.querySelectorAll('.chapter-item.active, .chapter-link.active');
        activeElements.forEach(el => {
            el.classList.remove('active', 'current');
        });

        // Add watched class to previous chapters
        this.querySelectorAll('.chapter-item').forEach((item, i) => {
            if (i < index) {
                item.classList.add('watched');
            } else {
                item.classList.remove('watched');
            }
        });

        // Set new active chapter
        if (index >= 0 && index < this.chapters.length) {
            const chapterItems = this.querySelectorAll('.chapter-item');
            const chapterLinks = this.querySelectorAll('.chapter-link');

            if (chapterItems[index] && chapterLinks[index]) {
                chapterItems[index].classList.add('active');
                chapterLinks[index].classList.add('active', 'current');

                // Scroll active chapter into view
                chapterLinks[index].scrollIntoView({
                    behavior: 'smooth',
                    block: 'nearest'
                });
            }
        }

        this.currentChapterIndex = index;
    }

    formatTime(seconds) {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);

        if (hours > 0) {
            return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
        } else {
            return `${minutes}:${secs.toString().padStart(2, '0')}`;
        }
    }

    sanitizeText(text) {
        // Create a temporary div to safely parse HTML from chapter text
        const div = document.createElement('div');
        div.innerHTML = text;
        return div.innerHTML; // This safely handles HTML entities and tags
    }

    cleanup() {
        if (this.textTrack) {
            this.textTrack.removeEventListener('cuechange', this.handleCueChange);
        }

        if (this.videoElement) {
            this.videoElement.removeEventListener('timeupdate', this.handleTimeUpdate);
        }

        // Clean up chapter click listeners
        this.querySelectorAll('.chapter-link').forEach(link => {
            link.removeEventListener('click', this.handleChapterClick);
        });
    }

    // Public API methods
    jumpToChapter(index) {
        if (index >= 0 && index < this.chapters.length) {
            this.videoElement.currentTime = this.chapters[index].startTime;
            this.setActiveChapter(index);
        }
    }

    getCurrentChapter() {
        return this.currentChapterIndex >= 0 ? this.chapters[this.currentChapterIndex] : null;
    }

    getAllChapters() {
        return [...this.chapters];
    }
}

// Register the custom element
customElements.define('video-chapters', VideoChapters);

export default VideoChapters;
