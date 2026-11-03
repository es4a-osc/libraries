globalThis.workersDone = false;
globalThis.workerAnswer = 0;
globalThis.moduleAnswerNew = 0;
globalThis.closeAnswer = 0;

const complete = () => {
	workersDone = workerAnswer === 42 && moduleAnswerNew === 84 && closeAnswer === 21 && reportedError && timerError;
};
const fail = event => { throw new Error(event.message); };
let reportedError = false, timerError = false;
const timed = new Worker('worker-timer-error.js');
timed.onerror = event => {
	timerError = event.message.includes('计时器示例错误');
	timed.terminate();
	complete();
};
const broken = new Worker('worker-error.js');
broken.onerror = event => {
	reportedError = event.message.includes('工作线程示例错误');
	complete();
};

const worker = new Worker('worker.js');
worker.onerror = fail;
worker.onmessage = event => {
	const value = event.data;
	workerAnswer = value.cycle && value.big === 12345678901234567890n ? value.answer : -1;
	worker.terminate();
	complete();
};
const input = {input: 21, big: 12345678901234567890n};
input.self = input;
worker.postMessage(input);

// 不可复制的数据和转移列表会明确报告错误。
let refused = 0;
for (const value of [() => 1, new SharedArrayBuffer(1)]) {
	try {
		worker.postMessage(value);
	} catch (error) {
		refused++;
	}
}
try {
	worker.postMessage(1, [new ArrayBuffer(1)]);
} catch (error) {
	refused++;
}
if (refused !== 3) {
	throw new Error('不支持的工作线程数据没有被拒绝');
}

const moduleWorker = new Worker('worker-module.js', {type: 'module'});
moduleWorker.onerror = fail;
moduleWorker.onmessage = event => {
	moduleAnswerNew = event.data;
	moduleWorker.terminate();
	complete();
};
moduleWorker.postMessage(21);

const closing = new Worker('worker-close.js');
closing.onerror = fail;
closing.onmessage = event => {
	closeAnswer = event.data;
	complete();
};
