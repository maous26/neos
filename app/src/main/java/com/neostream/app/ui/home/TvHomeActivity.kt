package com.neostream.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neostream.app.databinding.ActivityTvHomeBinding
import com.neostream.app.data.db.NeostreamDb
import com.neostream.app.data.db.TopGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TvHomeActivity : AppCompatActivity() {
  private lateinit var vb: ActivityTvHomeBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityTvHomeBinding.inflate(layoutInflater)
    setContentView(vb.root)

    setupMainCategories()
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
      startActivity(Intent().setClassName(this, "com.neostream.app.ui.imports.AddSourceActivity"))
    }
  }

  private fun loadStats() {
    lifecycleScope.launch {
      try {
        val dao = NeostreamDb.get(this@TvHomeActivity).dao()
        
        // Count by kind
        val liveGroups = withContext(Dispatchers.IO) { dao.topGroups("live") }
        val seriesGroups = withContext(Dispatchers.IO) { dao.topGroups("series") }
        val moviesGroups = withContext(Dispatchers.IO) { dao.topGroups("movie") }
        val radioGroups = withContext(Dispatchers.IO) { dao.topGroups("radio") }

        val liveCount = liveGroups.sumOf { it.cnt }
        val seriesCount = seriesGroups.sumOf { it.cnt }
        val moviesCount = moviesGroups.sumOf { it.cnt }
        val radioCount = radioGroups.sumOf { it.cnt }

        vb.tvLiveCount.text = "$liveCount chaînes"
        vb.tvSeriesCount.text = "$seriesCount épisodes"
        vb.tvMoviesCount.text = "$moviesCount films"
        vb.tvRadioCount.text = "$radioCount stations"

        vb.tvLiveCount.visibility = View.VISIBLE
        vb.tvSeriesCount.visibility = View.VISIBLE
        vb.tvMoviesCount.visibility = View.VISIBLE
        vb.tvRadioCount.visibility = View.VISIBLE
      } catch (e: Exception) {
        // Si erreur, on cache juste les compteurs
      }
    }
  }

  private fun openCategory(kind: String, title: String) {
    val intent = Intent().setClassName(this, "com.neostream.app.ui.browse.CategoryBrowseActivity").apply {
      putExtra("kind", kind)
      putExtra("title", title)
    }
    startActivity(intent)
  }

  private fun openCountriesList() {
    startActivity(Intent().setClassName(this, "com.neostream.app.ui.browse.CountriesActivity"))
  }

  private fun openQualityFilter() {
    startActivity(Intent().setClassName(this, "com.neostream.app.ui.browse.QualityFilterActivity"))
  }
}
