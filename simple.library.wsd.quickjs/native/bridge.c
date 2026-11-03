/*
 * ES4A QuickJS 原生接口：真实 JSValue、运行时隔离、中断、模块、回调与作业队列。
 * QuickJS ABI 固定到 wrapper-android 3.2.3 使用的 f209a8556bc6e4e87cd0fb8c0932b47c5f3eeae5。
 * @author 树先生 xhwsd@qq.com
 */
#include <jni.h>
#define NAN (__builtin_nanf(""))
#include "quickjs.h"
#include "intrinsics.inc"

/* 与 Native.java 保持一致的 JNI 值操作协议。 */
typedef enum {
	EVAL_SCRIPT = 0,
	EVAL_MODULE = 1,
	GET_GLOBAL = 2,
	NEW_UNDEFINED = 3,
	NEW_NULL = 4,
	NEW_BOOLEAN = 5,
	NEW_NUMBER = 6,
	NEW_STRING = 7,
	NEW_BIGINT = 8,
	NEW_OBJECT = 9,
	NEW_ARRAY = 10,
	NEW_BUFFER = 11,
	PARSE_JSON = 12,
	GET_PROPERTY = 13,
	SET_PROPERTY = 14,
	HAS_PROPERTY = 15,
	DELETE_PROPERTY = 16,
	GET_INDEX = 17,
	SET_INDEX = 18,
	CALL = 19,
	CONSTRUCT = 20,
	DUPLICATE = 21,
	CALL_INTRINSIC = 22,
	THROW = 23,
	PROMISE_STATE = 24,
	PROMISE_RESULT = 25,
	MODULE_NAMESPACE = 26,
	ENABLE_EXTENDED_NUMBERS = 27,
	READ_VALUE = 28,
	RUN_BYTECODE = 29,
	EVAL_OPTIONS = 30,
	EVAL_MODULE_OPTIONS = 31,
	LOAD_MODULE = 32,
	DETACH_BUFFER = 33
} Operation;

typedef struct Runtime Runtime;
typedef struct Context Context;
typedef struct Value Value;
typedef struct Module Module;
typedef struct Callback Callback;

struct Runtime {
	JSRuntime *engine;
	JavaVM *vm;
	jobject owner;
	volatile int cancelled;
	int64_t deadline;
	int timeout;
	Callback *callbacks;
	Context *contexts;
};

struct Context {
	Runtime *runtime;
	JSContext *engine;
	jobject owner;
	JSValue helpers;
	Value *values;
	Module *modules;
	Context *next;
	int closed;
	int extended;
};

struct Value {
	Context *context;
	JSValue value;
	Value *next;
	Value *previous;
};

struct Module {
	char *name;
	JSValue namespace;
	Module *next;
};

struct Callback {
	Runtime *runtime;
	Context *context;
	int id;
	Callback *next;
	Callback *previous;
};

static JSClassID callback_class;

struct es4a_timespec {
	long sec;
	long nsec;
};
extern int clock_gettime(int clock, struct es4a_timespec *time);

static size_t text_length(const char *text) {
	size_t length = 0;
	while (text && text[length]) {
		length++;
	}
	return length;
}

static int text_equal(const char *left, const char *right) {
	while (*left && *left == *right) {
		left++;
		right++;
	}
	return *left == *right;
}

static int64_t monotonic_time(void) {
	struct es4a_timespec time;
	if (clock_gettime(1, &time)) {
		return 0;
	}
	return (int64_t)time.sec * 1000000000 + time.nsec;
}

/* JNI UTF-16 与标准 UTF-8 显式往返，保留零字符及补充平面字符。 */
static char *utf8(JNIEnv *env, JSContext *ctx, jstring input, size_t *length) {
	if (!input) {
		if (length) {
			*length = 0;
		}
		return NULL;
	}
	jsize count = (*env)->GetStringLength(env, input);
	const jchar *chars = (*env)->GetStringChars(env, input, NULL);
	if (!chars) {
		return NULL;
	}
	char *result = js_malloc(ctx, (size_t)count * 3 + 1);
	if (!result) {
		(*env)->ReleaseStringChars(env, input, chars);
		return NULL;
	}
	size_t offset = 0;
	for (jsize i = 0; i < count; i++) {
		uint32_t c = chars[i];
		if (c >= 0xd800 && c <= 0xdbff && i + 1 < count && chars[i + 1] >= 0xdc00 && chars[i + 1] <= 0xdfff) {
			c = 0x10000 + ((c - 0xd800) << 10) + (chars[++i] - 0xdc00);
		}
		if (c < 0x80) {
			result[offset++] = c;
		} else if (c < 0x800) {
			result[offset++] = 0xc0 | (c >> 6);
			result[offset++] = 0x80 | (c & 63);
		} else if (c < 0x10000) {
			result[offset++] = 0xe0 | (c >> 12);
			result[offset++] = 0x80 | ((c >> 6) & 63);
			result[offset++] = 0x80 | (c & 63);
		} else {
			result[offset++] = 0xf0 | (c >> 18);
			result[offset++] = 0x80 | ((c >> 12) & 63);
			result[offset++] = 0x80 | ((c >> 6) & 63);
			result[offset++] = 0x80 | (c & 63);
		}
	}
	(*env)->ReleaseStringChars(env, input, chars);
	result[offset] = 0;
	if (length) {
		*length = offset;
	}
	return result;
}

static jstring java_text(JNIEnv *env, const char *input, size_t length) {
	if (!input) {
		return (*env)->NewStringUTF(env, "");
	}
	/* Java 数组分配负责失败报告；不借用宿主 malloc。 */
	jcharArray storage = (*env)->NewCharArray(env, (jsize)length + 1);
	if (!storage) {
		return NULL;
	}
	jchar *chars = (*env)->GetCharArrayElements(env, storage, NULL);
	if (!chars) {
		(*env)->DeleteLocalRef(env, storage);
		return NULL;
	}
	size_t i = 0;
	jsize count = 0;
	while (i < length) {
		uint32_t c = (uint8_t)input[i++];
		if (c >= 0xc0 && c < 0xe0 && i < length) {
			c = ((c & 31) << 6) | ((uint8_t)input[i++] & 63);
		} else if (c >= 0xe0 && c < 0xf0 && i + 1 < length) {
			c = ((c & 15) << 12) | (((uint8_t)input[i] & 63) << 6) | ((uint8_t)input[i + 1] & 63);
			i += 2;
		} else if (c >= 0xf0 && i + 2 < length) {
			c = ((c & 7) << 18) | (((uint8_t)input[i] & 63) << 12) | (((uint8_t)input[i + 1] & 63) << 6) |
					((uint8_t)input[i + 2] & 63);
			i += 3;
		}
		if (c > 0xffff) {
			c -= 0x10000;
			chars[count++] = 0xd800 | (c >> 10);
			chars[count++] = 0xdc00 | (c & 1023);
		} else {
			chars[count++] = (jchar)c;
		}
	}
	jstring result = (*env)->NewString(env, chars, count);
	(*env)->ReleaseCharArrayElements(env, storage, chars, JNI_ABORT);
	(*env)->DeleteLocalRef(env, storage);
	return result;
}

static Value *hold(Context *context, JSValue value) {
	Value *handle = js_malloc(context->engine, sizeof(*handle));
	if (!handle) {
		JS_FreeValue(context->engine, value);
		return NULL;
	}
	handle->context = context;
	handle->value = value;
	handle->previous = NULL;
	handle->next = context->values;
	if (handle->next) {
		handle->next->previous = handle;
	}
	context->values = handle;
	return handle;
}

