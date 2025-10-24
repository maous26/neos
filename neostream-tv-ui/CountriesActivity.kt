package com.neostream.app.ui.browse

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neostream.app.databinding.ActivityCountriesBinding
import android.view.View

class CountriesActivity : AppCompatActivity() {
  private lateinit var vb: ActivityCountriesBinding

  private val countries = listOf(
    Country("🇫🇷", "FR", "France"),
    Country("🇸🇦", "AR", "Arabe"),
    Country("🇪🇸", "ES", "Espagne"),
    Country("🇮🇹", "IT", "Italie"),
    Country("🇺🇸", "US", "États-Unis"),
    Country("🇹🇷", "TR", "Turquie"),
    Country("🇬🇧", "UK", "Royaume-Uni"),
    Country("🇧🇪", "BE", "Belgique"),
    Country("🇨🇦", "CA", "Canada"),
    Country("🇩🇪", "DE", "Allemagne"),
    Country("🇨🇭", "CH", "Suisse"),
    Country("🇳🇱", "NL", "Pays-Bas"),
    Country("🇷🇴", "RO", "Roumanie"),
    Country("🇧🇷", "BR", "Brésil"),
    Country("🇸🇪", "SE", "Suède"),
    Country("🇵🇰", "PK", "Pakistan"),
    Country("🇷🇸", "RS", "Serbie"),
    Country("🇦🇱", "AL", "Albanie"),
    Country("🇮🇳", "IN", "Inde"),
    Country("🇸🇬", "SG", "Singapour")
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityCountriesBinding.inflate(layoutInflater)
    setContentView(vb.root)

    vb.btnBack.setOnClickListener { finish() }

    val adapter = CountriesAdapter { country ->
      openChannelsByCountry(country)
    }
    
    vb.recyclerView.layoutManager = GridLayoutManager(this, 3)
    vb.recyclerView.adapter = adapter
    adapter.submitList(countries)
  }

  private fun openChannelsByCountry(country: Country) {
    val intent = Intent().setClassName(this, "com.neostream.app.ui.browse.ChannelsGridActivity").apply {
      putExtra("kind", "live")  // Most country content is live
      putExtra("country", country.code)
      putExtra("title", "📺 ${country.name}")
    }
    startActivity(intent)
  }
}

data class Country(val flag: String, val code: String, val name: String)

class CountriesAdapter(
  private val onCountryClick: (Country) -> Unit
) : RecyclerView.Adapter<CountriesAdapter.ViewHolder>() {

  private var countries = listOf<Country>()

  fun submitList(list: List<Country>) {
    countries = list
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
    val view = android.view.LayoutInflater.from(parent.context)
      .inflate(android.R.layout.simple_list_item_2, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(countries[position])
  }

  override fun getItemCount() = countries.size

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val text1 = view.findViewById<android.widget.TextView>(android.R.id.text1)
    private val text2 = view.findViewById<android.widget.TextView>(android.R.id.text2)

    fun bind(country: Country) {
      text1.text = "${country.flag} ${country.name}"
      text2.text = country.code
      itemView.setOnClickListener { onCountryClick(country) }
      
      // Styling
      text1.textSize = 18f
      text1.setTextColor(android.graphics.Color.WHITE)
      text2.setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
      itemView.setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
      itemView.setPadding(24, 24, 24, 24)
    }
  }
}
