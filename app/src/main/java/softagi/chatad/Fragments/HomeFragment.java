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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import softagi.chatad.CreateRoomActivity;
import softagi.chatad.MainActivity;
import softagi.chatad.Models.RoomModel;
import softagi.chatad.R;

public class HomeFragment extends Fragment
{
    View view;
    private FloatingActionButton create_Room_fab;
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
        view = inflater.inflate(R.layout.home_fragment, container, false);
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
        databaseReference.child("Rooms").addValueEventListener(new ValueEventListener()
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
        create_Room_fab = view.findViewById(R.id.create_room_fab);
        recyclerView = view.findViewById(R.id.recyclerview);

        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        roomModelList = new ArrayList<>();

        create_Room_fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(getContext(), CreateRoomActivity.class));
            }
        });
    }

    class roomAdapter extends RecyclerView.Adapter<roomAdapter.roomVH>
    {
        List<RoomModel> models;

        public roomAdapter(List<RoomModel> models)
        {
            this.models = models;
        }

        @NonNull
        @Override
        public roomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.room_item, parent, false);
            return new roomVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull roomVH holder, int position)
        {
            final RoomModel roomModel = models.get(position);
            String title = roomModel.getTitle();
            String image = roomModel.getImage();
            final String key = roomModel.getId();

            holder.textView.setText(title);
            Picasso.get().load(image).into(holder.circleImageView);
            holder.check(key,roomModel);
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

            void check(final String key, final RoomModel roomModel)
            {
                databaseReference.child("MyRooms").child(getuID()).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(key))
                        {
                            button.setText("Exit");

                            button.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    databaseReference.child("MyRooms").child(getuID()).child(key).removeValue();
                                    Toast.makeText(getContext(), "joined ..", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else
                            {
                                button.setText("Join");

                                button.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        databaseReference.child("MyRooms").child(getuID()).child(key).setValue(roomModel);
                                        Toast.makeText(getContext(), "joined ..", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }
        }
    }

    private String getuID()
    {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }
}
