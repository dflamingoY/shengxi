package org.xiaoxingqi.shengxi.modules


//世界界面可能继续使用的代码
/*
* if (!TextUtils.isEmpty(item.first_share_voice) && item.first_share_voice == "1" && TextUtils.isEmpty(item.is_shared)) {
                    helper.getView(R.id.tv_first_share_world).visibility = View.VISIBLE
                    helper.getView(R.id.tv_Interested).visibility = View.GONE
                } else if (item.may_interested == 1) {
                    helper.getView(R.id.tv_Interested).visibility = View.VISIBLE
                    helper.getView(R.id.tv_first_share_world).visibility = View.GONE
                    when {
                        item.user_gender == 1 -> helper.getView(R.id.tv_Interested).isSelected = true
                        item.user_gender == 2 -> helper.getView(R.id.tv_Interested).isSelected = false
                        else -> {
                            helper.getView(R.id.tv_Interested).visibility = View.GONE
                        }
                    }
                } else {
                    helper.getView(R.id.tv_Interested).visibility = View.GONE
                    helper.getView(R.id.tv_first_share_world).visibility = View.GONE
                }
* */

/*
 * 获取窗体的bitmap
 */
//private fun onCut(): Bitmap {
//    //获取window最底层的view
//    val view = window.decorView
//    view.buildDrawingCache()
//    //状态栏高度
//    val rect = Rect()
//    view.getWindowVisibleDisplayFrame(rect)
//    val stateBarHeight = rect.top
//    val display = windowManager.defaultDisplay
//    //获取屏幕宽高
//    val widths = display.width
//    val height = display.height
//    //设置允许当前窗口保存缓存信息
//    view.isDrawingCacheEnabled = true
//    //去掉状态栏高度
//    val bitmap = Bitmap.createBitmap(view.drawingCache, 0, stateBarHeight, widths, height - stateBarHeight)
//    view.destroyDrawingCache()
//    return bitmap
//}