package com.dddpeter.app.rainweather.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dddpeter.app.rainweather.data.local.database.AppDatabase
import com.dddpeter.app.rainweather.data.models.CityInfo
import com.dddpeter.app.rainweather.data.models.CityModel
import com.dddpeter.app.rainweather.databinding.DialogAddCityBinding
import com.dddpeter.app.rainweather.databinding.FragmentMainCitiesBinding
import com.dddpeter.app.rainweather.ui.CityWeatherActivity
import com.dddpeter.app.rainweather.ui.MainActivity
import com.dddpeter.app.rainweather.ui.adapters.CitySearchAdapter
import com.dddpeter.app.rainweather.ui.adapters.MainCityAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 主要城市Fragment（对应Flutter版本的MainCitiesScreen）
 */
class MainCitiesFragment : Fragment() {
    
    private var _binding: FragmentMainCitiesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var database: AppDatabase
    private lateinit var cityAdapter: MainCityAdapter
    private var currentCities = listOf<CityModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainCitiesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("📱 MainCitiesFragment: onViewCreated")
        
        database = (requireActivity() as MainActivity).getApp().database
        
        setupViews()
        loadCities()
    }
    
    private fun setupViews() {
        // 设置城市列表
        cityAdapter = MainCityAdapter(
            onCityClick = { city ->
                // 点击城市项，跳转到该城市的天气详情页面
                CityWeatherActivity.start(requireContext(), city)
            },
            onDeleteClick = { city ->
                showDeleteConfirmDialog(city)
            },
            onOrderChanged = { newOrder ->
                updateCityOrder(newOrder)
            }
        )
        
        binding.rvCities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cityAdapter
            cityAdapter.attachToRecyclerView(this)
        }
    }
    
    /**
     * 添加城市（从菜单调用）
     */
    fun onAddCityClick() {
        showAddCityDialog()
    }
    
    private fun loadCities() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val cities = database.mainCityDao().getAllMainCities()
                currentCities = cities
                cityAdapter.submitList(cities)
                
                // 更新UI状态
                updateUIState(cities)
                
                Timber.d("✅ 加载了 ${cities.size} 个城市")
            } catch (e: Exception) {
                Timber.e(e, "❌ 加载城市失败")
                Toast.makeText(requireContext(), "加载城市失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateUIState(cities: List<CityModel>) {
        if (cities.isEmpty()) {
            binding.rvCities.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.tvCityCount.text = "0个城市"
        } else {
            binding.rvCities.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            binding.tvCityCount.text = "${cities.size}个城市"
        }
    }
    
    private fun showAddCityDialog() {
        val dialogBinding = DialogAddCityBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        // 设置搜索适配器
        val searchAdapter = CitySearchAdapter { cityInfo ->
            addCity(cityInfo)
            dialog.dismiss()
        }
        
        dialogBinding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
        
        // 搜索功能
        dialogBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                searchCities(query, searchAdapter, dialogBinding)
            }
        })
        
        // 取消按钮
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun searchCities(query: String, adapter: CitySearchAdapter, dialogBinding: DialogAddCityBinding) {
        if (query.isEmpty()) {
            adapter.submitList(emptyList())
            dialogBinding.rvSearchResults.visibility = View.GONE
            dialogBinding.layoutEmpty.visibility = View.VISIBLE
            dialogBinding.tvEmptyMessage.text = "输入城市名称搜索"
            return
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results = database.cityDao().searchCitiesByName("%$query%")
                if (results.isEmpty()) {
                    adapter.submitList(emptyList())
                    dialogBinding.rvSearchResults.visibility = View.GONE
                    dialogBinding.layoutEmpty.visibility = View.VISIBLE
                    dialogBinding.tvEmptyMessage.text = "未找到匹配的城市"
                } else {
                    adapter.submitList(results)
                    dialogBinding.rvSearchResults.visibility = View.VISIBLE
                    dialogBinding.layoutEmpty.visibility = View.GONE
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ 搜索城市失败")
            }
        }
    }
    
    private fun addCity(cityInfo: CityInfo) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 检查是否已存在
                val existingCity = database.mainCityDao().getMainCityById(cityInfo.id)
                if (existingCity != null) {
                    Toast.makeText(requireContext(), "${cityInfo.name}已在列表中", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // 获取当前最大排序值
                val maxOrder = database.mainCityDao().getMaxSortOrder() ?: -1
                
                // 创建新城市
                val newCity = cityInfo.toCityModel(sortOrder = maxOrder + 1)
                database.mainCityDao().insertMainCity(newCity)
                
                Toast.makeText(requireContext(), "已添加${cityInfo.name}", Toast.LENGTH_SHORT).show()
                
                // 重新加载列表
                loadCities()
                
                Timber.d("✅ 添加城市: ${cityInfo.name}")
            } catch (e: Exception) {
                Timber.e(e, "❌ 添加城市失败")
                Toast.makeText(requireContext(), "添加城市失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteConfirmDialog(city: CityModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除城市")
            .setMessage("确定要删除${city.name}吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteCity(city)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deleteCity(city: CityModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                database.mainCityDao().deleteMainCity(city.id)
                
                Toast.makeText(requireContext(), "已删除${city.name}", Toast.LENGTH_SHORT).show()
                
                // 重新加载列表
                loadCities()
                
                Timber.d("✅ 删除城市: ${city.name}")
            } catch (e: Exception) {
                Timber.e(e, "❌ 删除城市失败")
                Toast.makeText(requireContext(), "删除城市失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateCityOrder(newOrder: List<CityModel>) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 更新每个城市的sortOrder
                newOrder.forEachIndexed { index, city ->
                    database.mainCityDao().updateSortOrder(city.id, index)
                }
                
                Timber.d("✅ 更新城市排序")
            } catch (e: Exception) {
                Timber.e(e, "❌ 更新城市排序失败")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

