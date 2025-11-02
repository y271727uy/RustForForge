package org.tab.minrust;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Rust工具箱实现类
 * 通过JNI调用Rust动态库
 */
public class RustToolboxImpl implements IRustToolbox {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RustToolboxImpl.class);
    private static final RustToolboxImpl INSTANCE = new RustToolboxImpl();
    
    // 是否成功加载了Rust库
    private boolean rustLibraryLoaded = false;
    
    private RustToolboxImpl() {
        try {
            // 尝试加载Rust动态库
            loadNativeLibrary();
            rustLibraryLoaded = true;
            LOGGER.info("Successfully loaded Rust native library");
        } catch (UnsatisfiedLinkError e) {
            LOGGER.error("Failed to load Rust native library: " + e.getMessage());
            rustLibraryLoaded = false;
        } catch (Exception e) {
            LOGGER.error("Failed to load Rust native library: " + e.getMessage(), e);
            rustLibraryLoaded = false;
        }
    }
    
    private void loadNativeLibrary() throws Exception {
        // 获取操作系统和架构相关的库名称
        String osName = System.getProperty("os.name").toLowerCase();
        String libName;
        String resourcePath;
        
        if (osName.contains("win")) {
            libName = "rustforforge_native.dll";
            resourcePath = "/natives/windows64/" + libName;
        } else if (osName.contains("mac")) {
            libName = "librustforforge_native.dylib";
            resourcePath = "/natives/macosx64/" + libName;
        } else if (osName.contains("linux")) {
            libName = "librustforforge_native.so";
            resourcePath = "/natives/linux64/" + libName;
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + osName);
        }
        
        // 从JAR中提取库文件到临时目录
        InputStream libStream = RustToolboxImpl.class.getResourceAsStream(resourcePath);
        if (libStream == null) {
            throw new RuntimeException("Native library not found in resources: " + resourcePath);
        }
        
        // 创建临时文件
        File tempLibFile = File.createTempFile(libName.replace(".", "_"), "." + libName.substring(libName.lastIndexOf('.') + 1));
        tempLibFile.deleteOnExit();
        
        // 将库文件复制到临时文件
        Files.copy(libStream, tempLibFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        libStream.close();
        
        // 加载库
        System.load(tempLibFile.getAbsolutePath());
        LOGGER.info("Successfully extracted and loaded native library from: " + resourcePath);
    }
    
    public static RustToolboxImpl getInstance() {
        return INSTANCE;
    }
    
    /**
     * 检查Rust库是否已加载
     * @return 如果已加载返回true，否则返回false
     */
    public boolean isRustLibraryLoaded() {
        return rustLibraryLoaded;
    }
    
    /**
     * 使用Rust计算两个整数的和
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 两个整数的和
     */
    public int add(int a, int b) {
        if (!rustLibraryLoaded) {
            throw new RuntimeException("Rust library not loaded");
        }
        
        try {
            return nativeAdd(a, b);
        } catch (Exception e) {
            // 如果异常来源于Rust，在日志中标记[rust]
            LOGGER.error("[rust]: Exception occurred in Rust addition: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 使用Rust处理字符串
     * @param input 输入字符串
     * @return 处理后的字符串
     */
    public String processString(String input) {
        if (!rustLibraryLoaded) {
            throw new RuntimeException("Rust library not loaded");
        }
        
        try {
            return nativeProcessString(input);
        } catch (Exception e) {
            // 如果异常来源于Rust，在日志中标记[rust]
            LOGGER.error("[rust]: Exception occurred in Rust string processing: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 调用可能panic的Rust函数
     * @param shouldPanic 是否触发panic
     */
    public void mightPanic(boolean shouldPanic) {
        if (!rustLibraryLoaded) {
            throw new RuntimeException("Rust library not loaded");
        }
        
        try {
            nativeMightPanic(shouldPanic);
        } catch (Exception e) {
            // 如果异常来源于Rust，在日志中标记[rust]
            LOGGER.error("[rust]: Exception occurred in Rust panic test: " + e.getMessage(), e);
            throw new RustPanicException("Rust代码发生panic", e);
        }
    }
    
    // JNI本地方法声明
    private native int nativeAdd(int a, int b) throws RuntimeException;
    private native String nativeProcessString(String input) throws RuntimeException;
    private native void nativeMightPanic(boolean shouldPanic) throws RuntimeException;
}