static void release(Value *value, int free_value) {
	Context *context = value->context;
	if (value->previous) {
		value->previous->next = value->next;
	} else {
		context->values = value->next;
	}
	if (value->next) {
		value->next->previous = value->previous;
	}
	if (free_value) {
		JS_FreeValue(context->engine, value->value);
	}
	js_free(context->engine, value);
}

static JSValue unwrap(jlong pointer) {
	return pointer ? ((Value *)(intptr_t)pointer)->value : JS_UNDEFINED;
}

static void simple_failure(JNIEnv *env, const char *message) {
	jclass type = (*env)->FindClass(env, "simple/library/wsd/quickjs/NativeFailure");
	if (type) {
		(*env)->ThrowNew(env, type, message);
		(*env)->DeleteLocalRef(env, type);
	}
}

static void fail(JNIEnv *env, Context *context) {
	if ((*env)->ExceptionCheck(env)) {
		return;
	}
	JSContext *ctx = context->engine;
	JSValue exception = JS_GetException(ctx);
	size_t length = 0;
	const char *text = JS_ToCStringLen(ctx, &length, exception);
	jstring message = text ? java_text(env, text, length) : (*env)->NewStringUTF(env, "JavaScript operation failed");
	if (text) {
		JS_FreeCString(ctx, text);
	}
	JSValue stack = JS_IsObject(exception) ? JS_GetPropertyStr(ctx, exception, "stack") : JS_UNDEFINED;
	text = JS_IsString(stack) ? JS_ToCStringLen(ctx, &length, stack) : NULL;
	jstring trace = text ? java_text(env, text, length) : (*env)->NewStringUTF(env, "");
	if (text) {
		JS_FreeCString(ctx, text);
	}
	JS_FreeValue(ctx, stack);
	if (JS_HasException(ctx)) {
		JS_FreeValue(ctx, JS_GetException(ctx));
	}
	Value *thrown = hold(context, exception);
	if (JS_HasException(ctx)) {
		JS_FreeValue(ctx, JS_GetException(ctx));
	}
	jclass type = (*env)->FindClass(env, "simple/library/wsd/quickjs/NativeFailure");
	if (type) {
		jmethodID constructor = (*env)->GetMethodID(env, type, "<init>", "(Ljava/lang/String;Ljava/lang/String;IJ)V");
		int reason = context->runtime->timeout
				? 2
				: (__atomic_load_n(&context->runtime->cancelled, __ATOMIC_RELAXED) ? 1 : 0);
		jobject error = (*env)->NewObject(env, type, constructor, message, trace, reason, (jlong)(intptr_t)thrown);
		if (error) {
			(*env)->Throw(env, error);
			(*env)->DeleteLocalRef(env, error);
		}
		(*env)->DeleteLocalRef(env, type);
	}
	(*env)->DeleteLocalRef(env, message);
	(*env)->DeleteLocalRef(env, trace);
}

static jlong result(JNIEnv *env, Context *context, JSValue value) {
	if ((*env)->ExceptionCheck(env)) {
		JS_FreeValue(context->engine, value);
		return 0;
	}
	if (JS_IsException(value)) {
		fail(env, context);
		return 0;
	}
	Value *handle = hold(context, value);
	if (!handle) {
		fail(env, context);
	}
	return (jlong)(intptr_t)handle;
}

static int interrupted(JSRuntime *engine, void *opaque) {
	Runtime *runtime = opaque;
	if (__atomic_load_n(&runtime->cancelled, __ATOMIC_RELAXED)) {
		return 1;
	}
	if (runtime->deadline && monotonic_time() >= runtime->deadline) {
		runtime->timeout = 1;
		return 1;
	}
	return 0;
}

static JNIEnv *current_env(Runtime *runtime) {
	JNIEnv *env = NULL;
	(*runtime->vm)->GetEnv(runtime->vm, (void **)&env, JNI_VERSION_1_6);
	return env;
}

/* 捕获对象随 JS 函数回收，同时解除 Java 对回调对象的登记引用。终结器不得执行 JS。 */
static void callback_finalizer(JSRuntime *engine, JSValue value) {
	Callback *callback = JS_GetOpaque(value, callback_class);
	if (!callback) {
		return;
	}
	Runtime *runtime = callback->runtime;
	if (runtime) {
		if (callback->previous) {
			callback->previous->next = callback->next;
		} else {
			runtime->callbacks = callback->next;
		}
		if (callback->next) {
			callback->next->previous = callback->previous;
		}
	}
	Context *context = callback->context;
	if (context && !context->closed && context->owner) {
		JNIEnv *env = current_env(runtime);
		if (env && !(*env)->ExceptionCheck(env)) {
			jclass type = (*env)->GetObjectClass(env, context->owner);
			if (type) {
				jmethodID method = (*env)->GetMethodID(env, type, "nativeDisposeCallback", "(I)V");
				if (method) {
					(*env)->CallVoidMethod(env, context->owner, method, callback->id);
				}
				(*env)->DeleteLocalRef(env, type);
			}
		}
	}
	js_free_rt(engine, callback);
}

static JSValue java_exception(JNIEnv *env, Context *context) {
	jthrowable error = (*env)->ExceptionOccurred(env);
	(*env)->ExceptionClear(env);
	jclass exported = (*env)->FindClass(env, "simple/library/wsd/quickjs/QuickJS错误");
	if (exported && (*env)->IsInstanceOf(env, error, exported)) {
		jmethodID method =
				(*env)->GetMethodID(env, exported, "nativeThrown", "(Lsimple/library/wsd/quickjs/QuickJS实例;)J");
		jlong pointer = (*env)->CallLongMethod(env, error, method, context->owner);
		if ((*env)->ExceptionCheck(env)) {
			(*env)->ExceptionClear(env);
		}
		if (pointer) {
			Value *handle = (Value *)(intptr_t)pointer;
			JSValue thrown = handle->value;
			release(handle, 0);
			(*env)->DeleteLocalRef(env, exported);
			(*env)->DeleteLocalRef(env, error);
			return JS_Throw(context->engine, thrown);
		}
	}
	if (exported) {
		(*env)->DeleteLocalRef(env, exported);
	}
	jclass type = (*env)->GetObjectClass(env, error);
	jmethodID method = (*env)->GetMethodID(env, type, "toString", "()Ljava/lang/String;");
	jstring message = (jstring)(*env)->CallObjectMethod(env, error, method);
	char *text = utf8(env, context->engine, message, NULL);
	JSValue value = JS_ThrowInternalError(context->engine, "%s", text ? text : "Simple callback failed");
	if (text) {
		js_free(context->engine, text);
	}
	(*env)->DeleteLocalRef(env, message);
	(*env)->DeleteLocalRef(env, type);
	(*env)->DeleteLocalRef(env, error);
	return value;
}

/* 只缓存已实例化的命名空间对象，不保存可能因解析失败而失效的模块指针。 */
static int remember_module(Context *context, const char *name, JSModuleDef *module) {
	JSValue namespace = JS_GetModuleNamespace(context->engine, module);
	if (JS_IsException(namespace)) {
		return -1;
	}
	Module *entry = context->modules;
	while (entry && !text_equal(entry->name, name)) {
		entry = entry->next;
	}
	if (!entry) {
		entry = js_malloc(context->engine, sizeof(*entry));
		if (!entry) {
			JS_FreeValue(context->engine, namespace);
			return -1;
		}
		entry->name = js_strdup(context->engine, name);
		if (!entry->name) {
			js_free(context->engine, entry);
			JS_FreeValue(context->engine, namespace);
			return -1;
		}
		entry->namespace = JS_UNDEFINED;
		entry->next = context->modules;
		context->modules = entry;
	}
	JS_FreeValue(context->engine, entry->namespace);
	entry->namespace = namespace;
	return 0;
}

