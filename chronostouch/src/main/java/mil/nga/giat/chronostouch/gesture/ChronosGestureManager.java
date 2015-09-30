package mil.nga.giat.chronostouch.gesture;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureStore;
import android.gesture.Prediction;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ChronosGestureManager {
	protected static final String LOG_NAME = ChronosGestureManager.class.getName();

	// singleton
	protected static ChronosGestureManager chronosGestureManager;

	protected GestureLibrary mStore;
	protected ChronosLibrary mChronosLibrary;
	protected Context mContext;

	private ChronosGestureManager(Context context) {
		mContext = context;

		final String chronosGesturesFilename = "chronos_gestures";
		final File chronosgesturesFile = new File(mContext.getFilesDir(), chronosGesturesFilename);

		if (!chronosgesturesFile.exists()) {
			try {
				chronosgesturesFile.createNewFile();
			} catch (IOException ioe) {
				Log.e(LOG_NAME, "Problem creating " + chronosgesturesFile.getAbsolutePath() + ".", ioe);
			}
		}

		if (chronosgesturesFile.exists() && chronosgesturesFile.canRead()) {
			mStore = GestureLibraries.fromFile(chronosgesturesFile);
			if(!mStore.load()) {
				Log.e(LOG_NAME, "Problem loading file, re-creating file " + chronosgesturesFile.getAbsolutePath() + ".");
				chronosgesturesFile.delete();
				try {
					chronosgesturesFile.createNewFile();
					if(!mStore.load()) {
						Log.e(LOG_NAME, "Problem loading " + chronosgesturesFile.getAbsolutePath() + ".");
					}
				} catch (IOException ioe) {
					Log.e(LOG_NAME, "Problem creating " + chronosgesturesFile.getAbsolutePath() + ".", ioe);
				}
			}
			mStore.setSequenceType(GestureStore.SEQUENCE_INVARIANT);
		} else {
			Log.e(LOG_NAME, "Can not initialize store.");
		}

		final String chronosGesturesChronosDataFilename = "chronos_gestures_chronos_data";
		final File chronosGesturesChronosDataFile = new File(mContext.getFilesDir(), chronosGesturesChronosDataFilename);

		if (!chronosGesturesChronosDataFile.exists()) {
			try {
				chronosGesturesChronosDataFile.createNewFile();
			} catch (IOException ioe) {
				Log.e(LOG_NAME, "Problem creating " + chronosGesturesChronosDataFile.getAbsolutePath() + ".", ioe);
			}
		}

		if (chronosGesturesChronosDataFile.exists() && chronosGesturesChronosDataFile.canRead()) {
			mChronosLibrary = new ChronosLibrary(chronosGesturesChronosDataFile);
		} else {
			Log.e(LOG_NAME, "Can not initialize store.");
		}

	}

	public static ChronosGestureManager getInstance(final Context context) {
		if (context == null) {
			return null;
		} else if (chronosGestureManager == null) {
			chronosGestureManager = new ChronosGestureManager(context);
		}
		return chronosGestureManager;
	}

	public List<ChronosGesture> recognize(Gesture gesture) {
		ArrayList<ChronosGesture> gestures = new ArrayList<>();
		if(gesture != null) {
			ArrayList<Prediction> matches = mStore.recognize(gesture);

			if (matches != null && !matches.isEmpty()) {
				Collections.sort(matches, new Comparator<Prediction>() {
					@Override
					public int compare(Prediction lhs, Prediction rhs) {
						return Double.valueOf(rhs.score).compareTo(Double.valueOf(lhs.score));
					}
				});

				for (Gesture g : mStore.getGestures(matches.get(0).name)) {
					gestures.add(new ChronosGesture(g, mChronosLibrary.get(g.getID())));
				}
			}
		}
		return gestures;
	}

	public boolean addGesture(ChronosGesture gesture) {
		mStore.addGesture(gesture.getChronosData().getName(), gesture.getGesture());
		mChronosLibrary.add(gesture.getChronosData());
		return mStore.save() && mChronosLibrary.save();
	}

	public boolean removeGesture(ChronosGesture gesture) {
		mStore.removeGesture(gesture.getChronosData().getName(), gesture.getGesture());
		mStore.removeEntry(gesture.getChronosData().getName());
		mChronosLibrary.remove(gesture.getGesture().getID());
		return mStore.save() && mChronosLibrary.save();
	}

	public Set<ChronosGesture> getGestures() {
		Set<ChronosGesture> chronosGestures = new TreeSet<ChronosGesture>();

		for(String e : mStore.getGestureEntries()) {
			for(Gesture g : mStore.getGestures(e)) {
				chronosGestures.add(new ChronosGesture(g, mChronosLibrary.get(g.getID())));
			}
		}

		return chronosGestures;
	}
}
