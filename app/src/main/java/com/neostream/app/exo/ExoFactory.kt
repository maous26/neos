package com.neostream.app.exo

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

@UnstableApi
object ExoFactory {

  fun okHttp(headers: Map<String, String>): DataSource.Factory {
    val client = OkHttpClient.Builder()
      .addInterceptor(object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
          val req = chain.request().newBuilder()
          headers.forEach { (k, v) -> req.addHeader(k, v) }
          return chain.proceed(req.build())
        }
      })
      .followRedirects(true)
      .followSslRedirects(true)
      .build()

    return OkHttpDataSource.Factory(client)
  }

  fun player(context: Context): ExoPlayer {
    val renderersFactory = DefaultRenderersFactory(context)
      .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

    return ExoPlayer.Builder(context)
      .setRenderersFactory(renderersFactory)
      .build()
  }
}
