package softagi.chatad.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import softagi.chatad.ChatActivity;
import softagi.chatad.CreateRoomActivity;
import softagi.chatad.Models.RoomModel;
import softagi.chatad.R;

public class ChatsFragment extends Fragment
{
    View view;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private List<RoomModel> roomModelList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.chats_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        initViews();
        initFirebase();
        getRooms();
    }

    private void getRooms()
    {
        databaseReference.child("MyRooms").child(getuID()).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                roomModelList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    RoomModel roomModel = dataSnapshot1.getValue(RoomModel.class);

                    roomModelList.add(roomModel);
                }
                roomAdapter adapter = new roomAdapter(roomModelList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void initFirebase()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    private void initViews()
    {
        recyclerView = view.findViewById(R.id.recyclerview);

        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        roomModelList = new ArrayList<>();
    }

    class roomAdapter extends RecyclerView.Adapter<roomAdapter.roomVH>
    {
        List<RoomModel> models;

        roomAdapter(List<RoomModel> models)
        {
            this.models = models;
        }

        @NonNull
        @Override
        public roomAdapter.roomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.room_item, parent, false);
            return new roomAdapter.roomVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull roomAdapter.roomVH holder, int position)
        {
            final RoomModel roomModel = models.get(position);
            String title = roomModel.getTitle();
            String image = roomModel.getImage();
            final String key = roomModel.getId();

            holder.textView.setText(title);
            Picasso.get().load(image).into(holder.circleImageView);
            holder.button.setText("Exit");
            holder.button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    databaseReference.child("MyRooms").child(getuID()).child(key).removeValue();
                    Toast.makeText(getContext(), "Exit ..", Toast.LENGTH_SHORT).show();
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("roomID", key);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return models.size();
        }

        class roomVH extends RecyclerView.ViewHolder
        {
            TextView textView;
            CircleImageView circleImageView;
            Button button;

            roomVH(@NonNull View itemView)
            {
                super(itemView);

                textView = itemView.findViewById(R.id.room_title_txt);
                circleImageView = itemView.findViewById(R.id.room_image);
                button = itemView.findViewById(R.id.join_btn);
            }
        }
    }

    private String getuID()
    {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }
}
