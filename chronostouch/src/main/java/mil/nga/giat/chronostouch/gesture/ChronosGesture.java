package mil.nga.giat.chronostouch.gesture;

import android.gesture.Gesture;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wiedemanns on 9/18/15.
 */
public class ChronosGesture implements Comparable<ChronosGesture>, Parcelable {

	protected static final String LOG_NAME = ChronosGesture.class.getName();

	protected Gesture mGesture;
	protected ChronosData mChronosData;

	public ChronosGesture(Gesture gesture, ChronosData chronosData) {
		this.mGesture = gesture;
		this.mChronosData = chronosData;
	}

	@Override
	public Object clone() {
		ChronosGesture chronosGesture = new ChronosGesture((Gesture)mGesture.clone(), (ChronosData)mChronosData.clone());
		return chronosGesture;
	}

	@Override
	public int compareTo(ChronosGesture another) {
		return this.mChronosData.getName().compareTo(another.mChronosData.getName());
	}

	public Gesture getGesture() {
		return mGesture;
	}

	public ChronosData getChronosData() {
		return mChronosData;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(mGesture, 0);
		out.writeParcelable(mChronosData, 0);
	}

	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ChronosGesture> CREATOR = new Parcelable.Creator<ChronosGesture>() {
		public ChronosGesture createFromParcel(Parcel in) {
			ChronosGesture chronosGesture = new ChronosGesture((Gesture)in.readParcelable(Gesture.class.getClassLoader()), (ChronosData)in.readParcelable(ChronosData.class.getClassLoader()));
			return chronosGesture;
		}

		public ChronosGesture[] newArray(int size) {
			return new ChronosGesture[size];
		}
	};
}
