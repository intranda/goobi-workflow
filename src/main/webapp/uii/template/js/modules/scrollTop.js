
export const initScrollTop = () => {
    let offset = 50;
    let scrollTop = document.querySelector('.js-scroll-top');
    let progressPath = document.querySelector('.js-scroll-top path');
    if (progressPath === null) {
        return;
    }
    let pathLength = progressPath.getTotalLength();
    let dashArray = pathLength + ' ' + pathLength;
    let dashOffset = pathLength;

    window.addEventListener("scroll", () => {
        let scroll = document.body.scrollTop || document.documentElement.scrollTop;
        let height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        dashOffset = pathLength - (scroll * pathLength / height);
        updateProgressPath(progressPath, dashOffset, dashArray);
        if (scroll > offset) {
            scrollTop.classList.add('active-progress');
        } else {
            scrollTop.classList.remove('active-progress');
        }
    });
};

const updateProgressPath = (path, offset, array) => {
    path.style.setProperty('--scrollProgess', offset);
    path.style.setProperty('--dashArray', array);
};