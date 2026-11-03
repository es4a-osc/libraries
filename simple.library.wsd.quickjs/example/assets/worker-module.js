import {factor} from './worker-factor.js';

await new Promise(resolve => setTimeout(resolve, 20));

onmessage = event => {
	postMessage(event.data * factor);
};
