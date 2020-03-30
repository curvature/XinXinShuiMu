package net.newsmth.dirac.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.newsmth.dirac.R
import net.newsmth.dirac.activity.ThreadActivity

class PageNavAdapter(val a: ThreadActivity) : RecyclerView.Adapter<PageNavAdapter.ViewHolder>() {
    var totalPage: Int = 0
    var curPage: Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.page_row, parent, false)
        return ViewHolder(v, a)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = (position + 1).toString()
        holder.textView.isActivated = position + 1 == curPage
        holder.textView.tag = position + 1
    }

    override fun getItemCount(): Int {
        return totalPage
    }

    class ViewHolder(v: View, val act: ThreadActivity) : RecyclerView.ViewHolder(v), View.OnClickListener {
        override fun onClick(v: View) {
            act.jumpTo(v.tag as Int)
        }

        val textView = v as TextView

        init {
            textView.setOnClickListener(this)
        }
    }

    fun update(total: Int, cur: Int) {
        totalPage = total
        curPage = cur
        notifyDataSetChanged()
    }
}
