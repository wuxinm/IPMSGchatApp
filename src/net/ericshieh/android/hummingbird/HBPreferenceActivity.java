
package net.ericshieh.android.hummingbird;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

//继承PreferenceActivity，并实现OnPreferenceChangeListener和OnPreferenceClickListener监听接口   
public class HBPreferenceActivity extends PreferenceActivity implements OnPreferenceChangeListener,
        OnPreferenceClickListener {
    public static final String HB_TAG = BaseActivity.HB_TAG;
    
    public HBApplication app;
    
    // 定义相关变量
    public String nicknameKey;
    public String workgroupKey;
    public String historyCheckKey;
    public String messageRemindCheckKey;
    public String messageRemindPlanKey;
    public String advancedMessageCheckKey;
    public String messageOvertimeLimitKey;
    public String messageRetryTimesKey;
    public EditTextPreference nickname;
    public EditTextPreference workgroup;
    public CheckBoxPreference messageRemindCheck;
    public ListPreference messageRemindPlan;
    public CheckBoxPreference historyCheck;
    public CheckBoxPreference advancedMessageCheck;
    public ListPreference messageOvertimeLimit;
    public ListPreference messageRetryTimes;
    public static String NickNameStr;
    public static String WorkGroupStr;
    Resources res;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 从xml文件中添加Preference项
        addPreferencesFromResource(R.xml.preferences);
        
        app = (HBApplication)getApplication();
        app.setCfgChanged(false);
        
        res = getResources();

        // 获取各个Preference
        nicknameKey = res.getString(R.string.nickname_key);
        workgroupKey = res.getString(R.string.workgroup_key);
        nickname = (EditTextPreference)findPreference(nicknameKey);
        workgroup = (EditTextPreference)findPreference(workgroupKey);
        historyCheckKey = res.getString(R.string.history_check_key);
        historyCheck = (CheckBoxPreference)findPreference(historyCheckKey);

        messageRemindCheckKey = res.getString(R.string.message_remind_check_key);
        messageRemindCheck = (CheckBoxPreference)findPreference(messageRemindCheckKey);
        messageRemindPlanKey = res.getString(R.string.message_remind_plan_key);
        messageRemindPlan = (ListPreference)findPreference(messageRemindPlanKey);

        advancedMessageCheckKey = res.getString(R.string.advanced_message_check_key);
        messageOvertimeLimitKey = res.getString(R.string.message_overtime_limit_key);
        messageRetryTimesKey = res.getString(R.string.message_retry_times_key);
        advancedMessageCheck = (CheckBoxPreference)findPreference(advancedMessageCheckKey);
        messageOvertimeLimit = (ListPreference)findPreference(messageOvertimeLimitKey);
        messageRetryTimes = (ListPreference)findPreference(messageRetryTimesKey);
        // 为各个Preference注册监听接口
        // NickName.setOnPreferenceClickListener(this);
        nickname.setOnPreferenceChangeListener(this);
        // WorkGroup.setOnPreferenceClickListener(this);
        workgroup.setOnPreferenceChangeListener(this);
        advancedMessageCheck.setOnPreferenceChangeListener(this);
        advancedMessageCheck.setOnPreferenceClickListener(this);
        messageOvertimeLimit.setOnPreferenceChangeListener(this);
        // Message_overtime_limit.setOnPreferenceClickListener(this);
        messageRetryTimes.setOnPreferenceChangeListener(this);
        // Message_retry_times.setOnPreferenceClickListener(this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        NickNameStr = settings.getString(nicknameKey, "");
        WorkGroupStr = settings.getString(workgroupKey, "");
        nickname.setSummary(NickNameStr);
        workgroup.setSummary(WorkGroupStr);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        
        // 判断是哪个Preference改变了
        if (key.equals(advancedMessageCheckKey)) {
        } else if (key.equals(messageOvertimeLimitKey)) {
        } else if (key.equals(messageRetryTimesKey)) {
        } else if (key.equals(historyCheckKey)) {
        } else if (key.equals(nicknameKey)) {
            nickname.setSummary(nickname.getEditText().getText().toString());
        } else if (key.equals(workgroupKey)) {
            workgroup.setSummary(workgroup.getEditText().getText().toString());
        } else {
            // 如果返回false表示不允许被改变
            return false;
        }
        // 返回true表示允许改变
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // 判断是哪个Preference被点击了
        String key = preference.getKey();

        if (key.equals(advancedMessageCheckKey)) {
            if (advancedMessageCheck.isChecked()) {
                messageOvertimeLimit.setValue(String.valueOf(res.getInteger(R.integer.message_overtime_limit_default_value)));
                messageRetryTimes.setValue(String.valueOf(res.getInteger(R.integer.message_retry_times_default_value)));
            }
        } else if (key.equals(messageOvertimeLimitKey)) {
        } else if (key.equals(messageRetryTimesKey)) {

        } else {
            return false;
        }
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        app.addActivity();
        app.showHBNotify(this, false);

    }
    
    @Override
    protected void onStop() {
        app.removeActivity();
        app.showHBNotify(this, true);
        super.onStop();
    }
    
}
