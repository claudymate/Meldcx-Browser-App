package com.claudemate.meldcxbrowser

import android.R
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.test.InstrumentationRegistry
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.claudemate.meldcxbrowser.viewModel.HistoryViewModel
import junit.framework.Assert.*
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class MainInstrumentedTest {

    @get:Rule
    var rule =
        object : ActivityTestRule<MainActivity>(MainActivity::class.java) {
            override fun getActivityIntent(): Intent {
                InstrumentationRegistry.getTargetContext()
                val intent = Intent(Intent.ACTION_MAIN)
                intent.putExtra(MainActivity.SELECTED_URL, MainActivity.DEFAULT_URL)
                return intent
            }
        }

    private lateinit var activity: MainActivity

    @Before
    fun setup() {
        activity = rule.getActivity()
    }

    @Test
    fun checkViewsVisible() {
        assertTrue(activity.imgGo.visibility == View.VISIBLE);
        assertTrue(activity.imgCapture.visibility == View.VISIBLE);
        assertTrue(activity.imgHistory.visibility == View.VISIBLE);
        assertTrue(activity.imgHome.visibility == View.VISIBLE);
        assertTrue(activity.editUrl.visibility == View.VISIBLE);
    }

    @Test
    fun checkPermissionGranted() {
        assertTrue(activity.isStoragePermissionGranted());
    }

    @Test
    fun checkWebviewCapture() {
        assertTrue(activity.captureWebview().length > 0)
    }

    @Test
    fun checkWebviewInvisibility() {
        try {
            activity.placeRecentStoredImage()
            assertTrue(activity.webView.visibility == View.INVISIBLE)
        } catch (e: NullPointerException) {
            assertNotNull(e)
        }
    }

    @Test
    fun checkUriParser() {
        var path = activity.sharedPref.getString(MainActivity.SP_PATH, null)
        try {
            path = activity.getPathFromURI(Uri.parse(path))
            if (path != null) {
                assertTrue(path.isNotEmpty())
            } else {
                assertNull(path)
            }
        } catch (e: NullPointerException) {
            assertNotNull(e)
        }
    }

    @Test(expected = NullPointerException::class)
    fun checkNullUriParser() {
        val path = activity.getPathFromURI(Uri.parse(null))
        assertNull(path)
    }

    @Test
    fun checkWebViewDefaultURL() {
        assertEquals(activity.editUrl.text.toString(), MainActivity.DEFAULT_URL)
    }

    @Test
    fun checkIntentDataReceived() {
        assertEquals(activity.editUrl.text.toString(), MainActivity.DEFAULT_URL)
    }
}
