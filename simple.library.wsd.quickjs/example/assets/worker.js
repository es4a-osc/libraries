onmessage = event => {
	const value = event.data;
	postMessage({answer: value.input * 2, cycle: value.self === value, big: value.big});
};
