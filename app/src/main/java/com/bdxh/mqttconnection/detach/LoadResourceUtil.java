package com.bdxh.mqttconnection.detach;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.bdxh.mqttconnection.Config;
import com.bdxh.mqttconnection.utils.SPUtil;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import dalvik.system.DexClassLoader;

public class LoadResourceUtil {

    private static final String RESOURCE_TYPE_DRAWABLE = "drawable";// 图片
    private static final String RESOURCE_TYPE_STRING = "string";// 文字
    private static final String RESOURCE_TYPE_COLOR = "color";// 颜色

    private Context mContext;
    private String mDexDir; //资源路径
    //资源对象
    private static LoadResourcesBean mLoadResources;

    //静态内部类
    private static class LoadResouerceHolder{
        private static final LoadResourceUtil INSTANCE = new LoadResourceUtil();
    }

    public static final LoadResourceUtil getInstance(){
        return LoadResouerceHolder.INSTANCE;
    }

    public void init(Context context, String resourcePath){
        LogUtils.d(" init 初始化 ");
        mContext = context.getApplicationContext();
        File dexDir = mContext.getDir("dex",Context.MODE_PRIVATE);
        if ( !dexDir.exists()) {
            dexDir.mkdir();
        }
        mDexDir = dexDir.getAbsolutePath();/* Environment.getExternalStorageDirectory()+""*/
        LogUtils.d(" 资源路径 " + mDexDir);
        ThreadUtils.getIoPool().execute(() -> mLoadResources = getResouerceLoad(resourcePath));
    }

    //加载未安装资源包
    private LoadResourcesBean getResouerceLoad(String resourcePath) {
        LoadResourcesBean loadResources = null;
        //获取未安装的apk的PackageInfo
        PackageInfo info =  queryPackageInfo(resourcePath);
        if (info != null) {
            try {
                AssetManager manager = AssetManager.class.newInstance();
                Class clazz = AssetManager.class;
                Method method = clazz.getMethod("addAssetPath",String.class);
                //反射设置资源加载
                method.invoke(manager,resourcePath);
                Resources resources = new Resources(manager,mContext.getResources().getDisplayMetrics(),mContext.getResources().getConfiguration());
                loadResources = new LoadResourcesBean();
                loadResources.setResources(resources);
                loadResources.setPackageName(info.packageName);
                //设置类加载器
                loadResources.setClassLoader(new DexClassLoader(resourcePath,mDexDir,null ,mContext.getClassLoader()));
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                LogUtils.d(" 加载未安装资源异常: " + e.getMessage());
            }
        }
        return  loadResources;
    }

    //重新加载未安装资源apk
    public void setLoadResource(String resourcePath){
        //保存资源路径 下次启动直接加载
        SPUtil.put(Config.SP_RESOURCE_PATH, resourcePath);
//        SPUtils.getInstance(Config.SP_NAME,Context.MODE_PRIVATE).put(Config.SP_RESOURCE_PATH,resourcePath);
        mLoadResources = getResouerceLoad(resourcePath);
    }

    //未安装资源apk信息
    private PackageInfo queryPackageInfo(String resourcePath) {
        return mContext.getPackageManager().getPackageArchiveInfo(resourcePath, PackageManager.GET_ACTIVITIES);
    }

    //资源Drawable
    public Drawable getDrawable(String fileName){
        Drawable drawable = null;
        int resourceId = getResouercId(mLoadResources.getPackageName(),RESOURCE_TYPE_DRAWABLE,fileName);
        if (mLoadResources != null) {
            drawable = mLoadResources.getResources().getDrawable(resourceId);
        }
        return  drawable;
    }

    //资源Drawable
    public int getColor(String fileName){
        int color = 0;
        int resourceId = getResouercId(mLoadResources.getPackageName(),RESOURCE_TYPE_COLOR,fileName);
        if (mLoadResources != null) {
            color = mLoadResources.getResources().getColor(resourceId);
        }
        return color;
    }

    //资源Drawable
    public String getString(String fileName){
        String str = null;
        int resourceId = getResouercId(mLoadResources.getPackageName(),RESOURCE_TYPE_STRING,fileName);
        if (mLoadResources != null) {
            str = mLoadResources.getResources().getString(resourceId);
        }
        return  str;
    }

    public int getResouercId(String packageName, String type, String fieldName){
        int resId = 0;
        String clazzName = packageName + ".R$" + type;
        try {
            Class clazz = mLoadResources.getClassLoader().loadClass(clazzName);
            resId = (Integer) clazz.getField(fieldName).get(null);
        } catch (Exception  e) {
            e.printStackTrace();
        }
        return  resId;
    }

}
