package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.layout_admin_img_group.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity

class AdminImageGroup(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {
    private var list: ArrayList<String>? = null
    override fun getLayoutId(): Int {
        return R.layout.layout_admin_img_group
    }

    init {
        square_img_1.setOnClickListener {
            if (list != null)
                context.startActivity(Intent(context, ShowPicActivity::class.java).putExtra("data", list).putExtra("index", 0))
        }
        square_img_2.setOnClickListener {
            if (list != null)
                context.startActivity(Intent(context, ShowPicActivity::class.java).putExtra("data", list).putExtra("index", 1))
        }
        square_img_3.setOnClickListener {
            if (list != null)
                context.startActivity(Intent(context, ShowPicActivity::class.java).putExtra("data", list).putExtra("index", 2))
        }
    }

    fun setData(data: ArrayList<String>?) {
        list = data
        visibility = if (null == data || data.size == 0) {
            View.GONE
        } else {
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.drawable_default_tmpry).error(R.drawable.drawable_default_tmpry).signature(ObjectKey(data[0])))
                    .asBitmap()
                    .load(data[0])
                    .error(Glide.with(context)
                            .applyDefaultRequestOptions(RequestOptions()
                                    .signature(ObjectKey(if (data[0].contains("?")) data[0].substring(0, data[0].indexOf("?")) else data[0])))
                            .asBitmap()
                            .load(if (data[0].contains("?")) data[0].substring(0, data[0].indexOf("?")) else data[0]))
                    .into(square_img_1)
            when (data.size) {
                1 -> {
                    square_img_2.visibility = View.GONE
                    square_img_3.visibility = View.GONE
                }
                2 -> {
                    square_img_2.visibility = View.VISIBLE
                    square_img_3.visibility = View.GONE
                    Glide.with(this)
                            .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.drawable_default_tmpry).error(R.drawable.drawable_default_tmpry).signature(ObjectKey(data[1])))
                            .asBitmap()
                            .load(data[1])
                            .error(Glide.with(context)
                                    .applyDefaultRequestOptions(RequestOptions()
                                            .signature(ObjectKey(if (data[1].contains("?")) data[1].substring(0, data[1].indexOf("?")) else data[1])))
                                    .asBitmap()
                                    .load(if (data[1].contains("?")) data[1].substring(0, data[1].indexOf("?")) else data[1]))
                            .into(square_img_2)
                }
                else -> {
                    square_img_2.visibility = View.VISIBLE
                    square_img_3.visibility = View.VISIBLE
                    Glide.with(this)
                            .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.drawable_default_tmpry).error(R.drawable.drawable_default_tmpry).signature(ObjectKey(data[1])))
                            .asBitmap()
                            .load(data[1])
                            .error(Glide.with(context)
                                    .applyDefaultRequestOptions(RequestOptions()
                                            .signature(ObjectKey(if (data[1].contains("?")) data[1].substring(0, data[1].indexOf("?")) else data[1])))
                                    .asBitmap()
                                    .load(if (data[1].contains("?")) data[1].substring(0, data[1].indexOf("?")) else data[1]))
                            .into(square_img_2)
                    Glide.with(this)
                            .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.drawable_default_tmpry).error(R.drawable.drawable_default_tmpry).signature(ObjectKey(data[2])))
                            .asBitmap()
                            .load(data[2])
                            .error(Glide.with(context)
                                    .applyDefaultRequestOptions(RequestOptions()
                                            .signature(ObjectKey(if (data[2].contains("?")) data[2].substring(0, data[2].indexOf("?")) else data[2])))
                                    .asBitmap()
                                    .load(if (data[2].contains("?")) data[2].substring(0, data[2].indexOf("?")) else data[2]))
                            .into(square_img_3)
                }
            }
            View.VISIBLE
        }
    }
}