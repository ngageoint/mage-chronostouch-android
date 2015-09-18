package mil.nga.giat.chronostouch.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Translates between {@link Parcelable} and {@link byte[]}
 */
public class ParcelableToByteArrayUtil {

	/**
	 * {@link Parcelable} to {@link byte[]}
	 *
	 * @param parcelable
	 * @return
	 */
	public static byte[] toByteArray(Parcelable parcelable) {
		final Parcel parcel = Parcel.obtain();
		parcelable.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		byte[] byteArray = parcel.marshall();
		parcel.recycle();
		return byteArray;
	}

	/**
	 * {@link byte[]} to {@link Parcelable}
	 *
	 * @param byteArray
	 * @param creator
	 * @param <T>
	 * @return
	 */
	public static <T> T getParcelable(byte[] byteArray, Parcelable.Creator<T> creator) {
		final Parcel parcel = Parcel.obtain();
		parcel.unmarshall(byteArray, 0, byteArray.length);
		parcel.setDataPosition(0);
		final T object = creator.createFromParcel(parcel);
		parcel.recycle();
		return object;
	}
}
