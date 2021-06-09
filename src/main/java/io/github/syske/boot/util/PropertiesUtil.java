package io.github.syske.boot.util;

import java.util.Date;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * properties工具类
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-09 7:48
 */
public class PropertiesUtil {
    private static HashMap<String, PropertiesUtil> configMap = new HashMap();
    private Date loadTime = null;
    private ResourceBundle resourceBundle = null;
    private static final Integer TIME_OUT = 60000;

    private PropertiesUtil(String name) {
        this.loadTime = new Date();

        try {
            this.resourceBundle = ResourceBundle.getBundle(name);
        } catch (Exception var3) {
            this.resourceBundle = null;
        }

    }

    public static synchronized PropertiesUtil getInstance() {
        return getInstance("application");
    }

    public static synchronized PropertiesUtil getInstance(String name) {
        PropertiesUtil conf = configMap.get(name);
        if (null == conf) {
            conf = new PropertiesUtil(name);
            configMap.put(name, conf);
        }

        if ((new Date()).getTime() - conf.getLoadTime().getTime() > (long)TIME_OUT) {
            conf = new PropertiesUtil(name);
            configMap.put(name, conf);
        }

        return conf;
    }

    public String get(String key) {
        try {
            String value = this.resourceBundle.getString(key);
            return value;
        } catch (MissingResourceException var3) {
            return "";
        } catch (NullPointerException var4) {
            return "";
        }
    }

    public Integer getInt(String key) {
        try {
            String value = this.resourceBundle.getString(key);
            return Integer.parseInt(value);
        } catch (MissingResourceException var3) {
            return null;
        } catch (NullPointerException var4) {
            return null;
        }
    }

    public boolean getBoolean(String key) {
        try {
            String value = this.resourceBundle.getString(key);
            return "true".equals(value);
        } catch (MissingResourceException var3) {
            return false;
        } catch (NullPointerException var4) {
            return false;
        }
    }

    public Date getLoadTime() {
        return this.loadTime;
    }

    public static String getPropertiesValue(String name, String key) {
        try {
            return getInstance(name).get(key);
        } catch (MissingResourceException var3) {
            return "";
        } catch (NullPointerException var4) {
            return "";
        }
    }
}
