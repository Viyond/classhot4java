# classhot4java
class热替换项目


通过javaagent，动态扫描指定class文件目录，在发生变更时候，自动将最新的字节码加载到jvm，并通过asm进行初始化；期间业务进程可以持续运行，不受影响，以达到热更新的效果

程序运行，如通过java命令行形式：
java -javaagent:/Users/xxx/opensource/java_tools/hot-classloader/classhot/classhot/target/classhot.jar=/Users/xxx/opensource/java_tools/hot-classloader/classhot/classhot/target/test-classes/org/classhot/test org.classhot.test.TestAgent
