package com.ptc.fs.svn.data.def.utils;

import com.ptc.fs.svn.data.def.ChangePackageDef;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DefUtils
{
	public static void exportAsTextFile(ChangePackageDef changePackageDef, String fileName) throws IOException {
		BufferedWriter bw = null;
		try {
			FileWriter fw = new FileWriter(fileName);
			bw = new BufferedWriter(fw, 8192);
			bw.write((null != changePackageDef) ? changePackageDef
					.toString() : "");

			bw.close();
		} finally {
			if (null != bw)
				bw.close(); 
		} 
	}
}
