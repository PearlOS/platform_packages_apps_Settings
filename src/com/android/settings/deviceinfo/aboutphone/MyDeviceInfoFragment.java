/*
 * Copyright (C) 2018 The Android Open Source Project
 * Copyright (C) 2019 SereinOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.aboutphone;

import static com.android.settings.bluetooth.Utils.getLocalBtManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.os.UserHandle;
import android.os.Handler;
import android.os.UserManager;
import android.os.SystemProperties;
import android.os.SELinux;
import android.os.Bundle;
import android.os.StatFs;
import android.os.Environment;
import java.io.File;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.content.pm.ResolveInfo;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.util.Pair;
import android.util.Log;

import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionDialogFragment;
import com.android.settings.deviceinfo.devicestatus.DeviceStatusFragment;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.internal.util.MemInfoReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyDeviceInfoFragment extends Fragment
             implements View.OnClickListener{

    static String aprox;
    private String device;
    private final Activity mActivity = getActivity();
    

    private Toast mDevHitToast;
    
    String[] keks;
    private final int TAPS_FOR_EASTER = 11;
    private Toast mTapToast;
    private int mEasterCountdown = TAPS_FOR_EASTER;

    public Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
         mContext = getActivity().getApplicationContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        
        final MediaPlayer mp = MediaPlayer.create(mContext, R.raw.oof);
        
         // Create an ArrayAdapter that will contain all list items
         ArrayAdapter<String> adapter;
         keks = mContext.getResources().getStringArray(R.array.kek_tap);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.aboutphone, container, false);
        FrameLayout cardview1 = null;
        FrameLayout cardview2 = null;
        FrameLayout cardview3 = null;
        FrameLayout cardview4 = null;
        FrameLayout cardview5 = null;
        FrameLayout cardview6 = null;

        //Update descriptions
        TextView cpu = (TextView)view.findViewById(R.id.textView72);
        setInfo("ro.processor.model", cpu);

        TextView memory = (TextView)view.findViewById(R.id.textView82);
        memory.setText(Storage());

        TextView devicename = (TextView)view.findViewById(R.id.textView12);
        setInfo("ro.product.model", devicename);

        TextView version = (TextView)view.findViewById(R.id.textView32);
        setInfo("ro.build.version.release", version);

        TextView romver = (TextView)view.findViewById(R.id.textView42);
        setInfo("ro.pearl.display.version", romver);

        TextView selinux = (TextView)view.findViewById(R.id.textView52);
        setSelinux(selinux);

        TextView maintainer = (TextView)view.findViewById(R.id.textView62);
        setInfo("ro.pearl.maintainer", maintainer);
        
        ImageView phone = (ImageView)view.findViewById(R.id.phoneimg);
        device = SystemProperties.get("ro.product.device");
        switch (device) {
            
            case "mido":
                phone.setImageResource(R.drawable.ic_phone_mido);
                break;
                
            case "vince":
                phone.setImageResource(R.drawable.ic_phone_vince);
                break;

            case "tissot":
                phone.setImageResource(R.drawable.ic_phone_tissot);
                break;    

            case "kenzo":
                phone.setImageResource(R.drawable.ic_phone_kenzo);
                break;

            case "A0001":
                phone.setImageResource(R.drawable.ic_phone_bacon);
                break;
                
            case "X00T":
                phone.setImageResource(R.drawable.ic_phone_x00t);
                break;

            default:
                break;            
        }        

        //Add click listeners
        if (view != null) {
            cardview2 = view.findViewById(R.id.cardview2);
            cardview3 = view.findViewById(R.id.cardview3);
            cardview4 = view.findViewById(R.id.cardview4);
        }
        if (cardview3 != null && cardview4 != null) {
            cardview2.setOnClickListener(this);
            cardview3.setOnClickListener(this);
            cardview4.setOnClickListener(this);
        }
        if (phone != null) {
            phone.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                     mp.start();
                     return true;
                }
            });
        }

        return view;
    }

    //Launch new fragments on Status and android version
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            
        case R.id.cardview4:
            kektaps();
            break;

	    case R.id.cardview2:
            final DeviceStatusFragment fragment = new DeviceStatusFragment(this);
            replaceFragment(this, fragment);
            break;

        case R.id.cardview3:
            final FirmwareVersionDialogFragment dialog = new FirmwareVersionDialogFragment();
            showDialog(this, dialog);
            break;

        default:
            break;
        }
    }

    public static void showDialog(Fragment context, DialogFragment dialog) {
        FragmentTransaction ft = context.getChildFragmentManager().beginTransaction();
        Fragment prev = context.getChildFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            dialog.show(ft, "dialog");
    }

    public static void replaceFragment(Fragment context, DashboardFragment fragment) {
        FragmentTransaction ft = context.getChildFragmentManager().beginTransaction();
        ft.add(R.id.deviceinfo, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }
    
    public void kektaps() {
        if (mEasterCountdown -1 > 0) {
             if (mTapToast != null) {
                 mTapToast.cancel();
                 mTapToast = null;
             }
             toasts();
             mEasterCountdown--;
             if (mEasterCountdown -1 == 0) {
                 mEasterCountdown++;
                 showeasteregg();
             }
        }
    }
    
    public void toasts() {
        mTapToast= Toast.makeText(mContext, keks[11 - mEasterCountdown],
               mTapToast.LENGTH_SHORT);
        mTapToast.show();
    }
    
    public void showeasteregg() {
         if (mTapToast != null) {
              mTapToast.cancel();
         }
         mTapToast = mTapToast.makeText(mContext, "Long live the goddess!",
               mTapToast.LENGTH_LONG);
         mTapToast.show();
         View view = null;
         final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         LayoutInflater inflater = (LayoutInflater) mContext
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         view = inflater.inflate(R.layout.dialog_aqua, null);
         builder.setView(view);
         builder.setCancelable(true);
         AlertDialog alertdialog = builder.create();
         alertdialog.show();
     }

    //Set summaries
    public static void setInfo(String prop, TextView textview) {
        if (TextUtils.isEmpty(SystemProperties.get(prop))) {
            textview.setText("Unknown");
        } else if (prop == "ro.processor.model" || prop == "ro.pearl.maintainer") {
                String model = SystemProperties.get(prop);
                model = model.replace("_", " ");
                textview.setText(model);
            } else {    
                textview.setText(SystemProperties.get(prop));
            }
    }

    //Get SELinux status
    public void setSelinux(TextView textview) {
        if (!SELinux.isSELinuxEnabled()) {
            String status = mContext.getResources().getString(R.string.selinux_status_disabled);
            textview.setText(status);
        } else if (!SELinux.isSELinuxEnforced()) {
            String status = mContext.getResources().getString(R.string.selinux_status_permissive);
            textview.setText(status);
        } else {
            String status = mContext.getResources().getString(R.string.selinux_status_enforcing);
            textview.setText(status);
        }
    }

    //Get total mem
    public static int getTotalRAM() {
            MemInfoReader memReader = new MemInfoReader();
            memReader.readMemInfo();
            String aprox;
            double totalmem = memReader.getTotalSize();
            double gb = (totalmem / 1073741824) + 0.1f; //Cause 4gig devices show memory as 3.48 .-.
            int gigs = (int) Math.round(gb);
            return gigs;
    }

    //Get internal storage
    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        double total = (totalBlocks * blockSize)/ 1073741824;
        int lastval = (int) Math.round(total);
            if ( lastval > 0  && lastval <= 16){
                aprox = "16";
            } else if (lastval > 16 && lastval <=32) {
                aprox = "32";
            } else if (lastval > 32 && lastval <=64) {
                aprox = "64";
            } else if (lastval > 64 && lastval <=128) {
                aprox = "128";
            } else if (lastval > 128) {
                aprox = "128+";
            } else aprox = "null";
        return aprox;
    }

    //Get a string for representation of memory
    public String Storage () {
        String memory = Integer.toString(getTotalRAM()) + "GB / " + getTotalInternalMemorySize() + "GB";
        return memory;
    }


}
