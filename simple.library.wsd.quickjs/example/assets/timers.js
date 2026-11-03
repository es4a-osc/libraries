globalThis.timerDone = false;
globalThis.timerAnswer = 0;
let ticks = 0, microtask = false;

queueMicrotask(() => { microtask = true; });
const cancelled = setTimeout(() => { throw new Error('取消未生效'); }, 100);
clearTimeout(cancelled);
clearTimeout();

const interval = setInterval(() => {
	if (++ticks === 3) {
		clearInterval(interval);
		setTimeout((value) => {
			Promise.resolve(value).then(result => {
				timerAnswer = microtask && ticks === 3 ? result : -1;
				timerDone = true;
			});
		}, 0, 42);
	}
}, 5);
