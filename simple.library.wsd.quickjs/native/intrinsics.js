/* ES4A QuickJS：捕获标准内建函数，避免宿主辅助操作受全局名称覆盖影响。 */
(() => {
	'use strict';
	const E = TypeError, PromiseClass = Promise, promiseResolve = Promise.resolve;
	const global = globalThis;
	const R = Reflect, O = Object, A = Array, S = String, J = JSON, N = Number;
	const Sym = Symbol, symbolFor = Symbol.for, symbolKey = Symbol.keyFor;
	const DV = DataView, P = Proxy, D = Date, RE = RegExp;
	const M = Map, T = Set, WM = WeakMap, WT = WeakSet;
	const apply = R.apply, construct = R.construct, ownKeys = R.ownKeys;
	const define = R.defineProperty, descriptor = O.getOwnPropertyDescriptor;
	const getPrototype = O.getPrototypeOf, setPrototype = O.setPrototypeOf;
	const keys = O.keys, freeze = O.freeze, seal = O.seal;
	const prevent = O.preventExtensions, extensible = O.isExtensible;
	const frozen = O.isFrozen, sealed = O.isSealed, same = O.is;
	const hasOwn = O.prototype.hasOwnProperty, tag = O.prototype.toString;
	const json = J.stringify, splice = A.prototype.splice, join = A.prototype.join;
	const AB = ArrayBuffer, U8 = Uint8Array, view = AB.isView;
	const bufferLength = descriptor(AB.prototype, 'byteLength').get;
	const sharedLength =
			typeof SharedArrayBuffer === 'function' ? descriptor(SharedArrayBuffer.prototype, 'byteLength').get : null;
	const typedPrototype = getPrototype(U8.prototype);
	const typedBuffer = descriptor(typedPrototype, 'buffer').get;
	const typedOffset = descriptor(typedPrototype, 'byteOffset').get;
	const typedLength = descriptor(typedPrototype, 'byteLength').get;
	const dataBuffer = descriptor(DV.prototype, 'buffer').get;
	const dataOffset = descriptor(DV.prototype, 'byteOffset').get;
	const dataLength = descriptor(DV.prototype, 'byteLength').get;

	const types = [
		Int8Array, Uint8Array, Uint8ClampedArray, Int16Array, Uint16Array, Int32Array, Uint32Array, Float32Array,
		Float64Array, BigInt64Array, BigUint64Array
	];
	const ops = {
		'+' : (a, b) => a + b,
		'-' : (a, b) => a - b,
		'*' : (a, b) => a * b,
		'/' : (a, b) => a / b,
		'%' : (a, b) => a % b,
		'**' : (a, b) => a ** b,
		'&' : (a, b) => a & b,
		'|' : (a, b) => a | b,
		'^' : (a, b) => a ^ b,
		'<<' : (a, b) => a << b,
		'>>' : (a, b) => a >> b,
		'>>>' : (a, b) => a >>> b,
		'==' : (a, b) => a == b,
		'===' : (a, b) => a === b,
		'!=' : (a, b) => a != b,
		'!==' : (a, b) => a !== b,
		'<' : (a, b) => a < b,
		'<=' : (a, b) => a <= b,
		'>' : (a, b) => a > b,
		'>=' : (a, b) => a >= b,
		'in' : (a, b) => a in b,
		'instanceof' : (a, b) => a instanceof b
	};
	const unary = {
		'+' : a => +a,
		'-' : a => -a,
		'~' : a => ~a,
		'!' : a => !a,
		'typeof' : a => typeof a,
		'void' : a => void a
	};
	const objectPrototype = O.prototype;

	return {
		detachable: a => {
			apply(bufferLength, a, []);
			return true;
		},

		workers: (start, send, end) => {
			const ids = new WM();
			global.Worker = class Worker {
				constructor(url, options = {}) {
					if (typeof url !== 'string' || !url) {
						throw new E('Worker requires an asset or file address');
					}
					const type = options.type === undefined ? 'classic' : options.type;
					if (type !== 'classic' && type !== 'module') {
						throw new E('Unsupported Worker type');
					}
					this.onmessage = null;
					this.onerror = null;
					ids.set(this, start(this, url, type === 'module'));
				}
				postMessage(data, transfer) {
					if (transfer !== undefined && (!A.isArray(transfer) || transfer.length !== 0)) {
						throw new E('Worker transfer lists are not supported');
					}
					send(ids.get(this), data);
				}
				terminate() {
					end(ids.get(this));
					ids.delete(this);
				}
			};
		},

		timers: (start, cancel) => {
			global.setTimeout = (fn, delay = 0, ...args) => start(fn, delay, args, false);
			global.setInterval = (fn, delay = 0, ...args) => start(fn, delay, args, true);
			global.clearTimeout = cancel;
			global.clearInterval = cancel;
			global.queueMicrotask = fn => {
				if (typeof fn !== 'function') {
					throw new E('queueMicrotask requires a function');
				}
				apply(promiseResolve, PromiseClass, []).then(fn);
			};
		},

		plain: a => a !== null && typeof a === 'object'
			&& (getPrototype(a) === objectPrototype || getPrototype(a) === null),
		dataProperty: (a, key, value) => {
			if (!define(a, key, {value, writable: true, configurable: true, enumerable: true})) {
				throw new E('Cannot define data property');
			}
		},
		accessor: (a, key, get, set) => {
			if (!define(a, key, {get, set, configurable: true, enumerable: true})) {
				throw new E('Cannot define host accessor');
			}
		},
		constructor: (host, name, length) => {
			function HostConstructor(...args) {
				if (!new.target) {
					throw new E('Host constructor requires new');
				}
				return apply(host, this, [new.target, ...args]);
			}
			define(HostConstructor, 'name', {value: name, configurable: true});
			define(HostConstructor, 'length', {value: length, configurable: true});
			return HostConstructor;
		},

		keys : a => keys(a),
		ownKeys : a => ownKeys(a),
		hasOwn : (a, k) => apply(hasOwn, a, [ k ]),
		descriptor : (a, k) => descriptor(a, k),
		define : (a, k, d) => define(a, k, d),
		prototype : a => getPrototype(a),
		setPrototype : (a, p) => setPrototype(a, p),
		freeze : a => freeze(a),
		seal : a => seal(a),
		prevent : a => prevent(a),
		extensible : a => extensible(a),
		frozen : a => frozen(a),
		sealed : a => sealed(a),
		same : (a, b) => same(a, b),
		brand : a => apply(tag, a, []),
		json : (a, replacer, space) => json(a, replacer, space),
		string : a => S(a),
		number : a => N(a),
		symbol : a => Sym(a),
		globalSymbol : a => symbolFor(a),
		symbolKey : a => symbolKey(a),
		typed : (t, n) => new types[t](n),
		typedView : (t, b, o, n) => new types[t](b, o, n),
		dataView : (b, o, n) => new DV(b, o, n),
		bytes: a => {
			if (view(a)) {
				let buffer, offset, length;
				try {
					buffer = apply(typedBuffer, a, []);
					offset = apply(typedOffset, a, []);
					length = apply(typedLength, a, []);
				} catch (error) {
					buffer = apply(dataBuffer, a, []);
					offset = apply(dataOffset, a, []);
					length = apply(dataLength, a, []);
				}
				return new U8(buffer, offset, length);
			}

			let length;
			try {
				length = apply(bufferLength, a, []);
			} catch (error) {
				if (!sharedLength) {
					throw error;
				}
				length = apply(sharedLength, a, []);
			}
			return new U8(a, 0, length);
		},
		array : (...a) => a,
		splice : (a, start, count, ...items) => apply(splice, a, [ start, count, ...items ]),
		join : (a, separator) => apply(join, a, [ separator ]),
		invoke : (a, key, ...args) => apply(a[key], a, args),
		iterator : a => a[Sym.iterator](),
		next : (a, v) => a.next(v),
		binary : (op, a, b) => {
			if (!apply(hasOwn, ops, [ op ])) {
				throw new E('Unsupported operator: ' + op);
			}
			return ops[op](a, b);
		},
		unary : (op, a) => {
			if (!apply(hasOwn, unary, [ op ])) {
				throw new E('Unsupported unary operator: ' + op);
			}
			return unary[op](a);
		},
		proxy : (target, handler) => new P(target, handler),
		date : n => new D(n),
		regexp : (pattern, flags) => new RE(pattern, flags),
		map : () => new M(),
		set : () => new T(),
		weakMap : () => new WM(),
		weakSet : () => new WT()
	};
})()
