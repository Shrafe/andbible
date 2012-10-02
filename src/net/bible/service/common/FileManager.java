package net.bible.service.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class FileManager {

	private static final Logger log = new Logger(FileManager.class.getName());

	public static boolean copyFile(String filename, File fromDir, File toDir) {
		log.debug("Copying:"+filename);
		boolean ok = false;
	    try {
	    	//ensure the target dir exists or FileNotFoundException is thrown creating dst FileChannel
	    	toDir.mkdir();
	    	
            File fromFile = new File(fromDir, filename);
            File targetFile = new File(toDir, filename);

            // don't worry if tofile exists, allow overwrite
            if (fromFile.exists()) {
            	long fromFileSize = fromFile.length();
            	log.debug("Source file length:"+fromFileSize);
            	if (fromFileSize > CommonUtils.getFreeSpace(toDir.getPath())) {
            		// not enough room on SDcard
            		ok = false;            		
            	} else {
	            	// move the file
	                FileChannel src = new FileInputStream(fromFile).getChannel();
	                FileChannel dst = new FileOutputStream(targetFile).getChannel();
	            	try {
	            		dst.transferFrom(src, 0, src.size());
		                ok = true;
	            	} finally {
		                src.close();
		                dst.close();
	            	}
            	}
            } else {
            	// fromfile does not exist
            	ok = false;
            }
	    } catch (Exception e) {
	    	log.error("Error moving file to sd card", e);
	    }
	    return ok;
	}
}
