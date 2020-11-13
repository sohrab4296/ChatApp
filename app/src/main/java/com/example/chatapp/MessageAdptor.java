package com.example.chatapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdptor extends RecyclerView.Adapter<MessageAdptor.MessageViewHolder> {
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    public MessageAdptor (List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }
    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;
        public MessageViewHolder(View itemView) {
            super(itemView);
            senderMessageText = (TextView) itemView.findViewById(R.id.send_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receive_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_send_image_view);
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_message_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder messageViewHolder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiveImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiveImage).placeholder(R.drawable.logo_app).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        if (fromMessageType.equals("text")){

            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + "-" + messages.getDate());
            }
            else {
                messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + "-" + messages.getDate());
            }
        }
        else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        }
        else if (fromMessageType.equals("pdf") || fromMessageType.equals("docs") || fromMessageType.equals("ppt") || fromMessageType.equals("xlsx")){
            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/hackeridiot-3ea43.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=2065e460-d962-4d8f-acab-bdcfba25fd3e")
                        .into(messageViewHolder.messageSenderPicture);
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/hackeridiot-3ea43.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=2065e460-d962-4d8f-acab-bdcfba25fd3e")
                        .into(messageViewHolder.messageReceiverPicture);
            }
            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docs") || userMessageList.get(position).getType().equals("ppt") || userMessageList.get(position).getType().equals("xlsx")){
                            CharSequence options[] = new CharSequence[]{
                              "Delete From Me.",
                              "Download and View Document.",
                              "Cancel.",
                              "Delete from everyone.",
                            };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (position == 0){

                                    }
                                    else if (position == 1){
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if (position == 2){

                                    }
                                    else if (position == 3){

                                    }
                                }
                            });
                            builder.show();
                        }
                        else if (userMessageList.get(position).getType().equals("text")){
                            CharSequence options[] = new CharSequence[]{
                                    "Delete From Me.",
                                    "Cancel.",
                            };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (position == 0){
                                        deleteSentMessage(position, messageViewHolder);
                                    }
                                    else if (position == 1){

                                    }
                                }
                            });
                            builder.show();
                        }
                        else if (userMessageList.get(position).getType().equals("image")){
                            CharSequence options[] = new CharSequence[]{
                                    "Delete From Me.",
                                    "View Image.",
                                    "Cancel.",
                                    "Delete from everyone.",
                            };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (position == 0){
                                        deleteSentMessage(position, messageViewHolder);
                                    }
                                    else if (position == 1){

                                    }
                                    else if (position == 2){

                                    }
                                    else if (position == 3){
                                        deleteMessageFromEveryone(position, messageViewHolder);
                                    }
                                }
                            });
                            builder.show();
                        }
                    }
                });
            }
        }
        else {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docs") || userMessageList.get(position).getType().equals("ppt") || userMessageList.get(position).getType().equals("xlsx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete From Me.",
                                "Download the document",
                                "Cancel.",
                                "Delete From Everyone.",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (position == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                }
                                else if (position == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (position == 2){

                                }
                                else if (position == 3){
                                    deleteMessageFromEveryone(position, messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessageList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete From Me.",
                                "Cancel.",
                                "Delete from everyone.",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (position == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                }
                                else if (position == 1){

                                }
                                else if (position == 2){
                                    deleteMessageFromEveryone(position, messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessageList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete From Me.",
                                "View Image.",
                                "Cancel.",
                                "Delete from everyone.",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (position == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                }
                                else if (position == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (position == 2){
                                    deleteMessageFromEveryone(position, messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }
    private void deleteSentMessage(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void deleteReceiveMessage(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void deleteMessageFromEveryone(final int position, final MessageViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
