globalThis.asyncAnswer=0;
globalThis.rejectionHandled=false;
(async()=>{globalThis.asyncAnswer=(await Promise.resolve(21))*2})().catch(e=>{throw e});
Promise.reject(new Error('handled rejection')).catch(()=>{globalThis.rejectionHandled=true});
