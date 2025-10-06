package com.dddpeter.app.rainweather.ui.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dddpeter.app.rainweather.data.models.CityModel
import com.dddpeter.app.rainweather.data.models.CityWithWeather
import com.dddpeter.app.rainweather.databinding.ItemMainCityBinding
import java.util.Collections

/**
 * 主要城市适配器（支持拖拽排序）
 */
class MainCityAdapter(
    private val onCityClick: (CityModel) -> Unit,
    private val onDeleteClick: (CityModel) -> Unit,
    private val onOrderChanged: (List<CityWithWeather>) -> Unit
) : ListAdapter<CityWithWeather, MainCityAdapter.ViewHolder>(CityDiffCallback()) {
    
    private var itemTouchHelper: ItemTouchHelper? = null
    private val cities = mutableListOf<CityWithWeather>()
    
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        val callback = ItemTouchHelperCallback()
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }
    
    override fun submitList(list: List<CityWithWeather>?) {
        cities.clear()
        list?.let { cities.addAll(it) }
        super.submitList(list?.let { ArrayList(it) })
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMainCityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: ItemMainCityBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            // 点击卡片
            binding.cardCity.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCityClick(getItem(position).city)
                }
            }
            
            // 点击删除按钮
            binding.ivDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position).city)
                }
            }
            
            // 长按拖拽手柄开始拖拽
            binding.ivDragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper?.startDrag(this)
                }
                false
            }
        }
        
        fun bind(cityWithWeather: CityWithWeather) {
            val city = cityWithWeather.city
            
            // 城市名称
            binding.tvCityName.text = city.name
            
            // 显示/隐藏当前位置标签
            if (city.isCurrentLocation) {
                binding.tvCurrentLocationTag.visibility = View.VISIBLE
            } else {
                binding.tvCurrentLocationTag.visibility = View.GONE
            }
            
            // 天气信息
            binding.tvWeatherIcon.text = cityWithWeather.getWeatherIconText()
            binding.tvTemperature.text = cityWithWeather.getTemperatureText()
            binding.tvWeatherDesc.text = cityWithWeather.getWeatherText()
        }
    }
    
    /**
     * ItemTouchHelper回调
     */
    inner class ItemTouchHelperCallback : ItemTouchHelper.Callback() {
        
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragFlags, 0)
        }
        
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            
            // 交换列表中的项
            Collections.swap(cities, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            
            return true
        }
        
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // 不支持滑动删除
        }
        
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            
            // 拖拽结束，通知顺序变化
            onOrderChanged(cities.toList())
        }
        
        override fun isLongPressDragEnabled(): Boolean = false // 使用手柄拖拽
        override fun isItemViewSwipeEnabled(): Boolean = false // 禁用滑动
    }
    
    private class CityDiffCallback : DiffUtil.ItemCallback<CityWithWeather>() {
        override fun areItemsTheSame(oldItem: CityWithWeather, newItem: CityWithWeather): Boolean {
            return oldItem.city.id == newItem.city.id
        }
        
        override fun areContentsTheSame(oldItem: CityWithWeather, newItem: CityWithWeather): Boolean {
            return oldItem == newItem
        }
    }
}

