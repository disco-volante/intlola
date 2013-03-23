package fmv;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.DefaultListModel;
import javax.swing.SwingWorker;

import fmv.TablePane.ArchiveData;

public class Archive {

	/**
	 * Flag to tell if this archive has been viewed and therefore extracted
	 * before.
	 */
	private boolean isExtracted = false;

	private boolean isCompiled = false;

	/**
	 * The model for the list of ZIP files. (Think MVC.)
	 */
	@SuppressWarnings("rawtypes")
	private DefaultListModel listModel = null;

	/**
	 * Name of the directory containing the ZIP file.
	 */
	private String dir = null;

	/**
	 * Name of the ZIP file.
	 */
	private String name = null;

	/**
	 * Tree of items contained in the ZIP file.
	 */
	private final Source rootItem = new Source();

	/**
	 * Constructor. Stores the name of the ZIP file.
	 */
	@SuppressWarnings("rawtypes")
	public Archive(final String dir, final String name) {
		this.dir = dir;
		this.name = name;
		listModel = new DefaultListModel();
		if ("true".equals(FMV.getArchiveProperty(this, "false"))) {
			final ArchiveData data = new ArchiveData();
			data.readProperties(this);
			FMV.tablePane.addData(name, data);
			isCompiled = true;
		}
	}

	/**
	 * Add a new item to the archive. This will succeed if the new item is some
	 * file or directory in the "src" hierarchy. It if succeeds, the hierarchy,
	 * time stamp, and contents of the file is recorded in the archive.
	 * 
	 * @param name
	 *            the name of the item to add. This includes the file path, but
	 *            separated by "_" instead of the usual directory separators. It
	 *            also ends with the time stamp, counter stamp, and a letter to
	 *            indicate if the item has been added, changed, or removed.
	 */
	private void addItem(final String name, final InputStream in) {
		final String[] components = name.split("_");
		final int last = components.length - 4;
		int first = last;
		for (int i = 0; i <= last; i++) {
			if (components[i].equals("src")
					&& !components[i + 1].equals("testing")) {
				first = i + 1;
				break;
			}
		}
		if (first >= last) {
			return;
		}
		Source r = rootItem;
		for (int i = first; i <= last; i++) {
			r = r.addChild(components[i]);
		}
		r.addDetails(new Date(Long.parseLong(components[last + 1])), in);
	}

	public void extract() {
		ZipFile z;
		try {
			z = new ZipFile(dir + File.separator + name);
			final Enumeration<? extends ZipEntry> zz = z.entries();
			while (zz.hasMoreElements()) {
				final ZipEntry e = zz.nextElement();
				if (e.isDirectory()) {
					continue;
				}
				addItem(e.getName(), z.getInputStream(e));
			}
			rootItem.canonize();
			listModel.clear();
			rootItem.addToListModel(listModel, this);
			isExtracted = true;
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public DefaultListModel getModel(final Directory directory) {
		if (!isExtracted()) {
			extract();
		}
		return listModel;
	}

	public boolean isCompiled() {
		return isCompiled;
	}

	public boolean isExtracted() {
		return isExtracted;
	}

	public void runTool(final String tool) {
		ToolRunner runner;
		if (tool.equals("Tests")) {
			final String t = dir + File.separator + "TESTING.zip";
			runner = new ToolRunner(rootItem, FMV.toolrunner, "Tests", t);
		} else {
			runner = new ToolRunner(rootItem, FMV.toolrunner, tool);

		}
		runner.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				if ("progress".equals(e.getPropertyName())) {
					FMV.toolrunner.setProgress((Integer) e.getNewValue());
				} else if ("state".equals(e.getPropertyName())
						&& SwingWorker.StateValue.DONE == e.getNewValue()) {
					FMV.toolrunner.deactivate(false);
				}
			}
		});
		runner.execute();
		FMV.toolrunner.activate(this, rootItem);
	}

	public void setCompiled() {
		isCompiled = true;
	}

	public void setDiff(final int j) {
		final Source s = (Source) listModel.getElementAt(j);
		FMV.diffPane.setItem(this, s);
		FMV.timeGraph.setSource(this, s);
	}

	/**
	 * Return the file name for display in a list of ZIP files.
	 */
	@Override
	public String toString() {
		return name;
	}

}
