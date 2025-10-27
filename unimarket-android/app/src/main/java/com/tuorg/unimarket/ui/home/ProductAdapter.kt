package com.tuorg.unimarket.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tuorg.unimarket.R

class ProductAdapter(
    private var items: List<Product>,
    private val onFavClick: (Product) -> Unit = {},
    private val onItemClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgProduct)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val price: TextView = v.findViewById(R.id.tvPrice)
        val desc: TextView = v.findViewById(R.id.tvDescription)
        val fav: ImageView = v.findViewById(R.id.btnFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val p = items[pos]
        h.title.text = p.title
        h.price.text = "$${"%,.0f".format(p.price)}"
        h.desc.text = p.description ?: ""

        h.itemView.setOnClickListener { onItemClick(p) }
        h.fav.setOnClickListener { onFavClick(p) }

        val url = p.images.firstOrNull()
        if (url != null) Glide.with(h.itemView).load(url).into(h.img)
        else h.img.setImageResource(android.R.color.transparent)
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
