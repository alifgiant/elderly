package com.buahbatu.elderlywatch.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.buahbatu.elderlywatch.AppSetting;
import com.buahbatu.elderlywatch.R;
import com.buahbatu.elderlywatch.SocketController;
import com.buahbatu.elderlywatch.loader.ProfileLoader;
import com.buahbatu.elderlywatch.model.User;
import com.robinhood.spark.SparkView;

import java.util.ArrayList;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.BindView;


public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ProfileViewHolder>{
    public static final String SELF = "self";

    private Context context;
    private OnItemListener listener;
    private List<User> userList;
    private List<SocketController> socketControllers;

    // load user profile data
    private ProfileLoader profileLoader;

    private boolean ringtoneIsIdle = true;

    public ProfileRecyclerAdapter(Context context, OnItemListener itemClickedListener) {
        this.context = context;
        this.listener = itemClickedListener;
        this.userList = new ArrayList<>();
        this.socketControllers= new ArrayList<>();

        profileLoader = new ProfileLoader(context, new ProfileLoader.OnProfileReadyListener() {
            @Override
            public void onProfileDataReady(User user) {
                userList.add(user);
                notifyItemInserted(userList.size()-1);
            }
        });
    }

    public void addUser(String user){
        loadUserData(user);
    }

//    public void updateStatus(int position, String status){
//        userList.get(position).Status = status;
//        notifyItemChanged(position);
//    }
//
//    public void patientViewEnabled(){
//
//    }

    public void disconnectAllConnector(){
        for(SocketController s : socketControllers){
            s.disconnect();
        }
        socketControllers.clear();
    }

    private void soundOnDrop(){
        if (ringtoneIsIdle){
//            ringtoneIsIdle = false;
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone r = RingtoneManager.getRingtone(context, notification);
//            r.play();
//
//            MediaPlayer mp = MediaPlayer.create(context, notification);
//            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    ringtoneIsIdle = true;
//                }
//            });
//            mp.start();
        }
    }

    private void loadUserData(String username){
        if (TextUtils.equals(username, SELF))
            username = AppSetting.getUsername(context);

        profileLoader.addLoadQueue(username);
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.text_status) TextView mStatusText;
        @BindView(R.id.text_full_name) TextView mFullNameText;
        @BindView(R.id.text_address) TextView mAddressText;
        @BindView(R.id.text_phone_number) TextView mPhoneNumberText;
        @BindView(R.id.text_birth_date) TextView mBirthDateText;
        @BindView(R.id.spark_graph) SparkView mSparkView;

        SocketController controller;

        ProfileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        String role = userList.get(position).Role;
        switch (role){
            case "3": return 1;
            case "4": return 2;
            default: return 2;
        }
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout;
        Log.i("CREATE", "onCreateViewHolder: " + viewType);
        if (viewType == 1) {
            layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_doctor, parent, false);
        } else{
            layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        }
        return new ProfileViewHolder(layout);
    }

    private void setStatusText(String status, TextView textView){
        // setup status
        switch (status){
            case "idle": textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorIdle));
                break;
            case "sit": textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSit));
                break;
            case "walk": textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWalk));
                break;
            case "fall": textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDrop));
                soundOnDrop();
                break;
            case "drop": textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDrop));
                soundOnDrop();
                break;
        }
        textView.setText(status.toUpperCase());
    }

    @Override
    public void onBindViewHolder(final ProfileViewHolder holder, int position) {
        holder.mFullNameText.setText(userList.get(position).FullName);
        holder.mAddressText.setText(userList.get(position).Address);
        holder.mBirthDateText.setText(userList.get(position).BirthDate);
        holder.mPhoneNumberText.setText(userList.get(position).PhoneNumber);

        final ElderlyAdapter elderlyAdapter = new ElderlyAdapter();
        holder.mSparkView.setAdapter(elderlyAdapter);

        if (getItemViewType(position) != 1) {
            setStatusText(userList.get(position).Status, holder.mStatusText);

            holder.controller = new SocketController(context, userList.get(position).Username,
                    new SocketController.OnMessageArriveListener() {
                        @Override
                        public void onMessageArrive(double y, String status) {
                            elderlyAdapter.addDataToChart((float) y);
                            elderlyAdapter.notifyDataSetChanged();

                            setStatusText(status, holder.mStatusText);
                        }
                    });

            socketControllers.add(holder.controller);
            holder.controller.connect();

            if (position == 0){
                holder.mSparkView.setVisibility(View.VISIBLE);
            }
        }

        holder.mSparkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getItemViewType(holder.getAdapterPosition()) != 1 &&
                        holder.mSparkView.getVisibility() == View.GONE){
                    holder.mSparkView.setVisibility(View.VISIBLE);
                }else {
                    listener.itemClicked(holder.getAdapterPosition(), userList.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface OnItemListener {
        void itemClicked(int position, User user);
    }
}
