package com.neostream.app.ui.browse

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neostream.app.databinding.ActivityQualityFilterBinding
import android.view.View

class QualityFilterActivity : AppCompatActivity() {
  private lateinit var vb: ActivityQualityFilterBinding

  private val qualities = listOf(
    Quality("💎", "uhd", "Ultra HD (4K)", "La meilleure qualité disponible"),
    Quality("🔷", "fhd", "Full HD (1080p)", "Haute définition complète"),
    Quality("📺", "hd", "HD (720p)", "Haute définition"),
    Quality("🎬", "hevc", "HEVC/H.265", "Encodage moderne, bonne compression")
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityQualityFilterBinding.inflate(layoutInflater)
    setContentView(vb.root)

    vb.btnBack.setOnClickListener { finish() }

    val adapter = QualityAdapter { quality ->
      openChannelsByQuality(quality)
    }
    
    vb.recyclerView.layoutManager = LinearLayoutManager(this)
    vb.recyclerView.adapter = adapter
    adapter.submitList(qualities)
  }

  private fun openChannelsByQuality(quality: Quality) {
    val intent = Intent().setClassName(this, "com.neostream.app.ui.browse.ChannelsGridActivity").apply {
      putExtra("kind", "live")
      putExtra("quality", quality.code)
      putExtra("title", "${quality.icon} ${quality.name}")
    }
    startActivity(intent)
  }
}

data class Quality(val icon: String, val code: String, val name: String, val description: String)

class QualityAdapter(
  private val onQualityClick: (Quality) -> Unit
) : RecyclerView.Adapter<QualityAdapter.ViewHolder>() {

  private var qualities = listOf<Quality>()

  fun submitList(list: List<Quality>) {
    qualities = list
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
    val view = android.view.LayoutInflater.from(parent.context)
      .inflate(android.R.layout.simple_list_item_2, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(qualities[position])
  }

  override fun getItemCount() = qualities.size

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val text1 = view.findViewById<android.widget.TextView>(android.R.id.text1)
    private val text2 = view.findViewById<android.widget.TextView>(android.R.id.text2)

    fun bind(quality: Quality) {
      text1.text = "${quality.icon} ${quality.name}"
      text2.text = quality.description
      itemView.setOnClickListener { onQualityClick(quality) }
      
      // Styling
      text1.textSize = 20f
      text1.setTextColor(android.graphics.Color.WHITE)
      text2.textSize = 14f
      text2.setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
      itemView.setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
      itemView.setPadding(32, 32, 32, 32)
    }
  }
}
