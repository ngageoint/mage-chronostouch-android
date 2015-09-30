package mil.nga.giat.chronostouch.gesture;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wiedemanns on 9/29/15.
 */
public class ChronosLibrary {

	protected static final String LOG_NAME = ChronosLibrary.class.getName();

	private static final short FILE_FORMAT_VERSION = 1;

	static final int IO_BUFFER_SIZE = 32 * 1024; // 32K

	private final File mPath;

	private final Map<Long, ChronosData> mChronosData = new HashMap<Long, ChronosData>();

	public ChronosLibrary(File path) {
		mPath = path;
		load();
	}

	public boolean save() {
		final File file = mPath;

		final File parentFile = file.getParentFile();
		if (!parentFile.exists()) {
			if (!parentFile.mkdirs()) {
				return false;
			}
		}

		boolean result = false;
		try {
			//noinspection ResultOfMethodCallIgnored
			file.createNewFile();
			save(new FileOutputStream(file));
			result = true;
		} catch (IOException e) {
			Log.d(LOG_NAME, "Could not save the gesture library in " + mPath, e);
		}

		return result;

	}

	public boolean load() {
		boolean result = false;
		final File file = mPath;
		if (file.exists() && file.canRead()) {
			try {
				load(new FileInputStream(file));
				result = true;
			} catch (IOException e) {
				Log.d(LOG_NAME, "Could not load the gesture library from " + mPath, e);
			}
		}

		return result;
	}

	protected void save(OutputStream stream) throws IOException {
		DataOutputStream out = null;

		try {
			out = new DataOutputStream((stream instanceof BufferedOutputStream) ? stream : new BufferedOutputStream(stream, IO_BUFFER_SIZE));
			// Write version number
			out.writeShort(FILE_FORMAT_VERSION);
			// Write number of entries
			out.writeInt(mChronosData.size());

			for (Map.Entry<Long, ChronosData> entry : mChronosData.entrySet()) {
				final Long key = entry.getKey();
				final ChronosData data = entry.getValue();
				data.serialize(out);
			}

			out.flush();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Log.e(LOG_NAME, "Could not close stream", e);
				}
			}
		}
	}

	protected void load(InputStream stream) throws IOException {
		DataInputStream in = null;
		try {
			in = new DataInputStream((stream instanceof BufferedInputStream) ? stream : new BufferedInputStream(stream, IO_BUFFER_SIZE));

			// Read file format version number
			final short versionNumber = in.readShort();
			switch (versionNumber) {
				case 1:
					readFormatV1(in);
					break;
			}

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Log.e(LOG_NAME, "Could not close stream", e);
				}
			}
		}
	}

	private void readFormatV1(DataInputStream in) throws IOException {
		mChronosData.clear();

		final int count = in.readInt();

		for (int j = 0; j < count; j++) {
			final ChronosData chronosData = ChronosData.deserialize(in);
			mChronosData.put(chronosData.getGestureID(), chronosData);
		}
	}

	public void add(ChronosData chronosData) {
		mChronosData.put(chronosData.getGestureID(), chronosData);
	}

	public void remove(Long gestureID) {
		mChronosData.remove(gestureID);
	}

	public ChronosData get(Long gesutreID) {
		return mChronosData.get(gesutreID);
	}
}
