package mil.nga.giat.chronostouch.pipe;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * This service listens for changes on data layer made by either mobile or wearable.  For each event change, the dataItem is added to the DataManager and a broadcast is made to listening class(es) to retrieve the updated data.
 */
public class DataListenerService extends WearableListenerService {
	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

		for (DataEvent event : events) {
			if (event.getType() == DataEvent.TYPE_CHANGED) {
				DataItem item = event.getDataItem();
				DataManager.getInstance(this).fetchDataFromService(item);
			} else if (event.getType() == DataEvent.TYPE_DELETED) {
				// TODO: What does it mean when the DataItem was deleted?
			}
		}
	}

	@Override
	public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
		DataManager.getInstance(this).updateCapability(capabilityInfo);
	}

	@Override
	public void onConnectedNodes(List<Node> connectedNodes) {
		DataManager.getInstance(this).fetchConnectedNodes(connectedNodes);
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		DataManager.getInstance(this).fetchMessageEvent(messageEvent);
	}

	@Override
	public void onPeerConnected(Node peer) {
		DataManager.getInstance(this).fetchConnectedNode(peer.getId());
	}

	@Override
	public void onPeerDisconnected(Node peer) {
		DataManager.getInstance(this).fetchDisconnectedNode(peer.getId());
	}
}