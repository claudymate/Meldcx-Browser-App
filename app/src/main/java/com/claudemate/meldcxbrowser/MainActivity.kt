package com.claudemate.meldcxbrowser

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.claudemate.meldcxbrowser.room.History
import com.claudemate.meldcxbrowser.viewModel.HistoryViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.sql.Timestamp
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private final val TAG: String = MainActivity::class.java.simpleName
    public var previousUrl: String? = ""

    public lateinit var viewModel: HistoryViewModel
    public lateinit var sharedPref: SharedPreferences

    companion object {
        public val SP_PATH: String = "PATH"
        public val SELECTED_URL: String = "SELECTED_URL"
        public val DEFAULT_URL: String = "https://www.meldcx.com/category/press/"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set custom toolbar
        setSupportActionBar(toolbar)

        // Set up shared preferences
        sharedPref = getPreferences(Context.MODE_PRIVATE)

        //Request permission
        isStoragePermissionGranted()

        // Set up view model
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        //Set up webview
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.javaScriptEnabled = true

        //Get updates of page loading and URL
        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                // Updates the loading indicator
                progressBar.progress = newProgress
                editUrl.setText(view!!.url)

                // If the page is not fully loaded,
                // prevent the app from capturing the webview
                if (newProgress < 100) {
                    imgCapture.isEnabled = false
                } else {
                    imgCapture.isEnabled = true
                    webView.visibility = View.VISIBLE
                }
            }

            override fun onReceivedIcon(view: WebView?, bitmap: Bitmap?) {
                super.onReceivedIcon(view, bitmap)

                val currentUrl = editUrl.text.toString()

                // Insert to history_table
                if (!currentUrl.equals(previousUrl)) {

                    // Convert bitmap to byteArray
                    val stream = ByteArrayOutputStream()
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val icon = stream.toByteArray()

                    // Get current date and time
                    val cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(System.currentTimeMillis());
                    val dateTime = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();

                    // Insert to history_table
                    val history = History(0, currentUrl, dateTime, icon)
                    viewModel.insert(history)
                }

                previousUrl = currentUrl
            }
        }

        // Set up link navigation across the web application
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {
                view.loadUrl(url)
                return true
            }
        })

        // Set default values
        if (intent.hasExtra(SELECTED_URL)) {
            // When the user selected an item from history,
            // load the selected URL

            // Get the latest URL from history table and update the previousUrl variable.
            // To avoid inserting a URL that is same with the latest URL
            viewModel.viewModelScope.launch(Dispatchers.Default) {
                val history = viewModel.getRecent()
                if (history != null) {
                    previousUrl = history.url
                }
            }

            try {
                // Display the latest captured image when web view is not fully loaded
                placeRecentStoredImage()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            // Load the URL to the webview
            val url = intent.extras!!.getString(SELECTED_URL)
            editUrl.setText(url)
            webView.loadUrl(url)
        } else {

            // Upon opening the app, will set latest URL from history_table and load it to the web view.
            // If the table is empty, will use DEFAULT_URL variable
            viewModel.viewModelScope.launch(Dispatchers.Default) {
                // Fetch the recent history from history_table
                val history = viewModel.getRecent()
                var url = DEFAULT_URL
                if (history != null) {
                    url = history.url
                }

                // Load it to the webview.
                // It has to be processed within the webview thread
                webView.post {
                    editUrl.setText(url)
                    webView.loadUrl(url)
                }
            }
        }

        // Enable 'GO' action listener
        editUrl.setOnEditorActionListener(OnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_GO) {
                imgGo.performClick()
            }
            false
        })

        // Set OnClick listeners
        imgHome.setOnClickListener(this)
        imgGo.setOnClickListener(this)
        imgCapture.setOnClickListener(this)
        imgHistory.setOnClickListener(this)
    }

    // Call this function to capture the webview and save to gallery
    public fun captureWebview(): String {
        val config: Bitmap.Config = Bitmap.Config.ARGB_8888
        val bitmap = webView.drawToBitmap(config)
        val filename = "${UUID.randomUUID()}.jpg"
        return MediaStore.Images.Media.insertImage(
            getContentResolver(),
            bitmap,
            filename,
            "Web Screenshot"
        )
    }

    /**
     * Call this function to replace the webview with the latest captured image.
     * If the image is already not existing in the storage, this function will be ignored
     */
    @Throws(NullPointerException::class)
    public fun placeRecentStoredImage() {
        var path= sharedPref.getString(SP_PATH, null)
        path = getPathFromURI(Uri.parse(path))
        if (path != null) {
            val image = File(path);
            val bmOptions = BitmapFactory.Options();
            var bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height,true)
            imgHolder.setImageBitmap(bitmap)
            webView.visibility = View.INVISIBLE
        }
    }

    @Throws(NullPointerException::class)
    public fun getPathFromURI(contentURI: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(
            contentURI, projection, null,
            null, null)
            ?: return null
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        return if (cursor.moveToFirst()) {
            // cursor.close();
            cursor.getString(column_index)
        } else null
        // cursor.close();
    }

    /**
     * This overridden function is used to handle request permission result
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
        }
    }

    /**
     * Call this function to request a permission
     */
    public fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                true
            } else {
                // Permission is revoked
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else {
            // Permission is automatically granted on sdk < 23 upon installation
            // Permission is granted
            true
        }
    }

    /**
     * This overridden function is used to handle click events
     */
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.imgHome -> {
                webView.loadUrl(DEFAULT_URL)
            }
            R.id.imgGo -> {
                webView.loadUrl(editUrl.text.toString())
            }
            R.id.imgCapture -> {
                // Get URI of the image
                val path = captureWebview()

                // Save the path of the latest capture in shared preference
                with (sharedPref.edit()) {
                    putString(SP_PATH, path)
                    apply()
                }

                // Show snackbar with 'VIEW' action that lets the user to view the image
                val snackbar = Snackbar.make(view, getString(R.string.webpage_captured), Snackbar.LENGTH_LONG);
                snackbar.setAction(getString(R.string.view), object : View.OnClickListener {
                    override fun onClick(view: View?) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(path)));
                    }
                })
                snackbar.show()
            }
            R.id.imgHistory -> {

                // Go to history activity
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }
        }
    }

    /**
     * This overridden funtion is used to handle back button event
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                // Go back to previous web page
                webView.goBack();
            } else {
                // Close application
                finish()
            }
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }
}