/* 为源码、字节码和宿主模块统一设置元信息，再允许宿主加载器补充。 */
static int initialize_meta(Context *context, JSModuleDef *module, JSValueConst options, int entry) {
	JSContext *ctx = context->engine;
	JNIEnv *env = current_env(context->runtime);
	JSAtom atom = JS_GetModuleName(ctx, module);
	const char *name = JS_AtomToCString(ctx, atom);
	JS_FreeAtom(ctx, atom);
	if (!name) {
		return -1;
	}

	JSValue meta = JS_GetImportMeta(ctx, module);
	if (JS_IsException(meta)) {
		JS_FreeCString(ctx, name);
		return -1;
	}
	JSValue url = JS_UNDEFINED, main = JS_NewBool(ctx, entry);
	if (entry && JS_IsObject(options)) {
		url = JS_GetPropertyStr(ctx, options, "url");
		JS_FreeValue(ctx, main);
		main = JS_GetPropertyStr(ctx, options, "main");
	}
	if (JS_IsUndefined(url) || JS_IsString(url) && JS_ToBool(ctx, url) == 0) {
		JS_FreeValue(ctx, url);
		url = JS_NewString(ctx, name);
	}
	if (JS_IsException(url) || JS_IsException(main)) {
		JS_FreeValue(ctx, url);
		JS_FreeValue(ctx, main);
		JS_FreeValue(ctx, meta);
		JS_FreeCString(ctx, name);
		return -1;
	}
	int first = JS_SetPropertyStr(ctx, meta, "url", url);
	int second = JS_SetPropertyStr(ctx, meta, "main", main);
	if (first < 0 || second < 0) {
		JS_FreeValue(ctx, meta);
		JS_FreeCString(ctx, name);
		return -1;
	}

	Value *reference = hold(context, meta);
	if (!reference) {
		JS_FreeCString(ctx, name);
		return -1;
	}
	jclass type = (*env)->GetObjectClass(env, context->owner);
	jmethodID method = (*env)->GetMethodID(env, type, "nativeMeta", "(JLjava/lang/String;)V");
	jstring module_name = java_text(env, name, text_length(name));
	(*env)->CallVoidMethod(env, context->owner, method, (jlong)(intptr_t)reference, module_name);
	(*env)->DeleteLocalRef(env, module_name);
	(*env)->DeleteLocalRef(env, type);
	JS_FreeCString(ctx, name);
	if ((*env)->ExceptionCheck(env)) {
		java_exception(env, context);
		return -1;
	}
	return 0;
}

/* 宿主模块初始化只消费已保存快照的引用，成员名在模块创建时固定。 */
static int initialize_host_module(JSContext *ctx, JSModuleDef *module) {
	Context *context = JS_GetContextOpaque(ctx);
	JNIEnv *env = current_env(context->runtime);
	if (context->closed) {
		JS_ThrowReferenceError(ctx, "Context was closed");
		return -1;
	}
	JSAtom atom = JS_GetModuleName(ctx, module);
	const char *name = JS_AtomToCString(ctx, atom);
	JS_FreeAtom(ctx, atom);
	if (!name) {
		return -1;
	}
	jstring module_name = java_text(env, name, text_length(name));
	JS_FreeCString(ctx, name);
	jclass type = (*env)->GetObjectClass(env, context->owner);
	jmethodID names_method =
			(*env)->GetMethodID(env, type, "nativeModuleNames", "(Ljava/lang/String;)[Ljava/lang/String;");
	jmethodID value_method =
			(*env)->GetMethodID(env, type, "nativeModuleValue", "(Ljava/lang/String;Ljava/lang/String;)J");
	jobjectArray names = (*env)->CallObjectMethod(env, context->owner, names_method, module_name);
	int result_code = 0;
	if (!(*env)->ExceptionCheck(env) && names) {
		jsize count = (*env)->GetArrayLength(env, names);
		for (jsize i = 0; i < count; i++) {
			jstring key = (*env)->GetObjectArrayElement(env, names, i);
			jlong pointer = (*env)->ExceptionCheck(env) ? 0 :
					(*env)->CallLongMethod(env, context->owner, value_method, module_name, key);
			char *text = (*env)->ExceptionCheck(env) ? NULL : utf8(env, ctx, key, NULL);
			(*env)->DeleteLocalRef(env, key);
			if ((*env)->ExceptionCheck(env) || !pointer || !text) {
				if (pointer) {
					release((Value *)(intptr_t)pointer, 1);
				}
				if (text) {
					js_free(ctx, text);
				}
				result_code = -1;
				break;
			}
			Value *reference = (Value *)(intptr_t)pointer;
			JSValue value = reference->value;
			release(reference, 0);
			result_code = JS_SetModuleExport(ctx, module, text, value);
			js_free(ctx, text);
			if (result_code < 0) {
				break;
			}
		}
	} else {
		result_code = -1;
	}
	(*env)->DeleteLocalRef(env, names);
	(*env)->DeleteLocalRef(env, module_name);
	(*env)->DeleteLocalRef(env, type);
	if ((*env)->ExceptionCheck(env)) {
		java_exception(env, context);
		return -1;
	}
	return result_code;
}

static JSModuleDef *create_host_module(Context *context, const char *name) {
	JSContext *ctx = context->engine;
	JNIEnv *env = current_env(context->runtime);
	JSModuleDef *module = JS_NewCModule(ctx, name, initialize_host_module);
	if (!module) {
		return NULL;
	}
	jclass type = (*env)->GetObjectClass(env, context->owner);
	jmethodID method = (*env)->GetMethodID(env, type, "nativeModuleNames", "(Ljava/lang/String;)[Ljava/lang/String;");
	jstring module_name = java_text(env, name, text_length(name));
	jobjectArray names = (*env)->CallObjectMethod(env, context->owner, method, module_name);
	int code = 0;
	if (!(*env)->ExceptionCheck(env) && names) {
		for (jsize i = 0; i < (*env)->GetArrayLength(env, names); i++) {
			jstring key = (*env)->GetObjectArrayElement(env, names, i);
			char *text = utf8(env, ctx, key, NULL);
			(*env)->DeleteLocalRef(env, key);
			if (!text) {
				code = -1;
				break;
			}
			code = JS_AddModuleExport(ctx, module, text);
			js_free(ctx, text);
			if (code < 0) {
				break;
			}
		}
	} else {
		code = -1;
	}
	(*env)->DeleteLocalRef(env, names);
	(*env)->DeleteLocalRef(env, module_name);
	(*env)->DeleteLocalRef(env, type);
	if ((*env)->ExceptionCheck(env)) {
		java_exception(env, context);
		return NULL;
	}
	if (code < 0 || initialize_meta(context, module, JS_UNDEFINED, 0) < 0) {
		return NULL;
	}
	return module;
}

