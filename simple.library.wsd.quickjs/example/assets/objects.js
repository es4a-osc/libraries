(() => {
 const check=(v,n)=>{if(!v)throw Error(n)};
 const proto={inherited:42}, obj=Object.create(proto);
 Object.defineProperty(obj,'fixed',{value:42});
 check(obj.inherited===42 && Object.getOwnPropertyDescriptor(obj,'fixed').writable===false,'descriptor');
 const p=new Proxy(obj,{get:(o,k)=>k==='answer'?42:Reflect.get(o,k)});
 check(p.answer===42,'proxy');
 const map=new Map([['a',42]]), set=new Set([42,42]), weak=new WeakMap([[obj,42]]);
 check(map.get('a')===42 && set.size===1 && weak.get(obj)===42,'collections');
 check(/你好/u.test('你好😀') && new Date(0).getUTCFullYear()===1970,'regexp/date');
 check([...new Set([1,2,2])].join(',')==='1,2','iterator');
 check(sample.name==='ES4A','host object');
 return true;
})()
