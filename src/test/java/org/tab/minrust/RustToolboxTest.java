package org.tab.minrust.test;

import org.tab.minrust.RustToolboxImpl;

public class RustToolboxTest {
    public static void main(String[] args) {
        // 测试Rust工具箱功能
        RustToolboxImpl toolbox = RustToolboxImpl.getInstance();
        
        if (toolbox.isRustLibraryLoaded()) {
            System.out.println("Rust library loaded successfully!");
            
            // 测试加法功能
            try {
                int result = toolbox.add(5, 3);
                System.out.println("5 + 3 = " + result);
            } catch (Exception e) {
                System.err.println("Error in add operation: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 测试字符串处理功能
            try {
                String result = toolbox.processString("Hello from Java!");
                System.out.println("Processed string: " + result);
            } catch (Exception e) {
                System.err.println("Error in string processing: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 测试正常的panic处理功能
            try {
                toolbox.mightPanic(false);
                System.out.println("Normal execution without panic completed");
            } catch (Exception e) {
                System.err.println("Error in normal panic test: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 测试触发panic的功能（应该被捕获而不是导致JVM崩溃）
            try {
                System.out.println("Testing panic handling...");
                toolbox.mightPanic(true); // 这应该会抛出异常而不是导致JVM崩溃
                System.out.println("Panic test completed without throwing exception");
            } catch (Exception e) {
                System.err.println("Caught exception from Rust panic (this is expected): " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("All tests completed!");
        } else {
            System.err.println("Failed to load Rust library");
        }
    }
}