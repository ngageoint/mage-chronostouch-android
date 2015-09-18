package mil.nga.giat.chronostouch.pipe;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DataManager {
	private static final String CONNECTED_PATH = "/connected";

	private static DataManager dLManager;

	private Map<String, DataMapRequest> dataMapReqs;
	private Map<String, Map<String, Object>> fetchData;
	private HashSet<String> watchPaths;
	private HashSet<String> watchKeys;
	private HashSet<String> mConnectedNodes;
	private Context mContext;
	private Node mNode;
	private boolean wearConnected;

	private Handler handler = new Handler();

	private DataManager(Context context) {
		mContext = context;
		watchKeys = new HashSet<>();
		watchPaths = new HashSet<>();
		mConnectedNodes = new HashSet<>();
		dataMapReqs = new HashMap<>();
		fetchData = new HashMap<>();
		handler.post(fetchLocalNode);
		handler.post(fetchConnectedNodes);
	}

	public static DataManager getInstance(final Context context) {
		if (context == null) {
			return null;
		} else if (dLManager == null) {
			dLManager = new DataManager(context);
		}
		return dLManager;
	}

	public boolean createMap(final String path) {
		DataMapRequest dataMapReq = new DataMapRequest(path);

		if (dataMapReqs.containsKey(path)) {
			return false;
		} else {
			dataMapReqs.put(path, dataMapReq);
			return true;
		}
	}

	public <T extends Object> boolean addDataItem(final String path, final String key, final T dataItem) {
		DataMapRequest dataMapReq = dataMapReqs.get(path);
		boolean success = false;

		if (dataMapReq == null) {
			return success;
		}

		success = dataMapReq.put(key, dataItem);
		if (!success) {
			return success;
		}

		dataMapReqs.put(path, dataMapReq);
		return success;
	}

	public void sendData() {
		Runnable sendData = new Runnable() {
			@Override
			public void run() {
				GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(mContext).addApi(Wearable.API).build();
				mGoogleApiClient.connect();

				for (Map.Entry<String, DataMapRequest> dataMapEntry : dataMapReqs.entrySet()) {
					PutDataRequest putDataReq = dataMapEntry.getValue().asPutDataRequest();
					PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
				}
				dataMapReqs.clear();
			}
		};

		Handler handler = new Handler();
		handler.post(sendData);
	}

	public void fetchDataFromService(final DataItem item) {
		if (watchPaths.contains(item.getUri().getPath())) {
			DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
			Map<String, Object> itemsMap = new HashMap<>();

			for (String key : dataMap.keySet()) {
				if (watchKeys.contains(key)) {
					itemsMap.put(key, dataMap.get(key));
				}
			}

			fetchData.put(item.getUri().getPath(), itemsMap);
			Intent updatedIntent = new Intent(item.getUri().getPath());
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(updatedIntent);
		}
	}


	public void sendMessage(final String path, final String message) {
		Runnable sendData = new Runnable() {
			@Override
			public void run() {

				final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(mContext).addApi(Wearable.API).build();
				mGoogleApiClient.connect();
				final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);

				nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
					@Override
					public void onResult(NodeApi.GetConnectedNodesResult result) {
						for (Node node : result.getNodes()) {
							Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes());
						}
					}
				});


			}
		};
		Handler handler = new Handler();
		handler.post(sendData);
	}

	public void fetchMessageEvent(MessageEvent messageEvent) {
		if (messageEvent.getPath().equalsIgnoreCase(CONNECTED_PATH)) {
			if (!wearConnected) {
				mConnectedNodes.add(messageEvent.getSourceNodeId());
				wearConnected = true;
			}
		} else if (watchPaths.contains(messageEvent.getPath())) {
			String[] values = new String[]{messageEvent.getSourceNodeId(), messageEvent.getPath(), messageEvent.getData().toString()};
			Intent updatedIntent = new Intent(messageEvent.getPath());
			updatedIntent.putExtra("message", values);
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(updatedIntent);
		}
	}

	public void updateCapability(CapabilityInfo capabilityInfo) {
		// TODO: Do something with this capabilityinfo.
	}

	public Map<String, Map<String, Object>> getData() {
		Map<String, Map<String, Object>> data = new HashMap<>(fetchData);
		fetchData.clear();
		return data;
	}

	public boolean addListenKey(final String key) {
		return watchKeys.add(key);
	}

	public boolean removeListenKey(final String key) {
		return watchKeys.remove(key);
	}

	public boolean clearKeys() {
		watchKeys.clear();
		return watchKeys.isEmpty();
	}

	public boolean addListenPath(final String path) {
		return watchPaths.add(path);
	}

	public boolean removeListenPath(final String path) {
		return watchPaths.remove(path);
	}

	public boolean clearPaths() {
		watchPaths.clear();
		return watchPaths.isEmpty();
	}

	public void fetchConnectedNode(String nodeIdDisplay) {
		mConnectedNodes.add(nodeIdDisplay);
		wearConnected = true;
	}

	public void fetchDisconnectedNode(String nodeIdDisplay) {
		mConnectedNodes.remove(nodeIdDisplay);
		wearConnected = false;
	}

	public void fetchConnectedNodes(List<Node> connectedNodes) {
		if (connectedNodes.size() > 0) {
			for (Node node : connectedNodes) {
				mConnectedNodes.add(node.getId());
			}
			wearConnected = true;
		} else {
			wearConnected = false;
		}
	}

	public HashSet<String> getConnectedNodes() {
		return mConnectedNodes;
	}

	public boolean isWearConnected() {
		handler.post(fetchConnectedNodes);
		return wearConnected;
	}

	private Runnable fetchLocalNode = new Runnable() {
		@Override
		public void run() {
			final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(mContext).addApi(Wearable.API).build();

			mGoogleApiClient.connect();

			final PendingResult<NodeApi.GetLocalNodeResult> localNode = Wearable.NodeApi.getLocalNode(mGoogleApiClient);

			localNode.setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
				@Override
				public void onResult(NodeApi.GetLocalNodeResult result) {
					mNode = result.getNode();
					sendMessage(CONNECTED_PATH, mNode.getId());
				}
			});
		}
	};

	private Runnable fetchConnectedNodes = new Runnable() {
		@Override
		public void run() {
			final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(mContext).addApi(Wearable.API).build();
			mGoogleApiClient.connect();
			final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);

			nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
				@Override
				public void onResult(NodeApi.GetConnectedNodesResult result) {
					if (result.getNodes().isEmpty()) {
						wearConnected = false;
					} else {
						for (Node node : result.getNodes()) {
							mConnectedNodes.add(node.getId());
							wearConnected = true;
						}
					}
				}
			});
		}
	};
}