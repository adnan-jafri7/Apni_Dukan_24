package com.sabziwala.shop


import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.sabziwala.shop.BuildConfig
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: RelativeLayout
    lateinit var navigationView: BottomNavigationView
    lateinit var webView: WebView
    lateinit var progressBar: ProgressBar
    lateinit var loadingView: RelativeLayout
    lateinit var voiceText: String
    var sharedPreferences: SharedPreferences? = null
    var url: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        webView = findViewById(R.id.WebView)
        progressBar = findViewById(R.id.ProgressBar)
        loadingView = findViewById(R.id.LoadingView)
        navigationView = findViewById(R.id.navigation_view)
        isUpdateAvailable()
        getUrl()
        setUpToolbar("Home")
        navigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item?.itemId) {
                R.id.home -> {
                    loadingView.visibility = View.VISIBLE
                    setUpToolbar("Home")
                    loadPage("$url")
                }
                R.id.products -> {
                    loadingView.visibility = View.VISIBLE
                    setUpToolbar("Shop")
                    loadPage("${url}shop-1")
                }
                R.id.myAccount -> {
                    loadingView.visibility = View.VISIBLE
                    setUpToolbar("My Account")
                    loadPage("${url}account/my-account")
                }
                R.id.contact -> {
                    loadingView.visibility = View.VISIBLE
                    setUpToolbar("Contact Us")
                    loadPage("${url}contact-us-feedback")
                }
                R.id.voice -> {
                    promptSpeechInput()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }


    }

    fun isUpdateAvailable() {
        var versionCode=BuildConfig.VERSION_CODE
        //Toast.makeText(this,"$versionCode",Toast.LENGTH_SHORT).show()
        if (ConnectionManager().checkConnectivity(this@MainActivity)) {
            val queue = Volley.newRequestQueue(this)
            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET,
                "https://bill-payment-app-74ff9-default-rtdb.firebaseio.com/.json",
                null,
                Response.Listener { response ->
                    //Toast.makeText(activity as Context,"$response",Toast.LENGTH_SHORT).show()
                    try {
                        var update = response.getString("update")
                        var versionUpgraded=response.getString("version").toInt()
                        //Toast.makeText(this, "$update", Toast.LENGTH_SHORT).show()
                        if ((update == "1") && (versionCode<versionUpgraded) ) {
                            val dialog = AlertDialog.Builder(this@MainActivity)
                            dialog.setTitle("Update Available")
                            dialog.setMessage("Please update your app to get latest features.")
                            dialog.setCancelable(false)
                            dialog.setPositiveButton("Update") { text, listener ->
                                intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.sabziwala.shop")
                                )
                                startActivity(intent)

                            }
                            dialog.setNegativeButton("Cancel") { text, listener ->
                            }
                            dialog.create()
                            dialog.show()
                        } else {
                            getUrl()
                        }

                        //Toast.makeText(activity as Context,"$i",Toast.LENGTH_SHORT).show()

                    } catch (e1: JSONException) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error: VolleyError? ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                }) {

                /*Send the headers using the below method*/
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    //headers["Content-type"] = "application/json"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            val dialog = AlertDialog.Builder(this@MainActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setCancelable(false)
            dialog.setPositiveButton("Retry") { text, listener ->
                getUrl()
            }
            dialog.setNegativeButton("Cancel") { text, listener ->
                finish()
            }
            dialog.create()
            dialog.show()
        }


    }

    private fun promptSpeechInput() {
        val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        try {
            startActivityForResult(intent, REQUEST_CODE)
        } catch (a: ActivityNotFoundException) {
        }
    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    voiceText = result[0]
                }

            }
        }
        loadPage("${url}search-results/q-$voiceText")
    }


    fun setUpToolbar(title: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = title

    }
    override  fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        if(id==R.id.action_share){
            val intent=Intent(android.content.Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=com.sabziwala.shop"
            )
            startActivity(Intent.createChooser(intent, "Share"))
        }
        return super.onOptionsItemSelected(item)
    }


    fun loadPage(url: String) {
        if (ConnectionManager().checkConnectivity(this@MainActivity)) {
            webView.webViewClient = WebViewClient()
            webView.settings.loadsImagesAutomatically = true
            webView.settings.setSupportZoom(true)
            webView.setOnLongClickListener({ v -> true })
            webView.isLongClickable = false
            webView.settings.javaScriptEnabled = true
            webView.settings.setSupportMultipleWindows(true)
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = false
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            webView.settings.domStorageEnabled = true
            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
            webView.settings.useWideViewPort = true
            webView.settings.savePassword = true
            webView.settings.saveFormData = true
            webView.settings.setEnableSmoothTransition(true)
            webView.loadUrl(url)
            webView.webViewClient=object:WebViewClient(){
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if(url.contains("tel:")){
                        val intent=Intent(Intent.ACTION_DIAL)
                        intent.setData(Uri.parse(url))
                        startActivity(intent)
                    }
                    else if(url.contains("mailto:")){
                        val intent=Intent(Intent.ACTION_SEND)
                        val mail=url.substring(7)
                        var email=arrayOf(mail)
                        intent.putExtra(Intent.EXTRA_EMAIL, email)
                        intent.setType("text/html")
                        intent.setPackage("com.google.android.gm")
                        startActivity(intent)
                    }
                    else{
                    view.loadUrl(url)
                    loadingView.visibility=View.VISIBLE}
                    return true
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    view?.loadUrl("about:blank")
                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Some error occurred")
                    dialog.setPositiveButton("Retry") { text, listener ->
                        getUrl()
                    }
                    dialog.setNegativeButton("Cancel") { text, listener ->
                        ActivityCompat.finishAffinity(this@MainActivity)
                    }
                    dialog.create()
                    dialog.show()
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    super.onPageCommitVisible(view, url)
                    loadingView.visibility=View.GONE
                }

            }
        } else {
            val dialog = AlertDialog.Builder(this@MainActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Retry") { text, listener ->
                loadPage(url)
            }
            dialog.setNegativeButton("Cancel") { text, listener ->
                ActivityCompat.finishAffinity(this@MainActivity)
            }
            dialog.create()
            dialog.show()
        }

    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)


    }
    fun getUrl() {
            if (ConnectionManager().checkConnectivity(this@MainActivity)) {
                val queue = Volley.newRequestQueue(this)
                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    "https://bill-payment-app-74ff9-default-rtdb.firebaseio.com/.json",
                    null,
                    Response.Listener { response ->
                        //Toast.makeText(activity as Context,"$response",Toast.LENGTH_SHORT).show()
                        try {
                            url = response.getString("url")
                            loadPage("${url}")

                        } catch (e1: JSONException) {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { error: VolleyError? ->
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }) {

                    /*Send the headers using the below method*/
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        //headers["Content-type"] = "application/json"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)
            } else {
                val dialog = AlertDialog.Builder(this@MainActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection not Found")
                dialog.setCancelable(false)
                dialog.setPositiveButton("Retry") { text, listener ->
                    getUrl()
                }
                dialog.setNegativeButton("Cancel") { text, listener ->
                    ActivityCompat.finishAffinity(this@MainActivity)
                }
                dialog.create()
                dialog.show()
            }

        }
    }




