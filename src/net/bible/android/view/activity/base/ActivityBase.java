package net.bible.android.view.activity.base;

import net.bible.android.view.activity.navigation.History;
import net.bible.android.view.activity.page.MainBibleActivity;
import net.bible.android.view.util.UiUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

/** Base class for activities
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class ActivityBase extends Activity implements AndBibleActivity {

	// standard request code for startActivityForResult
	public static final int STD_REQUEST_CODE = 1;
	
	// Special result that requests all activities to exit until the main/top Activity is reached
    public static final int RESULT_RETURN_TO_TOP           = 900;

	private SharedActivityState sharedActivityState = SharedActivityState.getInstance();

	// some screens are highly customised and the theme looks odd if it changes
	private boolean allowThemeChange = true;
	
	private CommonActivityBase commonActivityBase = new CommonActivityBase();
	
	private static final String TAG = "ActivityBase";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.onCreate(savedInstanceState, false);
    }

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState, boolean integrateWithHistoryManager) {
    	if (allowThemeChange) {
    		UiUtils.applyTheme(this);
    	}

		super.onCreate(savedInstanceState);
    	
        Log.i(getLocalClassName(), "onCreate");
        
        // Register current activity in onCreate and onResume
        CurrentActivityHolder.getInstance().setCurrentActivity(this);

        // fix for null context class loader (http://code.google.com/p/android/issues/detail?id=5697)
        // this affected jsword dynamic classloading
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		
        setFullScreen(sharedActivityState.isFullScreen());
        

		commonActivityBase.setIntegrateWithHistoryManager(integrateWithHistoryManager);
    }
    
    @Override
	public void startActivity(Intent intent) {
    	commonActivityBase.beforeStartActivity();
    	
		super.startActivity(intent);
	}
	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
    	commonActivityBase.beforeStartActivity();

    	super.startActivityForResult(intent, requestCode);
	}

	/**	This will be called automatically for you on 2.0 or later
	 */
	@Override
	public void onBackPressed() {
		if (!commonActivityBase.goBack()) {
			super.onBackPressed();
		}
	}
	
    public void toggleFullScreen() {
    	sharedActivityState.toggleFullScreen();
    	setFullScreen(sharedActivityState.isFullScreen());
    }
    
	public boolean isFullScreen() {
		return sharedActivityState.isFullScreen();
	}
	
	private void setFullScreen(boolean isFullScreen) {
    	if (!isFullScreen) {
    		Log.d(TAG, "NOT Fullscreen");
    		// http://stackoverflow.com/questions/991764/hiding-title-in-a-fullscreen-mode
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	} else {
    		Log.d(TAG, "Fullscreen");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    	}
	}

	/** called by Android 2.0 +
	 */
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// ignore long press on search because it causes errors
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
	    	// ignore
			return true;
		}
		
		//TODO make Long press work for screens other than main window e.g. does not work from search screen because wrong window is displayed 
	    if (keyCode == KeyEvent.KEYCODE_BACK && this instanceof MainBibleActivity) {
			Log.d(TAG, "Back Long");
	        // a long press of the back key. do our work, returning true to consume it.  by returning true, the framework knows an action has
	        // been performed on the long press, so will set the cancelled flag for the following up event.
	    	Intent intent = new Intent(this, History.class);
	    	startActivityForResult(intent, 1);
	        return true;
	    }
	    
		//TODO make Long press back - currently the History screen does not show the correct screen after item selection if not called from main window 
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	// ignore
	    	return true;
	    }

	    return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean isIntegrateWithHistoryManager() {
		return commonActivityBase.isIntegrateWithHistoryManager();
	}
	@Override
	public void setIntegrateWithHistoryManager(boolean integrateWithHistoryManager) {
		commonActivityBase.setIntegrateWithHistoryManager(integrateWithHistoryManager);
	}
	
    /** allow activity to enhance intent to correctly restore state */
	public Intent getIntentForHistoryList() {
		return getIntent();
	}

	public void showErrorMsg(int msgResId) {
		Dialogs.getInstance().showErrorMsg(msgResId);
	}

    protected void showHourglass() {
    	Dialogs.getInstance().showHourglass();
    }
    protected void dismissHourglass() {
    	Dialogs.getInstance().dismissHourglass();
    }

    protected void returnErrorToPreviousScreen() {
    	// just pass control back to the previous screen
    	Intent resultIntent = new Intent(this, this.getClass());
    	setResult(Activity.RESULT_CANCELED, resultIntent);
    	finish();    
    }
    protected void returnToPreviousScreen() {
    	// just pass control back to the previous screen
    	Intent resultIntent = new Intent(this, this.getClass());
    	setResult(Activity.RESULT_OK, resultIntent);
    	finish();    
    }
    
    protected void returnToTop() {
    	// just pass control back to the previous screen
    	Intent resultIntent = new Intent(this, this.getClass());
    	setResult(RESULT_RETURN_TO_TOP, resultIntent);
    	finish();    
    }
    
	@Override
	protected void onResume() {
		super.onResume();
        Log.i(getLocalClassName(), "onResume");
        CurrentActivityHolder.getInstance().setCurrentActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
        Log.i(getLocalClassName(), "onPause");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
        Log.i(getLocalClassName(), "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
        Log.i(getLocalClassName(), "onStart");
	}


	@Override
	protected void onStop() {
		super.onStop();
        Log.i(getLocalClassName(), "onStop");
        // call this onStop, although it is not guaranteed to be called, to ensure an overlap between dereg and reg of current activity, otherwise AppToBackground is fired mistakenly
        CurrentActivityHolder.getInstance().iAmNoLongerCurrent(this);
	}

	public void setAllowThemeChange(boolean allowThemeChange) {
		this.allowThemeChange = allowThemeChange;
	}
}
