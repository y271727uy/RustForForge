/// Rust高性能计算库示例
/// 提供通过JNI调用的原生函数
use std::panic;

/// 计算两个整数的和
/// 
/// # 参数
/// * `env` - JNI环境指针
/// * `_class` - Java类对象
/// * `a` - 第一个整数
/// * `b` - 第二个整数
/// 
/// # 返回值
/// 两个整数的和
#[no_mangle]
pub extern "C" fn Java_org_tab_minrust_RustToolboxImpl_nativeAdd(
    mut env: jni::JNIEnv,
    _class: jni::objects::JClass,
    a: i32,
    b: i32,
) -> i32 {
    // 使用 catch_unwind 捕获可能的 panic
    let result = panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        // 这里可以添加任何Rust代码
        // 为了演示目的，我们只做简单的加法
        a + b
    }));
    
    // 如果发生 panic，向 Java 抛出 RuntimeException
    match result {
        Ok(value) => value,
        Err(panic_info) => {
            let message = match panic_info.downcast_ref::<&str>() {
                Some(s) => format!("[rust]: {}", s),
                None => match panic_info.downcast_ref::<String>() {
                    Some(s) => format!("[rust]: {}", s),
                    None => "[rust]: Rust panic occurred in nativeAdd".to_string(),
                }
            };
            
            // 向 Java 抛出异常
            let _ = env.throw_new("java/lang/RuntimeException", &message);
            0 // 返回默认值
        }
    }
}

/// 处理字符串数据
/// 
/// # 参数
/// * `env` - JNI环境指针
/// * `_class` - Java类对象
/// * `input` - 输入的Java字符串
/// 
/// # 返回值
/// 处理后的字符串
#[no_mangle]
pub extern "C" fn Java_org_tab_minrust_RustToolboxImpl_nativeProcessString<'a>(
    mut env: jni::JNIEnv<'a>,
    _class: jni::objects::JClass<'a>,
    input: jni::objects::JString<'a>,
) -> jni::objects::JString<'a> {
    // 使用 catch_unwind 捕获可能的 panic
    let result = panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        // 将Java字符串转换为Rust字符串
        let input: String = env.get_string(&input).expect("Couldn't get java string!").into();
        
        // 处理字符串 - 这里我们简单地添加前缀
        let output = format!("[RUST] {}", input);
        
        // 创建并返回新的Java字符串
        env.new_string(output).expect("Couldn't create java string!")
    }));
    
    // 如果发生 panic，向 Java 抛出 RuntimeException
    match result {
        Ok(value) => value,
        Err(panic_info) => {
            let message = match panic_info.downcast_ref::<&str>() {
                Some(s) => format!("[rust]: {}", s),
                None => match panic_info.downcast_ref::<String>() {
                    Some(s) => format!("[rust]: {}", s),
                    None => "[rust]: Rust panic occurred in nativeProcessString".to_string(),
                }
            };
            
            // 向 Java 抛出异常
            let _ = env.throw_new("java/lang/RuntimeException", &message);
            
            // 返回空字符串作为默认值
            env.new_string("").unwrap_or_else(|_| jni::objects::JString::from(jni::objects::JObject::null()))
        }
    }
}

/// 可能会panic的函数示例
/// 
/// # 参数
/// * `env` - JNI环境指针
/// * `_class` - Java类对象
/// * `should_panic` - 是否触发panic
#[no_mangle]
pub extern "C" fn Java_org_tab_minrust_RustToolboxImpl_nativeMightPanic(
    mut env: jni::JNIEnv,
    _class: jni::objects::JClass,
    should_panic: bool,
) {
    // 使用 catch_unwind 捕获可能的 panic
    let result = panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        if should_panic {
            // 故意触发panic来演示错误处理
            panic!("Rust代码故意触发的panic!");
        }
        // 正常执行
    }));
    
    // 如果发生 panic，向 Java 抛出 RuntimeException
    if let Err(panic_info) = result {
        let message = match panic_info.downcast_ref::<&str>() {
            Some(s) => format!("[rust]: {}", s),
            None => match panic_info.downcast_ref::<String>() {
                Some(s) => format!("[rust]: {}", s),
                None => "[rust]: Rust panic occurred in nativeMightPanic".to_string(),
            }
        };
        
        // 向 Java 抛出异常
        let _ = env.throw_new("java/lang/RuntimeException", &message);
    }
}

//其实我是rust新手（（