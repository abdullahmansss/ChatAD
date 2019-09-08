package softagi.chatad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import softagi.chatad.Models.ChatModel;
import softagi.chatad.Models.UserModel;

public class ChatActivity extends AppCompatActivity
{
    EditText chat_field;
    FloatingActionButton send_msg;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    String msg,name,myimg,roomId;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<ChatModel> chatModels;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        roomId = getIntent().getStringExtra("roomID");

        initViews();
        initFirebase();
        getData(getuID());
        FAB();
        getChats(roomId);
    }

    private void getChats(String roomId)
    {
        databaseReference.child("Chats").child(roomId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                chatModels.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    ChatModel chatModel = dataSnapshot1.getValue(ChatModel.class);
                    chatModels.add(chatModel);
                }

                chatAdapter adapter = new chatAdapter(chatModels);
                recyclerView.setAdapter(adapter);
                recyclerView.scrollToPosition(chatModels.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void FAB()
    {
        send_msg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                msg = chat_field.getText().toString();

                if (TextUtils.isEmpty(msg))
                {
                    Toast.makeText(getApplicationContext(), "type a message ..", Toast.LENGTH_SHORT).show();
                    chat_field.requestFocus();
                    return;
                }

                sendMsg(msg,name,myimg,getuID());
            }
        });
    }

    private void initFirebase()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    private void sendMsg(String msg, String name, String myimg, String getuID)
    {
        ChatModel chatModel = new ChatModel(msg,name,myimg,getuID);
        String msgKey = databaseReference.child("Chats").child(roomId).push().getKey();

        if (msgKey != null)
        databaseReference.child("Chats").child(roomId).child(msgKey).setValue(chatModel);
        chat_field.setText("");
    }

    private void getData(String getuID)
    {
        databaseReference.child("Users").child(getuID).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null)
                {
                    name = userModel.getUsername();
                    myimg = userModel.getPicture();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void initViews()
    {
        chat_field = findViewById(R.id.msg_field);
        send_msg = findViewById(R.id.send_msg);
        recyclerView = findViewById(R.id.recyclerview);

        layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        chatModels = new ArrayList<>();
    }

    class chatAdapter extends RecyclerView.Adapter<chatAdapter.chatVH>
    {
        List<ChatModel> models;

        chatAdapter(List<ChatModel> models)
        {
            this.models = models;
        }

        @NonNull
        @Override
        public chatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.chat_item, parent, false);
            return new chatVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull chatVH holder, int position)
        {
            ChatModel chatModel = models.get(position);
            String img = chatModel.getImg();
            String name = chatModel.getName();
            String msg = chatModel.getMsg();
            String id = chatModel.getId();

            holder.name.setText(name);
            holder.chat.setText(msg);

            Picasso.get()
                    .load(img)
                    .into(holder.circleImageView);

            if (id.equals(getuID()))
            {
                holder.linearLayout.setGravity(Gravity.END);
            }
        }

        @Override
        public int getItemCount()
        {
            return models.size();
        }

        class chatVH extends RecyclerView.ViewHolder
        {
            TextView name,chat;
            CircleImageView circleImageView;
            LinearLayout linearLayout;

            chatVH(@NonNull View itemView)
            {
                super(itemView);

                name = itemView.findViewById(R.id.name_txt);
                chat = itemView.findViewById(R.id.msg_txt);
                circleImageView = itemView.findViewById(R.id.msg_img);
                linearLayout = itemView.findViewById(R.id.linear);
            }
        }
    }

    private String getuID()
    {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }
}
