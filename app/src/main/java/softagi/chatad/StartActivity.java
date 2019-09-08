package softagi.chatad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import softagi.chatad.Fragments.ChatsFragment;
import softagi.chatad.Fragments.HomeFragment;

public class StartActivity extends AppCompatActivity
{
    BottomNavigationView navigation;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        navigation = findViewById(R.id.bottom_navigation);

        HomeFragment homeFragment = new HomeFragment();
        loadFragment(homeFragment);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.home:
                        HomeFragment homeFragment = new HomeFragment();
                        loadFragment(homeFragment);
                        return true;
                    case R.id.chats:
                        ChatsFragment chatsFragment = new ChatsFragment();
                        loadFragment(chatsFragment);
                        return true;
                }
                return false;
            }
        });
    }

    public void loadFragment(Fragment fragment)
    {
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }
}
