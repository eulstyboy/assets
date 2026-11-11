package fr.eulst.pluievelo.data

import android.graphics.BitmapFactory
import fr.eulst.pluievelo.core.DecodedImage
import fr.eulst.pluievelo.core.Fetcher
import fr.eulst.pluievelo.core.TileDecoder
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Decodes radar PNG tiles with the platform decoder. */
object AndroidTileDecoder : TileDecoder {
    override fun decode(png: ByteArray): DecodedImage? {
        val bmp = BitmapFactory.decodeByteArray(png, 0, png.size) ?: return null
        val px = IntArray(bmp.width * bmp.height)
        bmp.getPixels(px, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        return DecodedImage(bmp.width, bmp.height, px).also { bmp.recycle() }
    }
}

/**
 * HTTP GET with one retry. Radar tiles never change once published, so they are kept on disk:
 * a refresh then only downloads the newest frame.
 */
class HttpFetcher(private val cacheDir: File?) : Fetcher {
    override fun get(url: String): ByteArray {
        val cached = if (cacheDir != null && url.contains("tilecache.rainviewer.com")) File(cacheDir, key(url)) else null
        if (cached != null && cached.isFile && cached.length() > 0) return cached.readBytes()
        var last: IOException? = null
        repeat(2) { attempt ->
            try {
                val bytes = fetch(url)
                if (cached != null) runCatching {
                    cacheDir!!.mkdirs()
                    cached.writeBytes(bytes)
                    prune()
                }
                return bytes
            } catch (e: IOException) {
                last = e
                if (attempt == 0) Thread.sleep(700)
            }
        }
        throw last!!
    }

    private fun fetch(url: String): ByteArray {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 12000
            c.readTimeout = 20000
            c.setRequestProperty("User-Agent", "PluieVelo/1.0 (Android)")
            val code = c.responseCode
            if (code !in 200..299) throw IOException("HTTP $code")
            return c.inputStream.use { it.readBytes() }
        } finally {
            c.disconnect()
        }
    }

    private fun key(url: String): String =
        MessageDigest.getInstance("SHA-1").digest(url.toByteArray()).joinToString("") { "%02x".format(it) } + ".png"

    private fun prune() {
        val files = cacheDir?.listFiles() ?: return
        if (files.size <= 160) return
        files.sortedBy { it.lastModified() }.take(files.size - 120).forEach { it.delete() }
    }
}
