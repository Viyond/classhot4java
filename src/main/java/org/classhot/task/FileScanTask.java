package org.classhot.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import org.classhot.ClassRedefiner;
import org.classhot.utils.ASMClassLoader;
import org.classhot.utils.Clazzs;
import org.classhot.utils.PackageFromClassFast;

/**
 * 文件扫描
 * @author jiaojianfeng
 *
 */
public class FileScanTask implements Runnable {
	private String filePath;
	private Map<String/*className*/, Long> fileLastModifiedMap = new HashMap<>();

	private ASMClassLoader asmClassLoader = new ASMClassLoader();
	
	public FileScanTask(final String filePath) {
		this.setFilePath(filePath);
		System.out.println("create FileScanTask, filePath:" + filePath);
	}

	@Override
	public void run() {
		try {
			//System.out.println(filePath);
			//System.out.println(Thread.currentThread().getContextClassLoader());
			File file = new File(filePath);
			if(!file.exists() || !file.isDirectory()) {
				System.out.println(String.format("FileScanTask executed but %s not exist or is not directory", filePath));
				return;
			}

			File[] classFiles = file.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if(!name.endsWith(".class")) {
						return false;
					}
					return true;
				}
			});

			String msg = String.format("FileScanTask schedule executed..., classFiles size:%s, asmClassloader:%s",
				(classFiles != null? classFiles.length : 0), asmClassLoader);
			System.out.println(msg);
			
			for(File classFile : classFiles) {
				FileInputStream fs = null;
				FileChannel channel = null;
				try {
					//modify time check
					String clazzName = PackageFromClassFast.getPackage(classFile.getAbsolutePath());
					Long lastModified = fileLastModifiedMap.get(clazzName);
					if (lastModified != null && lastModified >= classFile.lastModified()){
						continue;
					}
					if (lastModified == null){
						System.out.println("首次扫描并加载：" + clazzName);
					}else {
						System.out.println("发生类文件变更，重新扫描并加载：" + clazzName);
					}

					fs = new FileInputStream(classFile);
					channel = fs.getChannel();
					ByteBuffer byteBuffer = ByteBuffer.allocate((int)channel.size());
					while(channel.read(byteBuffer) > 0) {}
					
					byte[] b = byteBuffer.array();
					
					//System.out.println("替换：" + clazzName);
					
		            Class<?> clazz = Clazzs.getClazz(clazzName);
		            if (clazz == null){
		            	clazz = asmClassLoader.defineClassPublic(clazzName, b, 0, b.length);
		            	Clazzs.setClazz(clazzName, clazz);
					}
		            System.out.println(String.format("%s load by %s", clazz, clazz.getClassLoader()));
		            ClassRedefiner.redefine(clazz, b);

		            fileLastModifiedMap.put(clazzName, classFile.lastModified());
		            
		        } catch (IOException e) {
		            e.printStackTrace();
		        } finally {
					if(channel != null) {
						try {
							channel.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if(fs != null) {
						try {
							fs.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				//classFile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}