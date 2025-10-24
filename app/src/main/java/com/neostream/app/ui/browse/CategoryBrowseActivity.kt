package com.neostream.app.ui.browse

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neostream.app.databinding.ActivityCategoryBrowseBinding
import com.neostream.app.data.db.NeostreamDb
import com.neostream.app.data.db.TopGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryBrowseActivity : AppCompatActivity() {
  private lateinit var vb: ActivityCategoryBrowseBinding
  private lateinit var adapter: GroupsAdapter
  private var currentKind: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivityCategoryBrowseBinding.inflate(layoutInflater)
    setContentView(vb.root)

    currentKind = intent.getStringExtra("kind") ?: "live"
    val title = intent.getStringExtra("title") ?: "Catégorie"
    
    vb.tvTitle.text = title

    vb.btnBack.setOnClickListener { finish() }

    setupRecyclerView()
    loadGroups()
  }

  private fun setupRecyclerView() {
    adapter = GroupsAdapter { group ->
      openChannelsGrid(group)
    }
    vb.recyclerView.layoutManager = GridLayoutManager(this, 2)
    vb.recyclerView.adapter = adapter
  }

  private fun loadGroups() {
    vb.progressBar.visibility = View.VISIBLE

    lifecycleScope.launch {
      try {
        val groups = withContext(Dispatchers.IO) {
          NeostreamDb.get(this@CategoryBrowseActivity).dao().topGroups(currentKind)
        }

        if (groups.isEmpty()) {
          vb.tvEmpty.visibility = View.VISIBLE
          vb.tvEmpty.text = "Aucun contenu dans cette catégorie.\nImportez une playlist d'abord."
        } else {
          adapter.submitList(groups)
          vb.tvSubtitle.text = "${groups.size} groupes disponibles"
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

  private fun openChannelsGrid(group: TopGroup) {
    val intent = Intent().setClassName(this, "com.neostream.app.ui.browse.ChannelsGridActivity").apply {
      putExtra("kind", currentKind)
      putExtra("groupName", group.groupName)
      putExtra("title", group.groupName ?: "Chaînes")
    }
    startActivity(intent)
  }
}

class GroupsAdapter(
  private val onGroupClick: (TopGroup) -> Unit
) : RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {

  private var groups = listOf<TopGroup>()

  fun submitList(list: List<TopGroup>) {
    groups = list
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
    val view = android.view.LayoutInflater.from(parent.context)
      .inflate(android.R.layout.simple_list_item_2, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(groups[position])
  }

  override fun getItemCount() = groups.size

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val text1 = view.findViewById<android.widget.TextView>(android.R.id.text1)
    private val text2 = view.findViewById<android.widget.TextView>(android.R.id.text2)

    fun bind(group: TopGroup) {
      text1.text = group.groupName ?: "Autre"
      text2.text = "${group.cnt} chaînes"
      itemView.setOnClickListener { onGroupClick(group) }
      
      // Make it look better
      text1.textSize = 18f
      text1.setTextColor(android.graphics.Color.WHITE)
      text2.setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
      itemView.setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
      itemView.setPadding(32, 32, 32, 32)
    }
  }
}
