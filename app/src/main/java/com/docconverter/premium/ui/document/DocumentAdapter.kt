package com.docconverter.premium.ui.document

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.docconverter.premium.core.models.Document
import com.docconverter.premium.databinding.ItemDocumentBinding

/**
 * Adapter for displaying documents in a RecyclerView
 */
class DocumentAdapter(
    private val onItemClick: (Document) -> Unit
) : ListAdapter<Document, DocumentAdapter.DocumentViewHolder>(DocumentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemDocumentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = getItem(position)
        holder.bind(document)
    }

    inner class DocumentViewHolder(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(document: Document) {
            binding.documentTitle.text = document.name
            binding.documentFormat.text = document.format.name

            // In a real implementation, would set the appropriate icon based on document format
            // For example:
            // when (document.format.id) {
            //     "pdf" -> binding.documentIcon.setImageResource(R.drawable.ic_pdf)
            //     "docx" -> binding.documentIcon.setImageResource(R.drawable.ic_word)
            //     ...
            // }
        }
    }
}

/**
 * DiffUtil callback for efficient RecyclerView updates
 */
class DocumentDiffCallback : DiffUtil.ItemCallback<Document>() {
    override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
        return oldItem == newItem
    }
}
