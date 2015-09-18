package mil.nga.giat.chronostouch.gesture;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GestureManager {
	protected static final String LOG_NAME = GestureManager.class.getName();

	// singleton
	private static GestureManager gestureManager;

	private GestureLibrary mStore;
	private Context mContext;

	private GestureManager(Context context) {
		mContext = context;

		final String chronosgesturesFilename = "chronosgestures";
		final File chronosgesturesFile = new File(mContext.getFilesDir(), chronosgesturesFilename);

		if (!chronosgesturesFile.exists()) {
			try {
				chronosgesturesFile.createNewFile();
			} catch (IOException ioe) {
				Log.e(LOG_NAME, "Problem creating " + chronosgesturesFile.getAbsolutePath() + ".", ioe);
			}
		}

		if (chronosgesturesFile.exists() && chronosgesturesFile.canRead()) {
			mStore = GestureLibraries.fromFile(chronosgesturesFile);
			mStore.load();
		}
	}

	public static GestureManager getInstance(final Context context) {
		if (context == null) {
			return null;
		} else if (gestureManager == null) {
			gestureManager = new GestureManager(context);
		}
		return gestureManager;
	}

	public List<ChronosGesture> recognize(ChronosGesture gesture) {
		ArrayList<ChronosGesture> gestures = new ArrayList<>();
		ArrayList<Prediction> matches = mStore.recognize(gesture);

		if (matches != null && !matches.isEmpty()) {

			// TODO : check that sortted in the correct order
			Collections.sort(matches, new Comparator<Prediction>() {
				@Override
				public int compare(Prediction lhs, Prediction rhs) {
					return Double.valueOf(lhs.score).compareTo(Double.valueOf(rhs.score));
				}
			});

			for (Gesture g : mStore.getGestures(matches.get(0).name)) {
				if (g instanceof ChronosGesture) {
					gestures.add((ChronosGesture) g);
				} else {
					Log.d(LOG_NAME, "Matched a non-chronosgesture: " + g.getID());
				}
			}
		}
		return gestures;
	}

	public boolean addGesture(ChronosGesture gesture) {
		mStore.addGesture(gesture.getName(), gesture);
		return mStore.save();
	}

	public boolean removeGesture(ChronosGesture gesture) {
		mStore.removeGesture(gesture.getName(), gesture);
		return mStore.save();
	}
}
