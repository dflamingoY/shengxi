package org.xiaoxingqi.shengxi.impl;

/**
 * 提示输入密码,执行超管操作
 */
public interface DialogAdminPwdListener {
    void onResult(String key, String value, String pwd);
}
