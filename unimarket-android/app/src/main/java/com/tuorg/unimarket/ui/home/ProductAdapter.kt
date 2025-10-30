package com.tuorg.unimarket.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tuorg.unimarket.R
import java.text.NumberFormat
import java.util.*

class ProductAdapter(
    private var items: List<Product>,
    private val onFavClick: (Product) -> Unit,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    private val favorites = mutableSetOf<String>()

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val imgProduct: ImageView = v.findViewById(R.id.imgProduct)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvPrice: TextView = v.findViewById(R.id.tvPrice)
        val tvDescription: TextView = v.findViewById(R.id.tvDescription)
        val btnFav: ImageView = v.findViewById(R.id.btnFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = items[position]

        // Título
        h.tvTitle.text = p.title

        // Precio formateado
        val priceFormatted = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            .format(p.price)
        h.tvPrice.text = priceFormatted

        // Descripción
        h.tvDescription.text = p.description

        // Imagen (primera si existe)
        if (p.images.isNotEmpty()) {
            Glide.with(h.itemView.context)
                .load(p.images[0].url)
                .centerCrop()
                .placeholder(R.color.input_bg)
                .error(R.color.input_bg)
                .into(h.imgProduct)
        } else {
            h.imgProduct.setImageResource(R.color.input_bg)
        }

        // Favorito
        val isFav = favorites.contains(p._id)
        h.btnFav.setImageResource(
            if (isFav) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )

        // Clicks
        h.itemView.setOnClickListener { onItemClick(p) }
        h.btnFav.setOnClickListener {
            if (isFav) favorites.remove(p._id)
            else favorites.add(p._id)
            notifyItemChanged(position)
            onFavClick(p)
        }
    }

    override fun getItemCount() = items.size

    fun submit(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }
}