package mil.nga.giat.chronostouch.gesture;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by wiedemanns on 9/29/15.
 */
public class ChronosData implements Parcelable {
	// from gesture
	protected long mGestureID;
	protected String mName;
	protected String mType;
	// TODO : add event field so we can pull the correct gesutres per event

	public ChronosData(long gestureID, String name, String type) {
		this.mGestureID = gestureID;
		this.mName = name;
		this.mType = type;
	}

	void serialize(DataOutputStream out) throws IOException {
		// Write gesture ID
		out.writeLong(mGestureID);
		out.writeUTF(mName);
		out.writeUTF(mType);
	}

	static ChronosData deserialize(DataInputStream in) throws IOException {
		final ChronosData chronosData = new ChronosData(in.readLong(), in.readUTF(), in.readUTF());
		return chronosData;
	}

	public long getGestureID() {
		return mGestureID;
	}

	public String getName() {
		return mName;
	}

	public String getType() {
		return mType;
	}

	@Override
	public Object clone() {
		ChronosData chronosData = new ChronosData(mGestureID, mName, mType);
		return chronosData;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(mGestureID);
		out.writeString(mName);
		out.writeString(mType);
	}

	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ChronosData> CREATOR = new Parcelable.Creator<ChronosData>() {
		public ChronosData createFromParcel(Parcel in) {
			ChronosData chronosData = new ChronosData(in.readLong(), in.readString(), in.readString());
			return chronosData;
		}

		public ChronosData[] newArray(int size) {
			return new ChronosData[size];
		}
	};
}
