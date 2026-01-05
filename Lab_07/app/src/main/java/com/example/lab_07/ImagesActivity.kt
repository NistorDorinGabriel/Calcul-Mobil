package com.example.lab_07

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis

class ImagesActivity : AppCompatActivity() {

    private val imageUrls = List(9) { "http://cti.ubm.ro/cmo/digits/img$it.jpg" }

    private val adapter = ImageAdapter()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        val rv = findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = adapter

        loadImages()
    }

    private fun loadImages() {
        scope.launch {
            val bitmaps: List<Bitmap>
            val ms = measureTimeMillis {
                val downloaded = imageUrls.map { url ->
                    async { downloadImage(url) }
                }.awaitAll()

                bitmaps = downloaded.filterNotNull()
            }

            withContext(Dispatchers.Main) {
                Log.d("ImagesActivity", "Downloaded ${bitmaps.size}/9 in $ms ms")
                Toast.makeText(this@ImagesActivity, "Downloaded ${bitmaps.size}/9", Toast.LENGTH_SHORT).show()
                adapter.setItems(bitmaps)
            }
        }
    }

    private fun downloadImage(url: String): Bitmap? {
        return try {
            var currentUrl = url
            var redirects = 0

            while (redirects < 5) {
                val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    doInput = true
                    instanceFollowRedirects = false // urmărim noi ca să vedem Location
                }

                val code = conn.responseCode
                Log.d("ImagesActivity", "HTTP $code for $currentUrl")

                // Redirect?
                if (code in listOf(
                        HttpURLConnection.HTTP_MOVED_PERM,   // 301
                        HttpURLConnection.HTTP_MOVED_TEMP,   // 302
                        HttpURLConnection.HTTP_SEE_OTHER,    // 303
                        307, // Temporary Redirect
                        308  // Permanent Redirect
                    )
                ) {
                    val location = conn.getHeaderField("Location")
                    if (location.isNullOrBlank()) {
                        Log.e("ImagesActivity", "Redirect without Location header for $currentUrl")
                        return null
                    }

                    // Location poate fi relativ; îl rezolvăm corect
                    val nextUrl = URL(URL(currentUrl), location).toString()
                    Log.d("ImagesActivity", "Redirect -> $nextUrl")

                    currentUrl = nextUrl
                    redirects++
                    continue
                }

                if (code != HttpURLConnection.HTTP_OK) {
                    Log.e("ImagesActivity", "Non-200 response: $code ${conn.responseMessage} for $currentUrl")
                    return null
                }

                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                return conn.inputStream.use { BitmapFactory.decodeStream(it, null, opts) }
            }

            Log.e("ImagesActivity", "Too many redirects for $url")
            null
        } catch (e: Exception) {
            Log.e("ImagesActivity", "Download failed: $url", e)
            null
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
