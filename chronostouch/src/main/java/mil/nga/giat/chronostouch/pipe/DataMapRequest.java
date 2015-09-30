package mil.nga.giat.chronostouch.pipe;

import android.os.Parcelable;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;

import mil.nga.giat.chronostouch.utils.ParcelableToByteArrayUtil;

public class DataMapRequest {
	private PutDataMapRequest dataMapRequest;

	public DataMapRequest(String path) {
		dataMapRequest = PutDataMapRequest.create(path);
	}

	public PutDataRequest asPutDataRequest() {
		return dataMapRequest.asPutDataRequest();
	}

	public <T extends Object> boolean put(final String key, final T dataItem) {
		switch (dataItem.getClass().getSimpleName()) {
			case "Asset":
				dataMapRequest.getDataMap().putAsset(key, (Asset) dataItem);
				break;
			case "Boolean":
			case "boolean":
				dataMapRequest.getDataMap().putBoolean(key, (Boolean) dataItem);
				break;
			case "Byte":
			case "byte":
				dataMapRequest.getDataMap().putByte(key, (Byte) dataItem);
				break;
			case "DataMap":
				dataMapRequest.getDataMap().putDataMap(key, (DataMap) dataItem);
				break;
			case "Double":
			case "double":
				dataMapRequest.getDataMap().putDouble(key, (Double) dataItem);
				break;
			case "Float":
			case "float":
				dataMapRequest.getDataMap().putFloat(key, (Float) dataItem);
				break;
			case "Gesture":
				dataMapRequest.getDataMap().putByteArray(key, ParcelableToByteArrayUtil.toByteArray((Parcelable) dataItem));
				break;
			case "Integer":
			case "int":
				dataMapRequest.getDataMap().putInt(key, (Integer) dataItem);
				break;
			case "Long":
			case "long":
				dataMapRequest.getDataMap().putLong(key, (Long) dataItem);
				break;
			case "String":
				dataMapRequest.getDataMap().putString(key, (String) dataItem);
				break;
			default:
				return false;
		}
		return true;
	}

}
