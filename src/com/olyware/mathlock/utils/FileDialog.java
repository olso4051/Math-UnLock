package com.olyware.mathlock.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.ListenerList.FireHandler;

public class FileDialog {
	private static final String PARENT_DIR = "..";
	private int dp5;
	private List<File> files, dirs, allEntries;
	private File currentPath;
	private Context ctx;
	private Locale loc;

	public interface FileSelectedListener {
		void fileSelected(File file);
	}

	public interface DirectorySelectedListener {
		void directorySelected(File directory);
	}

	private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<FileDialog.FileSelectedListener>();
	private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<FileDialog.DirectorySelectedListener>();
	private boolean selectDirectoryOption;
	private String fileEndsWith;

	/**
	 * @param activity
	 * @param initialPath
	 */
	public FileDialog(Context ctx, File path, String fileEndsWith) {
		this.ctx = ctx;
		loc = new Locale("en");
		dp5 = (int) (5 * ctx.getResources().getDisplayMetrics().density + 0.5f);
		if (!path.exists())
			path = Environment.getExternalStorageDirectory();
		this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase(loc) : fileEndsWith;
		files = new ArrayList<File>();
		dirs = new ArrayList<File>();
		allEntries = new ArrayList<File>();
		loadFileList(path);
	}

	public class FileAdapter extends ArrayAdapter<File> {
		public FileAdapter(int resid) {
			super(ctx, resid, allEntries);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// User super class to create the View
			View v = super.getView(position, convertView, parent);
			TextView tv = (TextView) v.findViewById(R.id.file_text);
			// Put the image on the TextView
			if (allEntries.get(position).equals(currentPath)) {
				tv.setText(PARENT_DIR);
				tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			} else if (allEntries.get(position).isDirectory()) {
				tv.setCompoundDrawablesWithIntrinsicBounds(ctx.getResources().getDrawable(R.drawable.folder), null, null, null);
				tv.setCompoundDrawablePadding(dp5);
				tv.setText(allEntries.get(position).getName());
			} else {
				tv.setCompoundDrawablesWithIntrinsicBounds(ctx.getResources().getDrawable(R.drawable.file), null, null, null);
				tv.setCompoundDrawablePadding(dp5);
				tv.setText(allEntries.get(position).getName());
			}
			return tv;
		}
	};

	/**
	 * @return file dialog
	 */
	public Dialog createFileDialog() {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		FileAdapter fAdapter = new FileAdapter(R.layout.file_list_text_item);

		builder.setTitle(currentPath.getPath());
		if (selectDirectoryOption) {
			builder.setPositiveButton("Select directory", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					fireDirectorySelectedEvent(currentPath);
				}
			});
		}
		builder.setAdapter(fAdapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				File chosenFile = getChosenFile(allEntries.get(which));
				if (chosenFile.isDirectory()) {
					loadFileList(chosenFile);
					dialog.cancel();
					dialog.dismiss();
					showDialog();
				} else
					fireFileSelectedEvent(chosenFile);
			}
		});
		/*builder.setItems(fileList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String fileChosen = fileList[which];
				File chosenFile = getChosenFile(fileChosen);
				if (chosenFile.isDirectory()) {
					loadFileList(chosenFile);
					dialog.cancel();
					dialog.dismiss();
					showDialog();
				} else
					fireFileSelectedEvent(chosenFile);
			}
		});*/
		dialog = builder.show();
		return dialog;
	}

	public void addFileListener(FileSelectedListener listener) {
		fileListenerList.add(listener);
	}

	public void removeFileListener(FileSelectedListener listener) {
		fileListenerList.remove(listener);
	}

	public void setSelectDirectoryOption(boolean selectDirectoryOption) {
		this.selectDirectoryOption = selectDirectoryOption;
	}

	public void addDirectoryListener(DirectorySelectedListener listener) {
		dirListenerList.add(listener);
	}

	public void removeDirectoryListener(DirectorySelectedListener listener) {
		dirListenerList.remove(listener);
	}

	/**
	 * Show file dialog
	 */
	public void showDialog() {
		createFileDialog().show();
	}

	private void fireFileSelectedEvent(final File file) {
		fileListenerList.fireEvent(new FireHandler<FileDialog.FileSelectedListener>() {
			public void fireEvent(FileSelectedListener listener) {
				listener.fileSelected(file);
			}
		});
	}

	private void fireDirectorySelectedEvent(final File directory) {
		dirListenerList.fireEvent(new FireHandler<FileDialog.DirectorySelectedListener>() {
			public void fireEvent(DirectorySelectedListener listener) {
				listener.directorySelected(directory);
			}
		});
	}

	private void loadFileList(File path) {
		this.currentPath = path;
		if (path.exists()) {
			if (path.getParentFile() != null) {
				// r.add(PARENT_DIR);
			}
			FilenameFilter filterFiles = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					if (!sel.canRead())
						return false;
					else if (sel.isDirectory())
						return false;
					else
						return fileEndsWith != null ? filename.toLowerCase(loc).endsWith(fileEndsWith) : true;
				}
			};
			FilenameFilter filterDirs = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					if (!sel.canRead())
						return false;
					else
						return sel.isDirectory();
				}
			};
			File[] dirs1 = path.listFiles(filterDirs);
			File[] files1 = path.listFiles(filterFiles);
			dirs.clear();
			files.clear();
			for (File dir : dirs1) {
				dirs.add(dir);
			}
			for (File file : files1) {
				files.add(file);
			}
			Comparator<File> comp = new Comparator<File>() {
				public int compare(File f1, File f2) {
					return f1.getName().toLowerCase(loc).compareTo(f2.getName().toLowerCase(loc));
				}
			};
			Collections.sort(dirs, comp);
			Collections.sort(files, comp);
			allEntries.clear();
			allEntries.add(currentPath);
			allEntries.addAll(dirs);
			allEntries.addAll(files);
		}
	}

	private File getChosenFile(File fileChosen) {
		if (fileChosen == currentPath)
			return currentPath.getParentFile();
		else
			return fileChosen;
	}

	public void setFileEndsWith(String fileEndsWith) {
		this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase(loc) : fileEndsWith;
	}
}

class ListenerList<L> {
	private List<L> listenerList = new ArrayList<L>();

	public interface FireHandler<L> {
		void fireEvent(L listener);
	}

	public void add(L listener) {
		listenerList.add(listener);
	}

	public void fireEvent(FireHandler<L> fireHandler) {
		List<L> copy = new ArrayList<L>(listenerList);
		for (L l : copy) {
			fireHandler.fireEvent(l);
		}
	}

	public void remove(L listener) {
		listenerList.remove(listener);
	}

	public List<L> getListenerList() {
		return listenerList;
	}
}
