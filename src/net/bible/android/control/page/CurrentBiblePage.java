package net.bible.android.control.page;

import net.bible.android.activity.R;
import net.bible.android.control.ControlFactory;
import net.bible.android.view.activity.navigation.GridChoosePassageBook;
import net.bible.service.sword.SwordDocumentFacade;

import org.apache.commons.lang.StringUtils;
import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.book.BookCategory;
import org.crosswire.jsword.passage.Key;
import org.crosswire.jsword.passage.KeyUtil;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.crosswire.jsword.passage.NoSuchVerseException;
import org.crosswire.jsword.passage.Verse;
import org.crosswire.jsword.passage.VerseRange;
import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.BibleInfo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/** Reference to current passage shown by viewer
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class CurrentBiblePage extends CurrentPageBase implements CurrentPage {
	
	private CurrentBibleVerse currentBibleVerse;

	private static final String TAG = "CurrentBiblePage";
	
	
	/* default */ CurrentBiblePage(CurrentBibleVerse currentVerse) {
		super(true);
		// share the verse holder with the CurrentCommentaryPage
		this.currentBibleVerse = currentVerse;
	}

	public BookCategory getBookCategory() {
		return BookCategory.BIBLE;
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
		nextChapter();
	}
	/* go to prev verse quietly without updates
	 */
	public void doPreviousVerse() {
		Log.d(TAG, "Previous");
		Verse verse = currentBibleVerse.getVerseSelected();
		currentBibleVerse.setVerseSelected(verse.subtract(1));
	}
	
	/* go to next verse quietly without updates
	 */
	public void doNextVerse() {
		Log.d(TAG, "NextVerse");
		Verse verse = currentBibleVerse.getVerseSelected();
		currentBibleVerse.setVerseSelected(verse.add(1));
	}
	
	/* (non-Javadoc)
	 * @see net.bible.android.control.CurrentPage#previous()
	 */
	@Override
	public void previous() {
		Log.d(TAG, "Previous");
		previousChapter();
	}

	private void nextChapter() {
		setKey( getKeyPlus(+1) );
	}
	
	private void previousChapter() {
		setKey( getKeyPlus(-1) );
	}

	/** add or subtract a number of pages from the current position and return Verse
	 */
	public Verse getKeyPlus(int num) {
		Verse currVer = this.currentBibleVerse.getVerseSelected();
		BibleBook book = currVer.getBook();
		int chapter = currVer.getChapter();

		try {
			if (num>=0) {
				// allow verse correction to move to next book if required
				chapter = chapter+num;
			} else {
				if (chapter>1) {
					chapter--;
				} else {
					if (BibleBook.GEN.compareTo(book)<0) {
						book = BibleBook.getBooks()[book.ordinal()-1];
						chapter = BibleInfo.chaptersInBook(book);
					}
				}
			}
		
			return new Verse(book, chapter, 1, true);
		} catch (NoSuchVerseException nsve) {
			Log.e(TAG, "Incorrect verse", nsve);
			return currVer;
		}
	}
	
	/** add or subtract a number of pages from the current position and return Page
	 */
	public Key getPagePlus(int num) {
		Verse targetChapterVerse1 = getKeyPlus(num);
		// convert to full chapter before returning because bible view is for a full chapter
		return new VerseRange(targetChapterVerse1.getFirstVerseInChapter(), targetChapterVerse1.getLastVerseInChapter());
	}

	/* (non-Javadoc)
	 * @see net.bible.android.control.CurrentPage#setKey(java.lang.String)
	 */
	public void setKey(String keyText) {
		Log.d(TAG, "key text:"+keyText);
		try {
			Key key = getCurrentDocument().getKey(keyText);
			setKey(key);
		} catch (NoSuchKeyException nske) {
			Log.e(TAG, "Invalid verse reference:"+keyText);
		}
	}

	
	/** set key without notification
	 * 
	 * @param key
	 */
	public void doSetKey(Key key) {
		Log.d(TAG, "Bible key set to:"+key);
		if (key!=null) {
			Verse verse = KeyUtil.getVerse(key);
			currentBibleVerse.setVerseSelected(verse);
		}
	}

	/* (non-Javadoc)
	 * @see net.bible.android.control.CurrentPage#getSingleKey()
	 */
	@Override
	public Verse getSingleKey() {
		Key key = doGetKey(true);
		// it is already a Verse but this avoids a downcast
		return KeyUtil.getVerse(key);
    }
	
	/* (non-Javadoc)
	 * @see net.bible.android.control.CurrentPage#getKey()
	 */
	@Override
	public Key getKey() {
		return doGetKey(false);
    }

	private Key doGetKey(boolean requireSingleKey) {
		Verse verse = currentBibleVerse.getVerseSelected();
		if (verse!=null) {
			Key key;
			if (!requireSingleKey) {
				// display whole page of bible so return whole chapter key - not just teh single verse even if a single verse was set in verseKey
				// if verseNo is required too then use getVerse()
		        Key wholeChapterKey = new VerseRange(verse.getFirstVerseInChapter(), verse.getLastVerseInChapter());
		        key = wholeChapterKey;
			} else {
				key = verse;
			}
			return key;
		} else {
			return new Verse(BibleBook.GEN,1,1, true);
		}
    }

	@Override
	public boolean isSingleKey() {
		return false;
	}
	
	public int getCurrentVerseNo() {
		return currentBibleVerse.getVerseNo();
	}
	public void setCurrentVerseNo(int verse) {
		currentBibleVerse.setVerseNo(verse);
		pageDetailChange();
	}

	public boolean isSingleChapterBook() throws NoSuchKeyException{
    	return BibleInfo.chaptersInBook(currentBibleVerse.getCurrentBibleBook())==1;
	}
	
	/** called during app close down to save state
	 * 
	 * @param outState
	 */
	@Override
	public void saveState(SharedPreferences outState) {
		if (getCurrentDocument()!=null && currentBibleVerse!=null && currentBibleVerse.getVerseSelected()!=null) {
			Log.d(TAG, "Saving state");
			SharedPreferences.Editor editor = outState.edit();
			editor.putString("document", getCurrentDocument().getInitials());
			// dec/inc bibleBook on save/restore because the book no used to be 1 based, unlike now
			editor.putInt("bible-book", currentBibleVerse.getCurrentBibleBookNo()+1);
			editor.putInt("chapter", currentBibleVerse.getVerseSelected().getChapter());
			editor.putInt("verse", currentBibleVerse.getVerseNo());
			editor.commit();
		}
	}
	/** called during app start-up to restore previous state
	 * 
	 * @param inState
	 */
	@Override
	public void restoreState(SharedPreferences inState) {
		if (inState!=null) {
			Log.d(TAG, "State not null");
			String document = inState.getString("document", null);
			if (StringUtils.isNotEmpty(document)) {
				Log.d(TAG, "State document:"+document);
				Book book = SwordDocumentFacade.getInstance().getDocumentByInitials(document);
				if (book!=null) {
					Log.d(TAG, "Document:"+book.getName());
					// bypass setter to avoid automatic notifications
					localSetCurrentDocument(book);
				}
			}

			// bypass setter to avoid automatic notifications
			int bibleBookNo =  inState.getInt("bible-book", -1);
			int chapterNo = inState.getInt("chapter", 1);
			int verseNo = inState.getInt("verse", 1);
			if (bibleBookNo>0) {
				Log.d(TAG, "Restored verse:"+bibleBookNo+"."+chapterNo+"."+verseNo);
				// dec/inc bibleBook on save/restore because the book no used to be 1 based, unlike now
				Verse verse = new Verse(BibleBook.getBooks()[bibleBookNo-1], chapterNo, verseNo, true);
				this.currentBibleVerse.setVerseSelected(verse);
			}
			Log.d(TAG, "Current passage:"+toString());
		} 
	}

	/** can we enable the main menu search button 
	 */
	@Override
	public boolean isSearchable() {
		try {
			//TODO allow japanese search - japanese bibles use smartcn which is not available
			return !"ja".equals(getCurrentDocument().getLanguage().getCode());
		} catch (Exception e) {
			Log.w(TAG,  "Missing language code", e);
			return true;
		}
	}

	@Override
	public void updateContextMenu(Menu menu) {
		super.updateContextMenu(menu);
		// by default disable notes but bible will enable
		menu.findItem(R.id.notes).setVisible(true);
		
		// by default disable mynotes but bible and commentary will enable
		MenuItem myNotesMenuItem = menu.findItem(R.id.myNoteAddEdit);
		myNotesMenuItem.setVisible(true);
		myNotesMenuItem.setTitle(ControlFactory.getInstance().getMyNoteControl().getAddEditMenuText());

		// by default disable compare translation except for Bibles
		menu.findItem(R.id.compareTranslations).setVisible(true);
	}
}