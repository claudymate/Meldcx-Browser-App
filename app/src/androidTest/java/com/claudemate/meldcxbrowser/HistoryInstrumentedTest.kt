package com.claudemate.meldcxbrowser

import android.view.View
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.claudemate.meldcxbrowser.room.History
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@MediumTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class HistoryInstrumentedTest {

    @get:Rule
    var rule = ActivityTestRule(HistoryActivity::class.java)

    private var activity: HistoryActivity? = null

    @Before
    fun setup() {
        activity = rule.getActivity()
    }

    @Test
    fun a_checkViewsVisible() {
        assertTrue(activity!!.rvHistory.visibility == View.VISIBLE);
        assertTrue(activity!!.editSearch.visibility == View.VISIBLE);
    }

    @Test
    fun b_checkInsertHistory() {
        val expectedCount = activity!!.adapter.itemCount + 1
        activity!!.viewModel.insert(History(0, "url", "date", ByteArray(0)))
        Thread.sleep(2000)
        assertEquals(expectedCount, activity!!.adapter.itemCount)
    }

    @Test
    fun c_checkSearchResult() {
        activity!!.findItem("url")
        Thread.sleep(2000)
        assertTrue(activity!!.adapter.itemCount > 0)
    }

    @Test
    fun d_checkClearHistory() {
        activity!!.viewModel.deleteAll()
        Thread.sleep(2000)
        assertEquals(0, activity!!.adapter.itemCount)
    }
}