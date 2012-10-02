package net.bible.service.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import net.bible.android.control.page.CurrentPage;
import net.bible.android.control.page.CurrentPageManager;
import net.bible.android.view.activity.base.AndBibleActivity;
import net.bible.android.view.activity.base.CurrentActivityHolder;
import net.bible.android.view.activity.page.MainBibleActivity;

import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.passage.Key;

import android.app.Activity;
import android.util.Log;

/**
 * Application managed History List
 * 
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's authors.
 * @author Martin Denham [mjdenham at gmail dot com]
 */
public class HistoryManager {

	private static int MAX_HISTORY = 80;
	private Stack<HistoryItem> history = new Stack<HistoryItem>();
	private static HistoryManager singleton = new HistoryManager();

	private boolean isGoingBack = false;
	
	private static final String TAG = "HistoryManager";
	
	public static HistoryManager getInstance() {
		return singleton;
	}
	
	public boolean canGoBack() {
		return history.size()>0;
	}
	
	/**
	 *  called when a verse is changed to allow current Activity to be saved in History list
	 */
	public void beforePageChange() {
		// if we cause the change by requesting Back then ignore it
		if (!isGoingBack) {
			HistoryItem item = createHistoryItem();
			add(history, item);
		}
	}
	private HistoryItem createHistoryItem() {
		HistoryItem historyItem = null;
		
		Activity currentActivity = CurrentActivityHolder.getInstance().getCurrentActivity();
		if (currentActivity instanceof MainBibleActivity) {
			CurrentPage currentPage = CurrentPageManager.getInstance().getCurrentPage();
			Book doc = currentPage.getCurrentDocument();
			if (currentPage.getKey()==null) {
				return null;
			}
			
			Key key = currentPage.getSingleKey();
			float yOffsetRatio = currentPage.getCurrentYOffsetRatio();
			historyItem = new KeyHistoryItem(doc, key, yOffsetRatio);
		} else if (currentActivity instanceof AndBibleActivity) {
			AndBibleActivity andBibleActivity = (AndBibleActivity)currentActivity;
			if (andBibleActivity.isIntegrateWithHistoryManager()) {
				historyItem = new IntentHistoryItem(currentActivity.getTitle(), ((AndBibleActivity) currentActivity).getIntentForHistoryList());
			}
		}
		return historyItem;
	}
	
	public void goBack() {
		if (history.size()>0) {
			try {
				Log.d(TAG, "History size:"+history.size());
				isGoingBack = true;
	
				// pop the previous item
				HistoryItem previousItem = history.pop();
	
				if (previousItem!=null) {
					Log.d(TAG, "Going back to:"+previousItem);
					previousItem.revertTo();
					
					// finish current activity if not the Main screen
					Activity currentActivity = CurrentActivityHolder.getInstance().getCurrentActivity();
					if (!(currentActivity instanceof MainBibleActivity)) {
						currentActivity.finish();
					}
				}
			} finally {
				isGoingBack = false;
			}
		}
	}
	
	public List<HistoryItem> getHistory() {
		List<HistoryItem> allHistory = new ArrayList<HistoryItem>(history);
		// reverse so most recent items are at top rather than end
		Collections.reverse(allHistory);
		return allHistory;
	}
	
	/** add item and check size of stack
	 * 
	 * @param stack
	 * @param item
	 */
	private synchronized void add(Stack<HistoryItem> stack, HistoryItem item) {
		if (item!=null) {
			if (stack.isEmpty() || !item.equals(stack.peek())) {
				Log.d(TAG, "Adding "+item+" to history");
				Log.d(TAG, "Stack size:"+stack.size());
				
				stack.push(item);
				
				while (stack.size()>MAX_HISTORY) {
					Log.d(TAG, "Shrinking large stack");
					stack.remove(0);
				}
			}
		}
	}
}
