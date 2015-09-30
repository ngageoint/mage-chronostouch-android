package mil.nga.giat.chronostouch.gesture;

import android.gesture.GestureOverlayView;
import android.view.MotionEvent;

import com.android.internal.util.Predicate;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wiedemanns on 9/24/15.
 */
public class OnDelayedGestureListener implements GestureOverlayView.OnGestureListener {

	protected boolean isFirstStroke = true;
	protected Timer gestureFinishedTimer;

	protected Object mGesture;

	final protected Predicate<GestureOverlayView> mOnGestureStarted;
	final protected Predicate<Object> mOnGestureCreated;

	public OnDelayedGestureListener(Predicate<GestureOverlayView> onGestureStarted, Predicate<Object> onGestureCreated) {
		mOnGestureStarted = onGestureStarted;
		mOnGestureCreated = onGestureCreated;
	}

	@Override
	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
		mOnGestureStarted.apply(overlay);
		if (isFirstStroke) {
			mGesture = null;

		} else {
			gestureFinishedTimer.cancel();
		}
		gestureFinishedTimer = new Timer();
	}

	@Override
	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
		isFirstStroke = true;
	}

	@Override
	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
		isFirstStroke = false;
		mGesture = overlay.getGesture();

		gestureFinishedTimer.schedule(new FinishedTask(), 1000);
	}


	@Override
	public void onGesture(GestureOverlayView overlay, MotionEvent event) {

	}

	protected class FinishedTask extends TimerTask {
		public void run() {
			gestureFinishedTimer.cancel(); //Terminate the timer thread
			mOnGestureCreated.apply(mGesture);
		}
	}
}
