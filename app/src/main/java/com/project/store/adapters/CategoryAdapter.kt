package com.project.store.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.store.databinding.ItemCategoryBinding
import com.project.store.models.Category
import com.project.store.utils.MockRepository

class CategoryAdapter(
    private var categories: List<CategoryItem> = CategoryItem.fromMockCategories(),
    private val onCategoryClick: (CategoryItem) -> Unit = {}
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedCategoryId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding, onCategoryClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], selectedCategoryId)
    }

    override fun getItemCount(): Int = categories.size

    fun submitList(newCategories: List<CategoryItem>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    fun setSelectedCategory(categoryId: Int?) {
        selectedCategoryId = categoryId
        notifyDataSetChanged()
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val onCategoryClick: (CategoryItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryItem, selectedCategoryId: Int?) {
            binding.root.text = category.name
            binding.root.isChecked = category.id == selectedCategoryId
            binding.root.setOnClickListener { onCategoryClick(category) }
        }
    }

    data class CategoryItem(
        val id: Int?,
        val name: String
    ) {
        companion object {
            fun fromMockCategories(categories: List<Category> = MockRepository.categories): List<CategoryItem> {
                return categories.map { CategoryItem(id = it.id, name = it.name) }
            }
        }
    }
}
