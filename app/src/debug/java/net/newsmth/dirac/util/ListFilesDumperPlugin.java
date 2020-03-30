package net.newsmth.dirac.util;
import android.content.Context;

import com.facebook.stetho.dumpapp.DumpException;
import com.facebook.stetho.dumpapp.DumperContext;
import com.facebook.stetho.dumpapp.DumperPlugin;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

/**
 * Created by espen on 18.06.2015.
 */
public class ListFilesDumperPlugin implements DumperPlugin {
  private static final String COMMAND = "list";
  private final Context mContext;

  public ListFilesDumperPlugin(Context context) {
    mContext = context;
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public void dump(DumperContext dumpContext) throws DumpException {
    PrintStream writer = dumpContext.getStdout();
    doListFiles(mContext.getApplicationInfo().dataDir, writer);
  }

  private void doListFiles(String path, PrintStream writer) {
    File root = new File(path);
    File[] list = root.listFiles();

    if (list == null) return;

    for (File f : list) {
      if (f.isDirectory()) {
        writer.format("%8d %S %s/ (directory)\n", f.length(), new Date(f.lastModified()).toString(), f.getAbsoluteFile());
        doListFiles(f.getAbsolutePath(), writer);
      } else {
        writer.format("%8d %S %s\n", f.length(), new Date(f.lastModified()).toString(), f.getAbsoluteFile());
      }
    }
  }
}