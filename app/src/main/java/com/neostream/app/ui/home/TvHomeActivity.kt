package com.neostream.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.neostream.app.databinding.ActivityTvHomeBinding
import com.neostream.app.data.db.NeostreamDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TvHomeActivity : AppCompatActivity() {
  private lateinit var vb: ActivityTvHomeBinding

  private val importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == RESULT_OK) {
      Toast.makeText(this, "Playlist importée ✅", Toast.LENGTH_LONG).show()
      loadStats()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityTvHomeBinding.inflate(layoutInflater)
    setContentView(vb.root)

    setupMainCategories()
    loadStats()
  }

  override fun onResume() {
    super.onResume()
    loadStats()
  }

  private fun setupMainCategories() {
    // Live TV
    vb.cardLive.setOnClickListener {
      openCategory("live", "📺 TV en Direct")
    }

    // Séries
    vb.cardSeries.setOnClickListener {
      openCategory("series", "🎬 Séries")
    }

    // Films
    vb.cardMovies.setOnClickListener {
      openCategory("movie", "🎥 Films")
    }

    // Radio
    vb.cardRadio.setOnClickListener {
      openCategory("radio", "📻 Radio")
    }

    // Par Pays
    vb.cardCountries.setOnClickListener {
      openCountriesList()
    }

    // Par Qualité
    vb.cardQuality.setOnClickListener {
      openQualityFilter()
    }

    // Import
    vb.cardImport.setOnClickListener {
      importLauncher.launch(Intent().setClassName(this, "com.neostream.app.ui.imports.AddSourceActivity"))
    }
  }

  private fun loadStats() {
    lifecycleScope.launch {
      try {
        val dao = NeostreamDb.get(this@TvHomeActivity).dao()
        val live = withContext(Dispatchers.IO) { dao.topGroups("live").sumOf { it.cnt } }
        val series = withContext(Dispatchers.IO) { dao.topGroups("series").sumOf { it.cnt } }
        val movies = withContext(Dispatchers.IO) { dao.topGroups("movie").sumOf { it.cnt } }
        val radio = withContext(Dispatchers.IO) { dao.topGroups("radio").sumOf { it.cnt } }

        vb.tvLiveCount.text = "$live chaînes"
        vb.tvSeriesCount.text = "$series épisodes"
        vb.tvMoviesCount.text = "$movies films"
        vb.tvRadioCount.text = "$radio stations"

        vb.tvLiveCount.visibility = View.VISIBLE
        vb.tvSeriesCount.visibility = View.VISIBLE
        vb.tvMoviesCount.visibility = View.VISIBLE
        vb.tvRadioCount.visibility = View.VISIBLE
      } catch (_: Exception) {
        // ignore
      }
    }
  }

  private fun openCategory(kind: String, title: String) {
    startActivity(Intent().setClassName(this, "com.neostream.app.ui.browse.CategoryBrowseActivity").apply {
      putExtra("kind", kind)
      putExtra("title", title)
    })
  }

  private fun openCountriesList() {
    startActivity(Intent().setClassName(this, "com.neostream.app.ui.browse.CountriesActivity"))
  }

  private fun openQualityFilter() {
    startActivity(Intent().setClassName(this, "com.neostream.app.ui.browse.QualityFilterActivity"))
  }
}
