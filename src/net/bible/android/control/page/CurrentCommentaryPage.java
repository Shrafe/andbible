package net.bible.android.control.page;

import net.bible.android.activity.R;
import net.bible.android.control.ControlFactory;
import net.bible.android.view.activity.navigation.GridChoosePassageBook;

import org.crosswire.jsword.book.BookCategory;
import org.crosswire.jsword.passage.Key;
import org.crosswire.jsword.passage.KeyUtil;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.crosswire.jsword.passage.Verse;
import org.crosswire.jsword.versification.BibleInfo;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/** Reference to current passage shown by viewer
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class CurrentCommentaryPage extends CurrentPageBase implements CurrentPage {
	
	private CurrentBibleVerse currentBibleVerse;

	private static final String TAG = "CurrentCommentaryPage";
	
	
	/* default */ CurrentCommentaryPage(CurrentBibleVerse currentVerse) {
		super(true);
		// share the verse holder with the CurrentBiblePage
		this.currentBibleVerse = currentVerse;
	}

	public BookCategory getBookCategory() {
		return BookCategory.COMMENTARY;
	}

	public Class<? extends Activity> getKeyChooserActivity() {
		return GridChoosePassageBook.class;
	}

	/* (non-Javadoc)
	 * @see net.bible.android.control.CurrentPage#next()
	 */
	@Override
	public void next() {
		Log.d(TAG, "Next");
		nextVerse();
	}
	/* (non-Javadoc)
	 * @see net.bible.android.control.CurrentPage#previous()
	 */
	@Override
	public void previous() {
		Log.d(TAG, "Previous");
		previousVerse();
	}
	
	private void nextVerse() {
		setKey(getKeyPlus(1));
	}
	private void previousVerse() {
		setKey(getKeyPlus(-1));
	}
	
	/** add or subtract a number of pages from the current position and return Verse
	 */
	public Verse getKeyPlus(int num) {
		Verse currVer = this.currentBibleVerse.getVerseSelected();
		return currVer.add(num);
	}
	
	/** set key without notification
	 * 
	 * @param key
	 */
	protected void doSetKey(Key key) {
		if (key!=null) {
			Verse verse = KeyUtil.getVerse(key);
			currentBibleVerse.setVerseSelected(verse);
		}
	}

	/* (non-Javadoc)
	 * @see net.bible.android.control.CurrentPage#getKey()
	 */
	@Override
	public Key getKey() {
		return currentBibleVerse.getVerseSelected();
    }

	public boolean isSingleChapterBook() throws NoSuchKeyException{
    	return BibleInfo.chaptersInBook(currentBibleVerse.getCurrentBibleBook())==1;
	}
	
	public int getNumberOfVersesDisplayed() {
		return 1;
	}

	@Override
	public boolean isSingleKey() {
		return true;
	}
	public int getCurrentVerse() {
		return currentBibleVerse.getVerseNo();
	}
	public void setCurrentVerse(int verse) {
		currentBibleVerse.setVerseNo(verse);
		pageDetailChange();
	}
	
	/** can we enable the main menu search button 
	 */
	@Override
	public boolean isSearchable() {
		return true;
	}

	@Override
	public void updateContextMenu(Menu menu) {
		super.updateContextMenu(menu);
		// by default disable notes but bible and commentary will enable
		MenuItem myNotesMenuItem = menu.findItem(R.id.myNoteAddEdit);
		myNotesMenuItem.setVisible(true);
		myNotesMenuItem.setTitle(ControlFactory.getInstance().getMyNoteControl().getAddEditMenuText());
	}
}