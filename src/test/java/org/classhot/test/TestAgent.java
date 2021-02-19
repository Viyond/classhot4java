package org.classhot.test;

import java.lang.management.ManagementFactory;

/**
 * 启动参数配置 javaagent
 * -javaagent:classhot.jar
 * 
 * @author jiaojianfeng
 *
 */
public class TestAgent {

	public static void main(String[] args) {
		try{
			//Clazzs.setClazz(A.class.getName(), A.class);
			PID();
			int i = 0;
			A a = new A();
			a.run();
			i++;
			while(true) {
				Thread.sleep(10000);
				System.out.println("第" + (i++) + "次执行...");
				a.run();
			}
		}catch (Throwable e){
			e.printStackTrace();
		}
	}

	private static void PID(){
		String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
		System.out.println("### current pid:" + pid);
	}
}