static char *normalize_module(JSContext *ctx, const char *base, const char *name, void *opaque) {
	Context *context = JS_GetContextOpaque(ctx);
	JNIEnv *env = current_env(context->runtime);
	if (context->closed) {
		JS_ThrowReferenceError(ctx, "Context was closed");
		return NULL;
	}
	jclass type = (*env)->GetObjectClass(env, context->owner);
	jmethodID method = (*env)->GetMethodID(
			env, type, "nativeNormalize", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
	jstring b = java_text(env, base, text_length(base)), n = java_text(env, name, text_length(name));
	jstring output = (*env)->CallObjectMethod(env, context->owner, method, b, n);
	char *result = NULL;
	if ((*env)->ExceptionCheck(env)) {
		java_exception(env, context);
	} else {
		result = utf8(env, ctx, output, NULL);
	}
	(*env)->DeleteLocalRef(env, b);
	(*env)->DeleteLocalRef(env, n);
	(*env)->DeleteLocalRef(env, output);
	(*env)->DeleteLocalRef(env, type);
	return result;
}

static JSModuleDef *load_module(JSContext *ctx, const char *name, void *opaque) {
	Context *context = JS_GetContextOpaque(ctx);
	JNIEnv *env = current_env(context->runtime);
	if (context->closed) {
		JS_ThrowReferenceError(ctx, "Context was closed");
		return NULL;
	}
	jclass type = (*env)->GetObjectClass(env, context->owner);
	jmethodID method = (*env)->GetMethodID(env, type, "nativeLoad", "(Ljava/lang/String;)[B");
	jstring n = java_text(env, name, text_length(name));
	jbyteArray output = (*env)->CallObjectMethod(env, context->owner, method, n);
	JSValue compiled = JS_EXCEPTION;
	JSModuleDef *module = NULL;
	if ((*env)->ExceptionCheck(env)) {
		java_exception(env, context);
	} else if (!output || (*env)->GetArrayLength(env, output) < 1) {
		JS_ThrowReferenceError(ctx, "Module not found: %s", name);
	} else {
		jsize size = (*env)->GetArrayLength(env, output);
		jbyte *bytes = (*env)->GetByteArrayElements(env, output, NULL);
		if (bytes) {
			if (bytes[0] == 2) {
				module = create_host_module(context, name);
			} else if (bytes[0]) {
				compiled = JS_ReadObject(ctx, (uint8_t *)bytes + 1, size - 1, JS_READ_OBJ_BYTECODE);
			} else {
				char *source = js_malloc(ctx, size);
				if (source) {
					for (int i = 1; i < size; i++) {
						source[i - 1] = bytes[i];
					}
					source[size - 1] = 0;
					compiled = JS_Eval(ctx, source, size - 1, name, JS_EVAL_TYPE_MODULE | JS_EVAL_FLAG_COMPILE_ONLY);
					js_free(ctx, source);
				}
			}
			(*env)->ReleaseByteArrayElements(env, output, bytes, JNI_ABORT);
		}
		if (!JS_IsException(compiled)) {
			if (JS_VALUE_GET_TAG(compiled) != JS_TAG_MODULE) {
				JS_ThrowTypeError(ctx, "Expected module bytecode");
			} else {
				module = JS_VALUE_GET_PTR(compiled);
				if (initialize_meta(context, module, JS_UNDEFINED, 0) < 0) {
					module = NULL;
				}
			}
			JS_FreeValue(ctx, compiled);
		}
	}
	(*env)->DeleteLocalRef(env, output);
	(*env)->DeleteLocalRef(env, n);
	(*env)->DeleteLocalRef(env, type);
	return module;
}

static JSValue host_call(
		JSContext *ctx, JSValueConst receiver, int argc, JSValueConst *argv, int magic, JSValue *data) {
	Context *context = JS_GetContextOpaque(ctx);
	JNIEnv *env = current_env(context->runtime);
	if (context->closed) {
		return JS_ThrowReferenceError(ctx, "Context was closed");
	}
	Callback *callback = JS_GetOpaque(data[0], callback_class);
	if (!callback || !callback->context || !callback->context->owner) {
		return JS_ThrowReferenceError(ctx, "Callback context was closed");
	}
	Context *origin = callback->context;
	jlongArray args = (*env)->NewLongArray(env, argc);
	if (!args) {
		return JS_ThrowOutOfMemory(ctx);
	}
	jlong *pointers = argc ? (*env)->GetLongArrayElements(env, args, NULL) : NULL;
	if (argc && !pointers) {
		(*env)->DeleteLocalRef(env, args);
		return JS_ThrowOutOfMemory(ctx);
	}
	int retained = 0;
	for (; retained < argc; retained++) {
		pointers[retained] = (jlong)(intptr_t)hold(origin, JS_DupValue(ctx, argv[retained]));
		if (!pointers[retained]) {
			break;
		}
	}
	Value *this_value = retained == argc ? hold(origin, JS_DupValue(ctx, receiver)) : NULL;
	if (!this_value) {
		for (int i = 0; i < retained; i++) {
			release((Value *)(intptr_t)pointers[i], 1);
		}
		if (pointers) {
			(*env)->ReleaseLongArrayElements(env, args, pointers, JNI_ABORT);
		}
		(*env)->DeleteLocalRef(env, args);
		return JS_EXCEPTION;
	}
	if (pointers) {
		(*env)->ReleaseLongArrayElements(env, args, pointers, 0);
	}
	jclass type = (*env)->GetObjectClass(env, origin->owner);
	jmethodID method = (*env)->GetMethodID(env, type, "nativeCall", "(IJ[J)J");
	jlong returned =
			(*env)->CallLongMethod(env, origin->owner, method, callback->id, (jlong)(intptr_t)this_value, args);
	(*env)->DeleteLocalRef(env, args);
	(*env)->DeleteLocalRef(env, type);
	if ((*env)->ExceptionCheck(env)) {
		return java_exception(env, context);
	}
	if (!returned) {
		return JS_UNDEFINED;
	}
	Value *handle = (Value *)(intptr_t)returned;
	JSValue value = handle->value;
	release(handle, 0);
	return value;
}

static void rejected(JSContext *ctx, JSValueConst promise, JSValueConst reason, JS_BOOL handled, void *opaque) {
	Context *context = JS_GetContextOpaque(ctx);
	JNIEnv *env = current_env(context->runtime);
	if (context->closed) {
		return;
	}
	jclass type = (*env)->GetObjectClass(env, context->owner);
	jmethodID method = (*env)->GetMethodID(env, type, "nativeRejected", "(JJZ)V");
	Value *p = hold(context, JS_DupValue(ctx, promise)), *r = hold(context, JS_DupValue(ctx, reason));
	(*env)->CallVoidMethod(env, context->owner, method, (jlong)(intptr_t)p, (jlong)(intptr_t)r, (jboolean)handled);
	(*env)->DeleteLocalRef(env, type);
	if ((*env)->ExceptionCheck(env)) {
		__atomic_store_n(&context->runtime->cancelled, 1, __ATOMIC_RELAXED);
	}
}

JNIEXPORT jlong JNICALL Java_simple_library_wsd_quickjs_Native_runtime(JNIEnv *env, jclass type, jobject owner) {
	/* Java 的同步入口确保全局 class ID 只登记一次；每个运行时分别安装类定义。 */
	if (!callback_class) {
		JS_NewClassID(&callback_class);
	}
	JSRuntime *engine = JS_NewRuntime();
	if (!engine) {
		simple_failure(env, "Unable to allocate QuickJS runtime");
		return 0;
	}
	JSClassDef callback_definition = {.class_name = "ES4AHostCallback", .finalizer = callback_finalizer};
	if (JS_NewClass(engine, callback_class, &callback_definition) < 0) {
		JS_FreeRuntime(engine);
		simple_failure(env, "Unable to register callback finalizer");
		return 0;
	}
	Runtime *runtime = js_malloc_rt(engine, sizeof(*runtime));
	if (!runtime) {
		JS_FreeRuntime(engine);
		simple_failure(env, "Unable to allocate runtime state");
		return 0;
	}
	runtime->engine = engine;
	runtime->owner = (*env)->NewGlobalRef(env, owner);
	runtime->cancelled = 0;
	runtime->deadline = 0;
	runtime->timeout = 0;
	runtime->callbacks = NULL;
	runtime->contexts = NULL;
	if (!runtime->owner) {
		js_free_rt(engine, runtime);
		JS_FreeRuntime(engine);
		return 0;
	}
	(*env)->GetJavaVM(env, &runtime->vm);
	JS_SetInterruptHandler(engine, interrupted, runtime);
	JS_SetModuleLoaderFunc(engine, normalize_module, load_module, runtime);
	JS_SetHostPromiseRejectionTracker(engine, rejected, runtime);
	return (jlong)(intptr_t)runtime;
}

JNIEXPORT jlong JNICALL Java_simple_library_wsd_quickjs_Native_context(
		JNIEnv *env, jclass type, jlong pointer, jobject owner) {
	Runtime *runtime = (Runtime *)(intptr_t)pointer;
	JSContext *engine = JS_NewContext(runtime->engine);
	if (!engine) {
		simple_failure(env, "Unable to allocate QuickJS context");
		return 0;
	}
	Context *context = js_malloc(engine, sizeof(*context));
	if (!context) {
		JS_FreeContext(engine);
		simple_failure(env, "Unable to allocate context state");
		return 0;
	}
	context->runtime = runtime;
	context->engine = engine;
	context->owner = (*env)->NewGlobalRef(env, owner);
	context->values = NULL;
	context->modules = NULL;
	context->closed = 0;
	context->extended = 0;
	if (!context->owner) {
		js_free(engine, context);
		JS_FreeContext(engine);
		return 0;
	}
	JS_SetContextOpaque(engine, context);
	context->helpers =
			JS_Eval(engine, ES4A_INTRINSICS, sizeof(ES4A_INTRINSICS) - 1, "es4a:intrinsics", JS_EVAL_TYPE_GLOBAL);
	if (JS_IsException(context->helpers)) {
		JS_FreeValue(engine, JS_GetException(engine));
		(*env)->DeleteGlobalRef(env, context->owner);
		js_free(engine, context);
		JS_FreeContext(engine);
		simple_failure(env, "Unable to initialize context intrinsics");
		return 0;
	}
	context->next = runtime->contexts;
	runtime->contexts = context;
	return (jlong)(intptr_t)context;
}

JNIEXPORT void JNICALL Java_simple_library_wsd_quickjs_Native_begin(
		JNIEnv *env, jclass type, jlong pointer, jlong milliseconds) {
	Runtime *runtime = (Runtime *)(intptr_t)pointer;
	JS_UpdateStackTop(runtime->engine);
	runtime->timeout = 0;
	__atomic_store_n(&runtime->cancelled, 0, __ATOMIC_RELAXED);
	runtime->deadline = milliseconds ? monotonic_time() + milliseconds * 1000000 : 0;
}

JNIEXPORT void JNICALL Java_simple_library_wsd_quickjs_Native_cancel(JNIEnv *env, jclass type, jlong pointer) {
	__atomic_store_n(&((Runtime *)(intptr_t)pointer)->cancelled, 1, __ATOMIC_RELAXED);
}

JNIEXPORT void JNICALL Java_simple_library_wsd_quickjs_Native_stop(JNIEnv *env, jclass type, jlong pointer) {
	Runtime *runtime = (Runtime *)(intptr_t)pointer;
	runtime->deadline = 0;
	runtime->timeout = 0;
	__atomic_store_n(&runtime->cancelled, 0, __ATOMIC_RELAXED);
}

JNIEXPORT void JNICALL Java_simple_library_wsd_quickjs_Native_configure(
		JNIEnv *env, jclass type, jlong pointer, jint operation, jlong value) {
	Runtime *runtime = (Runtime *)(intptr_t)pointer;
	if (value < 0 || (uint64_t)value > (size_t)-1) {
		simple_failure(env, "Limit is outside the native address range");
		return;
	}
	if (operation == 0) {
		JS_SetMemoryLimit(runtime->engine, value ? (size_t)value : (size_t)-1);
	} else if (operation == 1) {
		JS_SetMaxStackSize(runtime->engine, value);
	} else if (operation == 2) {
		JS_SetGCThreshold(runtime->engine, value);
	} else if (operation == 3) {
		JS_RunGC(runtime->engine);
	} else if (operation == 4) {
		JS_SetCanBlock(runtime->engine, value != 0);
	}
}

JNIEXPORT jlong JNICALL Java_simple_library_wsd_quickjs_Native_value(JNIEnv *env, jclass type, jlong pointer, jint op,
		jlong left, jlong right, jstring text, jstring name, jlong index, jdouble number, jlongArray arguments,
		jbyteArray binary) {
	Context *context = (Context *)(intptr_t)pointer;
	JSContext *ctx = context->engine;
	JSValue a = unwrap(left), b = unwrap(right), value = JS_UNDEFINED;
	size_t length = 0;
	char *source = utf8(env, ctx, text, &length), *filename = utf8(env, ctx, name, NULL);
	if ((text && !source) || (name && !filename)) {
		if (source) {
			js_free(ctx, source);
		}
		if (filename) {
			js_free(ctx, filename);
		}
		if (!(*env)->ExceptionCheck(env)) {
			fail(env, context);
		}
		return 0;
	}
	jsize count = arguments ? (*env)->GetArrayLength(env, arguments) : 0;
	JSValue *args = count ? js_malloc(ctx, sizeof(JSValue) * (size_t)count) : NULL;
	if (count && !args) {
		if (source) {
			js_free(ctx, source);
		}
		if (filename) {
			js_free(ctx, filename);
		}
		fail(env, context);
		return 0;
	}
	if (count) {
		jlong *input = (*env)->GetLongArrayElements(env, arguments, NULL);
		if (!input) {
			js_free(ctx, args);
			if (source) {
				js_free(ctx, source);
			}
			if (filename) {
				js_free(ctx, filename);
			}
			return 0;
		}
		for (int i = 0; i < count; i++) {
			args[i] = unwrap(input[i]);
		}
		(*env)->ReleaseLongArrayElements(env, arguments, input, JNI_ABORT);
	}
	a = JS_DupValue(ctx, a);
	b = JS_DupValue(ctx, b);
	for (int i = 0; i < count; i++) {
		args[i] = JS_DupValue(ctx, args[i]);
	}

	switch (op) {
	case EVAL_SCRIPT:
		value = JS_Eval(ctx, source, length, filename ? filename : "script", JS_EVAL_TYPE_GLOBAL);
		break;
	case EVAL_OPTIONS: {
		JSValue receiver = left ? JS_DupValue(ctx, a) : JS_GetGlobalObject(ctx);
		value = JS_EvalThis(ctx, receiver, source, length, filename ? filename : "script",
				JS_EVAL_TYPE_GLOBAL | (index & (8 | 64 | 128)));
		JS_FreeValue(ctx, receiver);
		break;
	}
	case LOAD_MODULE:
		value = JS_LoadModule(ctx, filename ? filename : "", source);
		break;
	case DETACH_BUFFER: {
		JSValue checker = JS_GetPropertyStr(ctx, context->helpers, "detachable");
		JSValue valid = JS_Call(ctx, checker, JS_UNDEFINED, 1, &a);
		JS_FreeValue(ctx, checker);
		if (JS_IsException(valid)) {
			value = valid;
		} else {
			JS_FreeValue(ctx, valid);
			JS_DetachArrayBuffer(ctx, a);
			if (JS_HasException(ctx)) {
				value = JS_EXCEPTION;
			}
		}
		break;
	}
	case EVAL_MODULE:
	case EVAL_MODULE_OPTIONS: {
		JSValue compiled = JS_Eval(ctx, source, length, filename ? filename : "module",
				JS_EVAL_TYPE_MODULE | JS_EVAL_FLAG_COMPILE_ONLY | (op == EVAL_MODULE_OPTIONS ? index & (8 | 64) : 0));
		if (!JS_IsException(compiled)) {
			JSModuleDef *module = JS_VALUE_GET_PTR(compiled);
			if (initialize_meta(context, module, a, 1) < 0) {
				JS_FreeValue(ctx, compiled);
				value = JS_EXCEPTION;
			} else {
				value = JS_EvalFunction(ctx, compiled);
			}
			if (!JS_IsException(value) && remember_module(context, filename ? filename : "module", module) < 0) {
				JS_FreeValue(ctx, value);
				value = JS_EXCEPTION;
			}
		} else {
			value = compiled;
		}
		break;
	}
	case GET_GLOBAL:
		value = JS_GetGlobalObject(ctx);
		break;
	case NEW_UNDEFINED:
		value = JS_UNDEFINED;
		break;
	case NEW_NULL:
		value = JS_NULL;
		break;
	case NEW_BOOLEAN:
		value = JS_NewBool(ctx, index != 0);
		break;
	case NEW_NUMBER:
		value = JS_NewFloat64(ctx, number);
		break;
	case NEW_STRING:
		value = JS_NewStringLen(ctx, source ? source : "", length);
		break;
	case NEW_BIGINT: {
		char *literal = js_malloc(ctx, length + 2);
		if (!literal) {
			value = JS_EXCEPTION;
			break;
		}
		for (size_t i = 0; i < length; i++) {
			literal[i] = source[i];
		}
		literal[length] = 'n';
		literal[length + 1] = 0;
		value = JS_Eval(ctx, literal, length + 1, "bigint", 0);
		js_free(ctx, literal);
		break;
	}
	case NEW_OBJECT:
		value = JS_NewObject(ctx);
		break;
	case NEW_ARRAY:
		value = JS_NewArray(ctx);
		break;
	case NEW_BUFFER: {
		jsize size = binary ? (*env)->GetArrayLength(env, binary) : 0;
		jbyte *bytes = binary ? (*env)->GetByteArrayElements(env, binary, NULL) : NULL;
		if (binary && !bytes) {
			value = JS_EXCEPTION;
			break;
		}
		value = JS_NewArrayBufferCopy(ctx, (uint8_t *)bytes, size);
		if (bytes) {
			(*env)->ReleaseByteArrayElements(env, binary, bytes, JNI_ABORT);
		}
		break;
	}
	case PARSE_JSON:
		value = JS_ParseJSON(ctx, source, length, filename ? filename : "json");
		break;
	case GET_PROPERTY:
	case SET_PROPERTY:
	case HAS_PROPERTY:
	case DELETE_PROPERTY: {
		JSAtom key = source ? JS_NewAtomLen(ctx, source, length) : JS_ValueToAtom(ctx, count ? args[0] : JS_UNDEFINED);
		if (key == JS_ATOM_NULL) {
			value = JS_EXCEPTION;
			break;
		}
		if (op == GET_PROPERTY) {
			value = JS_GetProperty(ctx, a, key);
		} else {
			int code = op == SET_PROPERTY ? JS_SetProperty(ctx, a, key, JS_DupValue(ctx, b))
					: op == HAS_PROPERTY  ? JS_HasProperty(ctx, a, key)
										  : JS_DeleteProperty(ctx, a, key, 0);
			value = code < 0 ? JS_EXCEPTION : JS_NewBool(ctx, code);
		}
		JS_FreeAtom(ctx, key);
		break;
	}
	case GET_INDEX:
		value = JS_GetPropertyUint32(ctx, a, (uint32_t)index);
		break;
	case SET_INDEX: {
		int code = JS_SetPropertyUint32(ctx, a, (uint32_t)index, JS_DupValue(ctx, b));
		value = code < 0 ? JS_EXCEPTION : JS_NewBool(ctx, code);
		break;
	}
	case CALL:
		value = JS_Call(ctx, a, b, count, args);
		break;
	case CONSTRUCT:
		value = JS_CallConstructor(ctx, a, count, args);
		break;
	case DUPLICATE:
		value = JS_DupValue(ctx, a);
		break;
	case CALL_INTRINSIC: {
		JSValue function = JS_GetPropertyStr(ctx, context->helpers, source);
		value = JS_Call(ctx, function, JS_UNDEFINED, count, args);
		JS_FreeValue(ctx, function);
		break;
	}
	case THROW:
		value = JS_Throw(ctx, JS_DupValue(ctx, a));
		break;
	case PROMISE_STATE:
		value = JS_NewInt32(ctx, JS_PromiseState(ctx, a));
		break;
	case PROMISE_RESULT:
		value = JS_PromiseResult(ctx, a);
		break;
	case MODULE_NAMESPACE: {
		Module *module = context->modules;
		while (module && !text_equal(module->name, source)) {
			module = module->next;
		}
		value = module ? JS_DupValue(ctx, module->namespace)
					   : JS_ThrowReferenceError(ctx, "Module has not been loaded: %s", source);
		break;
	}
	case ENABLE_EXTENDED_NUMBERS:
		if (context->extended < 0) {
			value = JS_ThrowInternalError(ctx, "Extended numeric initialization failed; create a new context");
			break;
		}
		if (!context->extended) {
			JS_AddIntrinsicBigFloat(ctx);
			JS_AddIntrinsicBigDecimal(ctx);
			JS_AddIntrinsicOperators(ctx);
			JS_EnableBignumExt(ctx, 1);
			context->extended = JS_HasException(ctx) ? -1 : 1;
			if (context->extended < 0) {
				value = JS_EXCEPTION;
			}
		}
		break;
	case READ_VALUE:
	case RUN_BYTECODE: {
		jsize size = (*env)->GetArrayLength(env, binary);
		jbyte *bytes = (*env)->GetByteArrayElements(env, binary, NULL);
		if (!bytes) {
			break;
		}
		value = JS_ReadObject(
				ctx, (uint8_t *)bytes, size, op == RUN_BYTECODE ? JS_READ_OBJ_BYTECODE : JS_READ_OBJ_REFERENCE);
		(*env)->ReleaseByteArrayElements(env, binary, bytes, JNI_ABORT);
		if (op == RUN_BYTECODE && !JS_IsException(value)) {
			JSModuleDef *module = JS_VALUE_GET_TAG(value) == JS_TAG_MODULE ? JS_VALUE_GET_PTR(value) : NULL;
			if (module && JS_ResolveModule(ctx, value) < 0) {
				value = JS_EXCEPTION;
			} else {
				JSAtom atom = module ? JS_GetModuleName(ctx, module) : JS_ATOM_NULL;
				const char *module_name = module ? JS_AtomToCString(ctx, atom) : NULL;
				if (module && initialize_meta(context, module, JS_UNDEFINED, 1) < 0) {
					JS_FreeValue(ctx, value);
					value = JS_EXCEPTION;
				} else {
					value = JS_EvalFunction(ctx, value);
				}
				if (module_name && !JS_IsException(value) && remember_module(context, module_name, module) < 0) {
					JS_FreeValue(ctx, value);
					value = JS_EXCEPTION;
				}
				if (module_name) {
					JS_FreeCString(ctx, module_name);
				}
				if (atom) {
					JS_FreeAtom(ctx, atom);
				}
			}
		}
		break;
	}
	default:
		value = JS_ThrowInternalError(ctx, "Unknown native operation");
	}
	JS_FreeValue(ctx, a);
	JS_FreeValue(ctx, b);
	for (int i = 0; i < count; i++) {
		JS_FreeValue(ctx, args[i]);
	}

	if (source) {
		js_free(ctx, source);
	}
	if (filename) {
		js_free(ctx, filename);
	}
	if (args) {
		js_free(ctx, args);
	}
	return result(env, context, value);
}

JNIEXPORT jint JNICALL Java_simple_library_wsd_quickjs_Native_kind(
		JNIEnv *env, jclass type, jlong pointer, jlong handle) {
	Context *context = (Context *)(intptr_t)pointer;
	JSValue value = unwrap(handle);
	JSContext *ctx = context->engine;
	int tag = JS_VALUE_GET_NORM_TAG(value);
	if (tag == JS_TAG_UNDEFINED) {
		return 0;
	}
	if (tag == JS_TAG_NULL) {
		return 1;
	}
	if (tag == JS_TAG_BOOL) {
		return 2;
	}
	if (tag == JS_TAG_INT || tag == JS_TAG_FLOAT64) {
		return 3;
	}
	if (tag == JS_TAG_STRING) {
		return 4;
	}
	if (tag == JS_TAG_BIG_INT) {
		return 5;
	}
	if (tag == JS_TAG_SYMBOL) {
		return 6;
	}
	if (tag == JS_TAG_BIG_FLOAT) {
		return 13;
	}
	if (tag == JS_TAG_BIG_DECIMAL) {
		return 14;
	}
	if (JS_IsFunction(ctx, value)) {
		return 9;
	}
	int array = JS_IsArray(ctx, value);
	if (array > 0) {
		return 8;
	}
	if (array < 0) {
		JS_FreeValue(ctx, JS_GetException(ctx));
	}
	if (JS_PromiseState(ctx, value) >= 0) {
		return 10;
	}
	if (JS_IsObject(value)) {
		size_t size;
		JS_GetArrayBuffer(ctx, &size, value);
		if (!JS_HasException(ctx)) {
			return 11;
		}
		JS_FreeValue(ctx, JS_GetException(ctx));
		JSValue buffer = JS_GetTypedArrayBuffer(ctx, value, NULL, NULL, NULL);
		if (!JS_IsException(buffer)) {
			JS_FreeValue(ctx, buffer);
			return 12;
		}
		JS_FreeValue(ctx, JS_GetException(ctx));
	}
	return 7;
}

JNIEXPORT jstring JNICALL Java_simple_library_wsd_quickjs_Native_string(
		JNIEnv *env, jclass type, jlong pointer, jlong handle) {
	Context *context = (Context *)(intptr_t)pointer;
	size_t size;
	const char *text = JS_ToCStringLen2(context->engine, &size, unwrap(handle), 1);
	if (!text) {
		fail(env, context);
		return NULL;
	}
	jstring result = java_text(env, text, size);
	JS_FreeCString(context->engine, text);
	return result;
}

JNIEXPORT jdouble JNICALL Java_simple_library_wsd_quickjs_Native_number(
		JNIEnv *env, jclass type, jlong pointer, jlong handle) {
	Context *context = (Context *)(intptr_t)pointer;
	double value = 0;
	if (JS_ToFloat64(context->engine, &value, unwrap(handle)) < 0) {
		fail(env, context);
	}
	return value;
}

JNIEXPORT jboolean JNICALL Java_simple_library_wsd_quickjs_Native_bool(
		JNIEnv *env, jclass type, jlong pointer, jlong handle) {
	Context *context = (Context *)(intptr_t)pointer;
	return JS_ToBool(context->engine, unwrap(handle)) != 0;
}

JNIEXPORT jbyteArray JNICALL Java_simple_library_wsd_quickjs_Native_bytes(
		JNIEnv *env, jclass type, jlong pointer, jlong handle) {
	Context *context = (Context *)(intptr_t)pointer;
	JSContext *ctx = context->engine;
	size_t size = 0, offset = 0, length = 0;
	JSValue buffer = JS_GetTypedArrayBuffer(ctx, unwrap(handle), &offset, &length, NULL);
	if (JS_IsException(buffer)) {
		fail(env, context);
		return NULL;
	}
	uint8_t *data = JS_GetArrayBuffer(ctx, &size, buffer);
	if (length > 0x7fffffff || offset > size || length > size - offset) {
		JS_FreeValue(ctx, buffer);
		simple_failure(env, "Buffer is too large");
		return NULL;
	}
	jbyteArray result = (*env)->NewByteArray(env, length);
	if (result && length) {
		(*env)->SetByteArrayRegion(env, result, 0, length, (jbyte *)data + offset);
	}
	JS_FreeValue(ctx, buffer);
	return result;
}

JNIEXPORT jbyteArray JNICALL Java_simple_library_wsd_quickjs_Native_write(JNIEnv *env, jclass type, jlong pointer,
		jlong handle, jstring source, jstring filename, jint mode, jint flags) {
	Context *context = (Context *)(intptr_t)pointer;
	JSContext *ctx = context->engine;
	JSValue value = unwrap(handle);
	int temporary = source != NULL;
	if (temporary) {
		size_t size;
		char *text = utf8(env, ctx, source, &size), *name = utf8(env, ctx, filename, NULL);
		if (!text || (filename && !name)) {
			if (text) {
				js_free(ctx, text);
			}
			if (name) {
				js_free(ctx, name);
			}
			if (!(*env)->ExceptionCheck(env)) {
				fail(env, context);
			}
			return NULL;
		}
		value = JS_Eval(ctx, text, size, name ? name : "script",
				(mode == 2 ? JS_EVAL_TYPE_MODULE : JS_EVAL_TYPE_GLOBAL) | JS_EVAL_FLAG_COMPILE_ONLY |
						(flags & (mode == 2 ? (8 | 64) : (8 | 64 | 128))));
		js_free(ctx, text);
		if (name) {
			js_free(ctx, name);
		}
		if (JS_IsException(value)) {
			fail(env, context);
			return NULL;
		}
	}
	if (!temporary) {
		value = JS_DupValue(ctx, value);
	}
	size_t size;
	uint8_t *bytes = JS_WriteObject(ctx, &size, value, temporary ? JS_WRITE_OBJ_BYTECODE : JS_WRITE_OBJ_REFERENCE);
	JS_FreeValue(ctx, value);
	if (!bytes) {
		fail(env, context);
		return NULL;
	}
	if (size > 0x7fffffff) {
		js_free(ctx, bytes);
		simple_failure(env, "Serialized value is too large");
		return NULL;
	}
	jbyteArray result = (*env)->NewByteArray(env, size);
	if (result) {
		(*env)->SetByteArrayRegion(env, result, 0, size, (jbyte *)bytes);
	}
	js_free(ctx, bytes);
	return result;
}

JNIEXPORT jlong JNICALL Java_simple_library_wsd_quickjs_Native_host(
		JNIEnv *env, jclass type, jlong pointer, jint id, jstring name, jint length) {
	Context *context = (Context *)(intptr_t)pointer;
	JSContext *ctx = context->engine;
	JSValue data = JS_NewObjectClass(ctx, callback_class);
	if (JS_IsException(data)) {
		fail(env, context);
		return 0;
	}
	Callback *callback = js_malloc_rt(context->runtime->engine, sizeof(*callback));
	if (!callback) {
		JS_FreeValue(ctx, data);
		fail(env, context);
		return 0;
	}
	callback->runtime = context->runtime;
	callback->context = context;
	callback->id = id;
	callback->previous = NULL;
	callback->next = context->runtime->callbacks;
	if (callback->next) {
		callback->next->previous = callback;
	}
	context->runtime->callbacks = callback;
	JS_SetOpaque(data, callback);
	JSValue function = JS_NewCFunctionData(ctx, host_call, length, 0, 1, &data);
	JS_FreeValue(ctx, data);
	char *text = utf8(env, ctx, name, NULL);
	if (!JS_IsException(function)) {
		if (!text ||
				JS_DefinePropertyValueStr(ctx, function, "name", JS_NewString(ctx, text), JS_PROP_CONFIGURABLE) < 0) {
			JS_FreeValue(ctx, function);
			function = JS_EXCEPTION;
		}
	}
	if (text) {
		js_free(ctx, text);
	}
	return result(env, context, function);
}

JNIEXPORT jlongArray JNICALL Java_simple_library_wsd_quickjs_Native_promise(JNIEnv *env, jclass type, jlong pointer) {
	Context *context = (Context *)(intptr_t)pointer;
	JSValue functions[2];
	JSValue promise = JS_NewPromiseCapability(context->engine, functions);
	if (JS_IsException(promise)) {
		fail(env, context);
		return NULL;
	}
	jlongArray array = (*env)->NewLongArray(env, 3);
	if (!array) {
		JS_FreeValue(context->engine, promise);
		JS_FreeValue(context->engine, functions[0]);
		JS_FreeValue(context->engine, functions[1]);
		return NULL;
	}
	jlong pointers[3] = {0, 0, 0};
	JSValue values[3] = {promise, functions[0], functions[1]};
	for (int i = 0; i < 3; i++) {
		pointers[i] = (jlong)(intptr_t)hold(context, values[i]);
		if (!pointers[i]) {
			for (int j = 0; j < i; j++) {
				release((Value *)(intptr_t)pointers[j], 1);
			}
			for (int j = i + 1; j < 3; j++) {
				JS_FreeValue(context->engine, values[j]);
			}
			(*env)->DeleteLocalRef(env, array);
			fail(env, context);
			return NULL;
		}
	}
	(*env)->SetLongArrayRegion(env, array, 0, 3, pointers);
	if ((*env)->ExceptionCheck(env)) {
		for (int i = 0; i < 3; i++) {
			release((Value *)(intptr_t)pointers[i], 1);
		}
	}
	return array;
}

JNIEXPORT jint JNICALL Java_simple_library_wsd_quickjs_Native_jobs(
		JNIEnv *env, jclass type, jlong pointer, jint maximum) {
	Runtime *runtime = (Runtime *)(intptr_t)pointer;
	int done = 0;
	JSContext *ctx = NULL;
	while (done < maximum && JS_IsJobPending(runtime->engine)) {
		int code = JS_ExecutePendingJob(runtime->engine, &ctx);
		if (code < 0) {
			fail(env, JS_GetContextOpaque(ctx));
			break;
		}
		if ((*env)->ExceptionCheck(env)) {
			break;
		}
		if (code == 0) {
			break;
		}
		done++;
	}
	return done;
}

JNIEXPORT jboolean JNICALL Java_simple_library_wsd_quickjs_Native_pending(JNIEnv *env, jclass type, jlong pointer) {
	return JS_IsJobPending(((Runtime *)(intptr_t)pointer)->engine);
}

JNIEXPORT jlongArray JNICALL Java_simple_library_wsd_quickjs_Native_memory(JNIEnv *env, jclass type, jlong pointer) {
	JSMemoryUsage usage;
	JS_ComputeMemoryUsage(((Runtime *)(intptr_t)pointer)->engine, &usage);
	jlong data[] = {usage.malloc_size, usage.malloc_limit, usage.memory_used_size, usage.malloc_count,
			usage.memory_used_count, usage.atom_count, usage.atom_size, usage.str_count, usage.str_size,
			usage.obj_count, usage.obj_size, usage.prop_count, usage.prop_size, usage.shape_count, usage.shape_size,
			usage.js_func_count, usage.js_func_size, usage.js_func_code_size, usage.js_func_pc2line_count,
			usage.js_func_pc2line_size, usage.c_func_count, usage.array_count, usage.fast_array_count,
			usage.fast_array_elements, usage.binary_object_count, usage.binary_object_size};
	jlongArray output = (*env)->NewLongArray(env, 26);
	if (output) {
		(*env)->SetLongArrayRegion(env, output, 0, 26, data);
	}
	return output;
}

JNIEXPORT jlong JNICALL Java_simple_library_wsd_quickjs_Native_identity(
		JNIEnv *env, jclass type, jlong pointer, jlong handle) {
	JSValue value = unwrap(handle);
	return JS_IsObject(value) ? (jlong)(intptr_t)JS_VALUE_GET_PTR(value) : 0;
}

JNIEXPORT void JNICALL Java_simple_library_wsd_quickjs_Native_release(JNIEnv *env, jclass type, jlong pointer) {
	if (pointer) {
		release((Value *)(intptr_t)pointer, 1);
	}
}

JNIEXPORT jobject JNICALL Java_simple_library_wsd_quickjs_Native_owner(JNIEnv *env, jclass type, jlong pointer) {
	jobject owner = ((Value *)(intptr_t)pointer)->context->owner;
	return owner ? (*env)->NewLocalRef(env, owner) : NULL;
}

JNIEXPORT void JNICALL Java_simple_library_wsd_quickjs_Native_closeContext(JNIEnv *env, jclass type, jlong pointer) {
	Context *context = (Context *)(intptr_t)pointer;
	Runtime *runtime = context->runtime;
	JSContext *ctx = context->engine;
	if (context->closed) {
		return;
	}
	context->closed = 1;
	for (Callback *c = runtime->callbacks; c; c = c->next) {
		if (c->context == context) {
			c->context = NULL;
		}
	}
	while (context->values) {
		release(context->values, 1);
	}
	while (context->modules) {
		Module *m = context->modules;
		context->modules = m->next;
		JS_FreeValue(ctx, m->namespace);
		js_free(ctx, m->name);
		js_free(ctx, m);
	}
	JS_FreeValue(ctx, context->helpers);
	context->helpers = JS_UNDEFINED;
	(*env)->DeleteGlobalRef(env, context->owner);
	context->owner = NULL;
	/* 作业队列可能仍引用此上下文；运行时销毁时再统一释放，避免悬空的 opaque。 */
}

JNIEXPORT void JNICALL Java_simple_library_wsd_quickjs_Native_closeRuntime(JNIEnv *env, jclass type, jlong pointer) {
	Runtime *runtime = (Runtime *)(intptr_t)pointer;
	JSRuntime *engine = runtime->engine;
	/* 释放上下文期间禁止再进入 Java 或引用已经回收的 opaque。 */
	JS_SetHostPromiseRejectionTracker(engine, NULL, NULL);
	JS_SetInterruptHandler(engine, NULL, NULL);
	JS_SetModuleLoaderFunc(engine, NULL, NULL, NULL);
	while (runtime->contexts) {
		Context *context = runtime->contexts;
		runtime->contexts = context->next;
		JS_FreeContext(context->engine);
		js_free_rt(engine, context);
	}
	/* 排队任务仍可能持有函数；剩余捕获对象由 JS_FreeRuntime 的终结器释放。 */
	for (Callback *callback = runtime->callbacks; callback; callback = callback->next) {
		callback->runtime = NULL;
		callback->context = NULL;
	}
	(*env)->DeleteGlobalRef(env, runtime->owner);
	js_free_rt(engine, runtime);
	JS_FreeRuntime(engine);
}
