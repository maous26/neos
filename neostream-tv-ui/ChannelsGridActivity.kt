package com.neostream.app.ui.browse

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neostream.app.databinding.ActivityChannelsGridBinding
import com.neostream.app.data.db.ChannelEntity
import com.neostream.app.data.db.NeostreamDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelsGridActivity : AppCompatActivity() {
  private lateinit var vb: ActivityChannelsGridBinding
  private lateinit var adapter: ChannelsGridAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityChannelsGridBinding.inflate(layoutInflater)
    setContentView(vb.root)

    val title = intent.getStringExtra("title") ?: "Chaînes"
    vb.tvTitle.text = title

    vb.btnBack.setOnClickListener { finish() }

    setupRecyclerView()
    loadChannels()
  }

  private fun setupRecyclerView() {
    adapter = ChannelsGridAdapter { channel ->
      playChannel(channel)
    }
    vb.recyclerView.layoutManager = GridLayoutManager(this, 3)
    vb.recyclerView.adapter = adapter
  }

  private fun loadChannels() {
    vb.progressBar.visibility = View.VISIBLE

    val kind = intent.getStringExtra("kind") ?: "live"
    val groupName = intent.getStringExtra("groupName")
    val quality = intent.getStringExtra("quality")
    val country = intent.getStringExtra("country")

    lifecycleScope.launch {
      try {
        val channels = withContext(Dispatchers.IO) {
          val pagingSource = NeostreamDb.get(this@ChannelsGridActivity).dao()
            .page(kind, groupName, quality, country)
          
          val result = pagingSource.load(
            androidx.paging.PagingSource.LoadParams.Refresh(0, 100, false)
          )
          
          when (result) {
            is androidx.paging.PagingSource.LoadResult.Page -> result.data
            else -> emptyList()
          }
        }

        if (channels.isEmpty()) {
          vb.tvEmpty.visibility = View.VISIBLE
          vb.tvEmpty.text = "Aucune chaîne trouvée"
        } else {
          adapter.submitList(channels)
          vb.tvSubtitle.text = "${channels.size} chaînes"
          vb.tvSubtitle.visibility = View.VISIBLE
        }
      } catch (e: Exception) {
        vb.tvEmpty.visibility = View.VISIBLE
        vb.tvEmpty.text = "Erreur: ${e.message}"
      } finally {
        vb.progressBar.visibility = View.GONE
      }
    }
  }

  private fun playChannel(channel: ChannelEntity) {
    val intent = Intent().setClassName(this, "com.neostream.app.ui.PlayerActivity").apply {
      putExtra("url", channel.url)
      putExtra("referer", "")
      putExtra("cookie", "")
    }
    startActivity(intent)
  }
}

class ChannelsGridAdapter(
  private val onChannelClick: (ChannelEntity) -> Unit
) : RecyclerView.Adapter<ChannelsGridAdapter.ViewHolder>() {

  private var channels = listOf<ChannelEntity>()

  fun submitList(list: List<ChannelEntity>) {
    channels = list
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
    val view = android.view.LayoutInflater.from(parent.context)
      .inflate(android.R.layout.simple_list_item_2, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(channels[position])
  }

  override fun getItemCount() = channels.size

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val text1 = view.findViewById<android.widget.TextView>(android.R.id.text1)
    private val text2 = view.findViewById<android.widget.TextView>(android.R.id.text2)

    fun bind(channel: ChannelEntity) {
      text1.text = channel.title
      
      val badges = buildList {
        channel.quality?.let { add(it.uppercase()) }
        channel.countryTag?.let { add(it) }
        if (channel.isNew) add("NEW")
      }
      text2.text = badges.joinToString(" • ")
      
      itemView.setOnClickListener { onChannelClick(channel) }
      
      // Styling
      text1.textSize = 16f
      text1.setTextColor(android.graphics.Color.WHITE)
      text1.maxLines = 2
      text2.textSize = 12f
      text2.setTextColor(android.graphics.Color.parseColor("#1976D2"))
      itemView.setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
      itemView.setPadding(24, 24, 24, 24)
      
      // Highlight new channels
      if (channel.isNew) {
        itemView.setBackgroundColor(android.graphics.Color.parseColor("#263238"))
      }
    }
  }
}
