const METADATA_COMMIT_PATTERN = /:metadata:/;
const TREE_STRUCT_CLICK_PATTERN = /^treeform:.*:link\d*$/;
const DEFER_POLL_MS = 10;
const DEFER_TIMEOUT_MS = 3000;

let commitsInFlight = 0;

const isMetadataCommit = (sourceId) =>
    typeof sourceId === 'string' && METADATA_COMMIT_PATTERN.test(sourceId);

const isTreeStructClick = (sourceId) =>
    typeof sourceId === 'string' && TREE_STRUCT_CLICK_PATTERN.test(sourceId);

export const initRaceGuard = () => {
    if (window.__raceGuardInstalled) return;
    if (typeof faces === 'undefined' || !faces.ajax || !faces.ajax.request) return;
    window.__raceGuardInstalled = true;

    const origRequest = faces.ajax.request;

    faces.ajax.request = function (source, event, options) {
        const sourceId = typeof source === 'string' ? source : (source && source.id);

        if (isMetadataCommit(sourceId)) {
            commitsInFlight++;
            const wrappedOptions = Object.assign({}, options);
            const userOnEvent = wrappedOptions.onevent;
            wrappedOptions.onevent = function (data) {
                if (data && (data.status === 'complete' || data.status === 'success')) {
                    commitsInFlight = Math.max(0, commitsInFlight - 1);
                }
                if (typeof userOnEvent === 'function') {
                    userOnEvent.call(this, data);
                }
            };
            return origRequest.call(faces.ajax, source, event, wrappedOptions);
        }

        if (isTreeStructClick(sourceId) && commitsInFlight > 0) {
            const argsCopy = [source, event, options];
            const deadline = Date.now() + DEFER_TIMEOUT_MS;
            const retry = () => {
                if (commitsInFlight === 0) {
                    origRequest.apply(faces.ajax, argsCopy);
                    return;
                }
                if (Date.now() > deadline) {
                    origRequest.apply(faces.ajax, argsCopy);
                    return;
                }
                setTimeout(retry, DEFER_POLL_MS);
            };
            setTimeout(retry, DEFER_POLL_MS);
            return;
        }

        return origRequest.apply(faces.ajax, arguments);
    };
};
