package info.celsul.car.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import info.celsul.car.R
import info.celsul.car.model.CarItem
import info.celsul.car.ui.loadUrl

class CarItemAdapter(
    private val items: List<CarItem>,
    private val itemClickListener: (CarItem) -> Unit,
) : RecyclerView.Adapter<CarItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val fullNameTextView: TextView = view.findViewById(R.id.name)
        val ageTextView: TextView = view.findViewById(R.id.manufacturingYear)
        val professionTextView: TextView = view.findViewById(R.id.licensePlate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.setOnClickListener {
            itemClickListener.invoke(item)
        }
        holder.fullNameTextView.text = "${item.name}"

        holder.ageTextView.text = holder.itemView.context.getString(R.string.item_year, item.year)

        holder.professionTextView.text = item.licence

        holder.imageView.loadUrl(item.imageUrl)
    }
}