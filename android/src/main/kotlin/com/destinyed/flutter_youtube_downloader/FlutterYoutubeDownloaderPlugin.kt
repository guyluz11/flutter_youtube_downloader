package com.destinyed.flutter_youtube_downloader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.SparseArray
import android.widget.Toast
import androidx.annotation.NonNull
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.destinyed.flutter_youtube_downloader.DownloadManagerClass

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterYoutubeDownloaderPlugin */
class FlutterYoutubeDownloaderPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var activity: Context


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_youtube_downloader")
        channel.setMethodCallHandler(this)

        activity = flutterPluginBinding.applicationContext

    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result): Unit {


        val url = call.argument<String>("url")
        val title = call.argument<String>("title")
        val iTag = call.argument<Int>("itag")


        when (call.method) {
            //Extract The Youtube Link
            "extractYoutubeLink" -> {
                @SuppressLint("StaticFieldLeak")
                val ytExtractor = object : YouTubeExtractor(activity) {
                    override fun onExtractionComplete(
                            ytFiles: SparseArray<YtFile>?,
                            videoMeta: VideoMeta?
                    ) {
                        if (ytFiles != null) {

                            // 720, 1080, 480
                            // var iTags = listOf(22, 137, 18);

                            val downloadUrl = ytFiles.get(iTag!!).url
                            result.success(downloadUrl)

                        } else {
                            result.error("Error", "Failed to get iTag Url", "")
                        }

                    }
                }
                ytExtractor.extract(url, true, true)
            }
            //Extract and download YouTube Video
            "downloadVideo" -> {
                downloadVideo(url!!, title!!, iTag!!)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    //Method responsible for extracting youtube link
    //This method will also call the DownloadManagerClass to download the
    //Youtube video after extracting the link
    private fun downloadVideo(url: String, title: String, itag: Int) {

        @SuppressLint("StaticFieldLeak")
        val ytExtractor = object : YouTubeExtractor(activity) {
            override fun onExtractionComplete(
                    ytFiles: SparseArray<YtFile>?,
                    videoMeta: VideoMeta?
            ) {
                try {
                    if (ytFiles != null) {

                    // 720, 1080, 480
                    // var iTags = listOf(22, 137, 18);

                    val downloadUrl = ytFiles.get(itag).url

                    DownloadManagerClass(activity).download(title, "Downloading...", downloadUrl)
                    


                } else {
                    Toast.makeText(
                            activity,
                            "Invalid iTag Url",
                            Toast.LENGTH_LONG
                    ).show()
                }
                }
                catch(e : Exception) {
                    print("This is the error message $e")
                    Toast.makeText(activity, "$e", Toast.LENGTH_LONG).show()
                    print(e)
                }

            }
        }
        ytExtractor.extract(url, true, true)
    }
}
