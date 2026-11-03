(() => {
	if (host.名称 !== '初始名称') {
		throw new Error('读取宿主字段');
	}
	host.名称 = 'JS 修改';
	if (typeof host.相加 !== 'function' || typeof host.回声 !== 'function') {
		throw new Error('宿主成员：' + Object.keys(host).join(',') + '；相加：' + typeof host.相加 + '；回声：' + typeof host.回声);
	}
	if (host.相加(20, 22) !== 42 || host.回声('文本') !== '文本') {
		throw new Error('宿主方法');
	}

	const input = [1, {answer: 42}];
	input.push(input);
	const output = host.复制集合(input);
	return output[1].answer === 42 && output[2] === output;
})()
