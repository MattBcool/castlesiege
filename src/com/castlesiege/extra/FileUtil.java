package com.castlesiege.extra;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil
{
	public static void deleteFile(File file)
	{
		if (file.isDirectory())
		{
			File[] arrayOfFile;
			int j = (arrayOfFile = file.listFiles()).length;
			for (int i = 0; i < j; i++)
			{
				File subfile = arrayOfFile[i];
				deleteFile(subfile);
			}
		} else
		{
			file.delete();
		}
	}

	public static void extractZIP(File archive, File destDir) throws IOException
	{

		if (!destDir.exists())
		{
			destDir.mkdirs();
		}
		ZipFile zipFile = new ZipFile(archive);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		byte[] buffer = new byte[16384];
		int len;
		while (entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String entryFileName = entry.getName();
			File dir = buildDirectoryHierarchyFor(entryFileName, destDir);

			if (!dir.exists())
			{
				dir.mkdirs();
			}

			if (!entry.isDirectory())
			{
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(new File(destDir, entryFileName)));
				BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
				while ((len = bis.read(buffer)) > 0)
				{
					bos.write(buffer, 0, len);
				}

				bos.flush();
				bos.close();
				bis.close();

			}
		}
		zipFile.close();
	}

	private static File buildDirectoryHierarchyFor(String entryName, File destDir)
	{
		int lastIndex = entryName.lastIndexOf('/');
		String internalPathToEntry = entryName.substring(0, lastIndex + 1);
		return new File(destDir, internalPathToEntry);
	}
}
